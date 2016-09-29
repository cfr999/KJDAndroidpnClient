/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidpn.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.util.Log;

import org.androidpn.event.ConnectReturn;
import org.androidpn.event.ConnectSuccess;
import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.ProviderManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Future;

/**
 * This class is to manage the XMPP connection between client and server.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class XmppManager {

    private static final String LOGTAG = LogUtil.makeLogTag(XmppManager.class);

    private static final String XMPP_RESOURCE_NAME = "AndroidpnClient";

    private Context context;

    private NotificationService.TaskSubmitter taskSubmitter;

    private NotificationService.TaskTracker taskTracker;

    private SharedPreferences sharedPrefs;

    private String xmppHost;

    private int xmppPort;

    private XMPPConnection connection;

    private String username;

    private String password;

    private ConnectionListener connectionListener;

    private PacketListener notificationPacketListener;

    private Handler handler;

    //存储线程
    private List<Runnable> taskList;

    //ArrayList是线程不安的，作者使用很多代码保证线程安全，不如直接使用Vector
    private List<Runnable> taskVector;

    private boolean running = false;

    private Future<?> futureTask;

    private Thread reconnection;

    public XmppManager(NotificationService notificationService) {
        context = notificationService;
        taskSubmitter = notificationService.getTaskSubmitter();
        taskTracker = notificationService.getTaskTracker();
        sharedPrefs = notificationService.getSharedPreferences();

        xmppHost = sharedPrefs.getString(Constants.XMPP_HOST, "localhost");
        xmppPort = sharedPrefs.getInt(Constants.XMPP_PORT, 5222);
        username = sharedPrefs.getString(Constants.XMPP_USERNAME, "");
        password = sharedPrefs.getString(Constants.XMPP_PASSWORD, "");

        connectionListener = new PersistentConnectionListener(this);
        notificationPacketListener = new NotificationPacketListener(this);

        handler = new Handler();
        //初始化集合
        taskList = new ArrayList<Runnable>();
        taskVector = new Vector<>();

        reconnection = new ReconnectionThread(this);
    }

    public Context getContext() {
        return context;
    }



    public void terminatePersistentConnection() {
        Log.d(LOGTAG, "terminatePersistentConnection()...");
        Runnable runnable = new Runnable() {

            final XmppManager xmppManager = XmppManager.this;

            public void run() {
                if (xmppManager.isConnected()) {
                    Log.d(LOGTAG, "terminatePersistentConnection()... run()");
                    xmppManager.getConnection().removePacketListener(
                            xmppManager.getNotificationPacketListener());
                    xmppManager.getConnection().disconnect();
                }
//            xmppManager.runTask();
            }

        };
        addTask(runnable);
    }

    public XMPPConnection getConnection() {
        return connection;
    }

    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public PacketListener getNotificationPacketListener() {
        return notificationPacketListener;
    }
    /*****
     * ZHANG 20160831 FIX SOME BUG
     * **/
    public void startReconnectionThread() {
        synchronized (reconnection) {
            if (reconnection==null || !reconnection.isAlive()) {
            	reconnection= new ReconnectionThread(this);
                reconnection.setName("Xmpp Reconnection Thread");
                reconnection.start();
            }
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public void reregisterAccount() {
        removeAccount();
        submitLoginTask();
//        runTask();
    }

    public List<Runnable> getTaskList() {
        return taskList;
    }

    public Future<?> getFutureTask() {
        return futureTask;
    }

    public void runTaskVector(){
        taskVector.add(new ConnectTask());
        taskVector.add(new RegisterTask());
        taskVector.add(new LoginTask());
        for (Runnable runnable : taskVector){
            futureTask = taskSubmitter.submit(runnable);
        }
    }

    //z这个程序这里的写法很死板，他的目的只是想ConnectTask，RegisterTask ，LoginTask
    //依次执行
   public void runTask() {
        Log.d(LOGTAG, "runTask()...");
        synchronized (taskList) {
            running = false;
            futureTask = null;
            if (!taskList.isEmpty()) {
                for(Runnable runnable : taskList){
                    futureTask = taskSubmitter.submit(runnable);
                }
                running = true;
                taskList.clear();
//                Runnable runnable = (Runnable) taskList.get(0);
//                taskList.remove(0);
//                running = true;
//                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            }
        }
        taskTracker.decrease();
        Log.d(LOGTAG, "runTask()...done");
    }

    private String newRandomUUID() {
        String uuidRaw = UUID.randomUUID().toString();
        return uuidRaw.replaceAll("-", "");
    }



   //判断是否连接上
    private boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    public boolean isAuthenticated() {
        return connection != null && connection.isConnected()
                && connection.isAuthenticated();
    }

    private boolean isRegistered() {
        return sharedPrefs.contains(Constants.XMPP_USERNAME)
                && sharedPrefs.contains(Constants.XMPP_PASSWORD);
    }

    public void connect() {
        Log.d(LOGTAG, "connect()...");
//        submitLoginTask();
        runTaskVector();
    }

    public void disconnect() {
        Log.d(LOGTAG, "disconnect()...");
        terminatePersistentConnection();
    }

    private void submitConnectTask() {
        Log.d(LOGTAG, "submitConnectTask()...");
        addTask(new ConnectTask());
    }

    private void submitRegisterTask() {
        Log.d(LOGTAG, "submitRegisterTask()...");
        submitConnectTask();
        addTask(new RegisterTask());
    }

    private void submitLoginTask() {
        Log.d(LOGTAG, "submitLoginTask()...");
        submitRegisterTask();
        addTask(new LoginTask());
    }

    private void addTask(Runnable runnable) {
        Log.d(LOGTAG, "addTask(runnable)...");
        taskTracker.increase();
        synchronized (taskList) {
            if (taskList.isEmpty() && !running) {
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            } else {
                taskList.add(runnable);
            }
        }
        Log.d(LOGTAG, "addTask(runnable)... done");
    }

    private void removeAccount() {
        Editor editor = sharedPrefs.edit();
        editor.remove(Constants.XMPP_USERNAME);
        editor.remove(Constants.XMPP_PASSWORD);
        editor.commit();
    }
    /*****
     * ZHANG 20160901 ADD DROP TASK INTHOD**
     * **/
    private void dropTask(int dropCount){
    	synchronized (taskList){
	    	if(taskList.size()>=dropCount){
	    		for(int i =0;i<dropCount;i++){
	    			taskList.remove(0);
	    			taskTracker.decrease();
	    		}
	    	}
    	}
    }
    /**
     * A runnable task to connect the server. 
     */
    private class ConnectTask implements Runnable {

        final XmppManager xmppManager;

        private ConnectTask() {
            this.xmppManager = XmppManager.this;
        }

        private String getPrefixPort(){
            return "192.168.1.";
        }
        ConnectionConfiguration connConfig;
        public void run() {
            Log.i(LOGTAG, "ConnectTask.run()...");

            if (!xmppManager.isConnected()) {
                // Create the configuration for this new connection

                for (int i = 0 ; i < 255 ; i++){
                    String s = getPrefixPort() + i;
                    connConfig = new ConnectionConfiguration(
                            xmppHost, xmppPort);
                    Log.d("ServicAddress" , s);
                }

//                ConnectionConfiguration connConfig = new ConnectionConfiguration(
//                        xmppHost, xmppPort);
                // connConfig.setSecurityMode(SecurityMode.disabled);
                connConfig.setSecurityMode(SecurityMode.required);
                connConfig.setSASLAuthenticationEnabled(false);
                connConfig.setCompressionEnabled(false);

                XMPPConnection connection = new XMPPConnection(connConfig);
                xmppManager.setConnection(connection);

                try {
                    // Connect to the server
                    connection.connect();
                    Log.i(LOGTAG, "XMPP connected successfully");

                    // packet provider
                    ProviderManager.getInstance().addIQProvider("notification",
                            "androidpn:iq:notification",
                            new NotificationIQProvider());

                    //在这里提交其他连个任务,xmppManager.runTask在这里调用一次就够了，以后不用再调用了
                    //因为里面的任务提交完了，剩下的就等待线程池依次执行这些任务（感觉本程序的作者对线程池不熟悉）
//                    xmppManager.runTask();//ZHANG 20160901 ADD



//                    EventBus.getDefault().
//                            post(new ConnectReturn("tastList count:"+taskList.size()));

                } catch (XMPPException e) {
                    Log.e(LOGTAG, "XMPP connection failed", e);
                    /**
                     * ZHANG 20160901 ADD connect fail need drop register&login tash,and reconnect server
                     * 
                     * **/
                    xmppManager.dropTask(2);
                    xmppManager.startReconnectionThread();
//                    xmppManager.runTask();
                }

                //xmppManager.runTask(); ZHANG 20160901 REMOVE

            } else {
                //发送连接成功信息
//                EventBus.getDefault().post(new ConnectReturn());

                Log.i(LOGTAG, "XMPP connected already");
//                xmppManager.runTask(); //已在runTask做处理，一次性提交任务，此时taskList为空。
            }
        }
    }

    /**
     * A runnable task to register a new user onto the server. 
     */
    private class RegisterTask implements Runnable {

        final XmppManager xmppManager;
        boolean isRegisterSuccessed;
        boolean hasDropTask;
        private RegisterTask() {
            xmppManager = XmppManager.this;
        }

        public void run() {
            Log.i(LOGTAG, "RegisterTask.run()...");

            if (!xmppManager.isRegistered()) {
            	isRegisterSuccessed=false;
            	hasDropTask=false;
                final String newUsername = newRandomUUID();
                final String newPassword = newRandomUUID();

                Registration registration = new Registration();

                PacketFilter packetFilter = new AndFilter(new PacketIDFilter(
                        registration.getPacketID()), new PacketTypeFilter(
                        IQ.class));

                PacketListener packetListener = new PacketListener() {

                    public void processPacket(Packet packet) {
                    	synchronized(xmppManager){
//                    		Log.d("RegisterTask.PacketListener",
//                                    "processPacket().....");
//                            Log.d("RegisterTask.PacketListener", "packet="
//                                    + packet.toXML());

                            if (packet instanceof IQ) {
                                IQ response = (IQ) packet;
                                if (response.getType() == IQ.Type.ERROR) {
                                    if (!response.getError().toString().contains(
                                            "409")) {
                                        Log.e(LOGTAG,"Unknown error while registering XMPP account! "
                                                        + response.getError()
                                                                .getCondition());
                                    }
                                } else if (response.getType() == IQ.Type.RESULT) {
                                    xmppManager.setUsername(newUsername);
                                    xmppManager.setPassword(newPassword);
                                    Log.d(LOGTAG, "username=" + newUsername);
                                    Log.d(LOGTAG, "password=" + newPassword);

                                    Editor editor = sharedPrefs.edit();
                                    editor.putString(Constants.XMPP_USERNAME,
                                            newUsername);
                                    editor.putString(Constants.XMPP_PASSWORD,
                                            newPassword);
                                    editor.commit();
                                    isRegisterSuccessed=true;
                                    Log.i(LOGTAG,"Account registered successfully");
                                    if(!hasDropTask){
//                                    	xmppManager.runTask();
                                    }
                                }
                            }
                    	}
                        
                    }
                };

                connection.addPacketListener(packetListener, packetFilter);

                registration.setType(IQ.Type.SET);
                // registration.setTo(xmppHost);
                // Map<String, String> attributes = new HashMap<String, String>();
                // attributes.put("username", rUsername);
                // attributes.put("password", rPassword);
                // registration.setAttributes(attributes);
                registration.addAttribute("username", newUsername);
                registration.addAttribute("password", newPassword);
                connection.sendPacket(registration);
                try {
					Thread.sleep(10*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                /****
                 * ZHANG 20160901 ADD
                 * *****/
                synchronized(xmppManager){
	                if(!isRegisterSuccessed)
	                {
		                xmppManager.dropTask(1);
		                xmppManager.startReconnectionThread();
//		                xmppManager.runTask();
		                hasDropTask=true;
	                }
                }

            } else {
                Log.i(LOGTAG, "Account registered already");
//                xmppManager.runTask();
                EventBus.getDefault().
                        post(new ConnectReturn("tastList count:"+taskList.size()));
            }
        }
    }

    /**
     * A runnable task to log into the server. 
     */
    private class LoginTask implements Runnable {

        final XmppManager xmppManager;

        private LoginTask() {
            this.xmppManager = XmppManager.this;
        }

        public void run() {
            Log.i(LOGTAG, "LoginTask.run()...");

            if (!xmppManager.isAuthenticated()) {
                Log.d(LOGTAG, "username=" + username);
                Log.d(LOGTAG, "password=" + password);

                try {
                    xmppManager.getConnection().login(
                            xmppManager.getUsername(),
                            xmppManager.getPassword(), XMPP_RESOURCE_NAME);
                    Log.d(LOGTAG, "Loggedn in successfully");

                    // connection listener
                    if (xmppManager.getConnectionListener() != null) {
                        xmppManager.getConnection().addConnectionListener(
                                xmppManager.getConnectionListener());
                    }

                    // packet filter
                    PacketFilter packetFilter = new PacketTypeFilter(
                            NotificationIQ.class);
                    // packet listener
                    PacketListener packetListener = xmppManager
                            .getNotificationPacketListener();
                    connection.addPacketListener(packetListener, packetFilter);
                    //ZHANG 20160831 ADD 
                    connection.startHeartBeatThread();
                    synchronized(xmppManager){
                    	xmppManager.notifyAll();
                    }

                    //xmppManager.runTask();//ZHANG 20160901 REMOVE

                } catch (XMPPException e) {
                    Log.e(LOGTAG, "LoginTask.run()... xmpp error");
                    Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
                            + e.getMessage());
                    String INVALID_CREDENTIALS_ERROR_CODE = "401";
                    String errorMessage = e.getMessage();
                    if (errorMessage != null
                            && errorMessage
                                    .contains(INVALID_CREDENTIALS_ERROR_CODE)) {
                        xmppManager.reregisterAccount();
                        return;
                    }
                    xmppManager.startReconnectionThread();

                } catch (Exception e) {
                    Log.e(LOGTAG, "LoginTask.run()... other error");
                    Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
                            + e.getMessage());
                    xmppManager.startReconnectionThread();
                }finally{
//                	xmppManager.runTask();
                }

            } else {
                Log.i(LOGTAG, "Logged in already");
//                xmppManager.runTask();
            }

        }
    }

}
