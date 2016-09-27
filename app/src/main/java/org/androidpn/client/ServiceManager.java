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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/** 
 * This class is to manage the notificatin service and to load the configuration.
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public final class ServiceManager {

    private static final String LOGTAG = LogUtil
            .makeLogTag(ServiceManager.class);

    private Context context;

    private SharedPreferences sharedPrefs;

    private Properties props;

    private String version = "0.5.0";

    private String apiKey;

    private String xmppHost;

    private String xmppPort;

    private String callbackActivityPackageName;

    private String callbackActivityClassName;

    public ServiceManager(Context context) {
        this.context = context;

        if (context instanceof Activity) {
            Log.i(LOGTAG, "Callback Activity...");
            Activity callbackActivity = (Activity) context;
            callbackActivityPackageName = callbackActivity.getPackageName();
            callbackActivityClassName = callbackActivity.getClass().getName();
        }

        //        apiKey = getMetaDataValue("ANDROIDPN_API_KEY");
        //        Log.i(LOGTAG, "apiKey=" + apiKey);
        //        //        if (apiKey == null) {
        //        //            Log.e(LOGTAG, "Please set the androidpn api key in the manifest file.");
        //        //            throw new RuntimeException();
        //        //        }
		//从外部存储读取androidpn参数
        props = loadProperties();
        apiKey = props.getProperty("apiKey", "");
        xmppHost = props.getProperty("xmppHost", "127.0.0.1");
        xmppPort = props.getProperty("xmppPort", "5222");
        Log.i(LOGTAG, "apiKey=" + apiKey);
        Log.i(LOGTAG, "xmppHost=" + xmppHost);
        Log.i(LOGTAG, "xmppPort=" + xmppPort);

        sharedPrefs = context.getSharedPreferences(
                Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putString(Constants.API_KEY, apiKey);
        editor.putString(Constants.VERSION, version);
        editor.putString(Constants.XMPP_HOST, xmppHost);
        editor.putInt(Constants.XMPP_PORT, Integer.parseInt(xmppPort));
        editor.putString(Constants.CALLBACK_ACTIVITY_PACKAGE_NAME,
                callbackActivityPackageName);
        editor.putString(Constants.CALLBACK_ACTIVITY_CLASS_NAME,
                callbackActivityClassName);
        editor.commit();
        // Log.i(LOGTAG, "sharedPrefs=" + sharedPrefs.toString());
    }

    public void startService() {
        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = NotificationService.getIntent();
				//5.0以上不能隐试启动server
                context.startService(new Intent(context ,NotificationService.class));
            }
        });
        serviceThread.start();
    }

    public void stopService() {
        Intent intent = NotificationService.getIntent();
        context.stopService(intent);
    }
    /***
     * ZHANG 20160907 ADD
     * *********/
    public void setAlias(final String alias){
    	final String username=sharedPrefs.getString(Constants.XMPP_USERNAME, "");
    	Log.d("ZLJ","SET ALIAS ===="+username);
    	if (TextUtils.isEmpty(alias)|| TextUtils.isEmpty(username)){
    		return;
    	}
    	new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				NotificationService notificationService=NotificationService.getNotificationService();
		    	XmppManager xmppManager=notificationService.getXmppManager();
		    	if(xmppManager!=null){
		    		if(!xmppManager.isAuthenticated()){
		    		
		    				try {
		    						synchronized(xmppManager){
			    						Log.d(LOGTAG, "wait for authenticated...");
			    						xmppManager.wait();
		    						}
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		    			}
		    			Log.d(LOGTAG,"authenticated already, send SetAliasIQ now...");
			    		SetAliasIQ iq=new SetAliasIQ();
				    	iq.setType(Type.SET);
				    	iq.setAlias(alias);
				    	iq.setUsername(username);
				    	xmppManager.getConnection().sendPacket(iq);
		    		}
			    	
		    	}			
    		
    	}).start();
    	
    	
    }

	public void setTags(final List<String> tagsList) {
		final String username = sharedPrefs.getString(Constants.XMPP_USERNAME,
				"");
		if (tagsList == null || tagsList.isEmpty()
				|| TextUtils.isEmpty(username)) {
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				NotificationService notificationService = NotificationService
						.getNotificationService();
				XmppManager xmppManager = notificationService.getXmppManager();
				if (xmppManager != null) {
					if (!xmppManager.isAuthenticated()) {
						try {
							synchronized (xmppManager) {
								Log.d(LOGTAG,
										"settags wait for authenticated...");
								xmppManager.wait();
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
				Log.d(LOGTAG, "authenticated alreadly , send settagsiq now");
				SetTagsIQ iq = new SetTagsIQ();
				iq.setType(IQ.Type.SET);
				iq.setUsername(username);
				iq.setTagList(tagsList);
				xmppManager.getConnection().sendPacket(iq);
			}
		}).start();

	}

    //    private String getMetaDataValue(String name, String def) {
    //        String value = getMetaDataValue(name);
    //        return (value == null) ? def : value;
    //    }
    //
    //    private String getMetaDataValue(String name) {
    //        Object value = null;
    //        PackageManager packageManager = context.getPackageManager();
    //        ApplicationInfo applicationInfo;
    //        try {
    //            applicationInfo = packageManager.getApplicationInfo(context
    //                    .getPackageName(), 128);
    //            if (applicationInfo != null && applicationInfo.metaData != null) {
    //                value = applicationInfo.metaData.get(name);
    //            }
    //        } catch (NameNotFoundException e) {
    //            throw new RuntimeException(
    //                    "Could not read the name in the manifest file.", e);
    //        }
    //        if (value == null) {
    //            throw new RuntimeException("The name '" + name
    //                    + "' is not defined in the manifest file's meta data.");
    //        }
    //        return value.toString();
    //    }

    private Properties loadProperties() {
        //        InputStream in = null;
        //        Properties props = null;
        //        try {
        //            in = getClass().getResourceAsStream(
        //                    "/org/androidpn/client/client.properties");
        //            if (in != null) {
        //                props = new Properties();
        //                props.load(in);
        //            } else {
        //                Log.e(LOGTAG, "Could not find the properties file.");
        //            }
        //        } catch (IOException e) {
        //            Log.e(LOGTAG, "Could not find the properties file.", e);
        //        } finally {
        //            if (in != null)
        //                try {
        //                    in.close();
        //                } catch (Throwable ignore) {
        //                }
        //        }
        //        return props;

        Properties props = new Properties();
        try {
			props.load(new FileInputStream(Environment.getExternalStorageDirectory()+"/androidpn.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        /*try {
            int id = context.getResources().getIdentifier("androidpn", "raw",
                    context.getPackageName());
            props.load(context.getResources().openRawResource(id));
        } catch (Exception e) {
            Log.e(LOGTAG, "Could not find the properties file.", e);
            // e.printStackTrace();
        }*/
        return props;
    }

    //    public String getVersion() {
    //        return version;
    //    }
    //
    //    public String getApiKey() {
    //        return apiKey;
    //    }

    public void setNotificationIcon(int iconId) {
        Editor editor = sharedPrefs.edit();
        editor.putInt(Constants.NOTIFICATION_ICON, iconId);
        editor.commit();
    }

    //    public void viewNotificationSettings() {
    //        Intent intent = new Intent().setClass(context,
    //                NotificationSettingsActivity.class);
    //        context.startActivity(intent);
    //    }

    public static void viewNotificationSettings(Context context) {
        Intent intent = new Intent().setClass(context,
                NotificationSettingsActivity.class);
        context.startActivity(intent);
    }

}
