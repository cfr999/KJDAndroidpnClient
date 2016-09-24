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
package org.androidpn.demoapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import org.androidpn.client.Constants;
import org.androidpn.client.NotificationDetailsActivity;
import org.androidpn.client.NotificationHistoryActivity;
import org.androidpn.client.ServiceManager;
import org.androidpn.event.ConnectSuccess;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This is an androidpn client demo application.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class DemoAppActivity extends Activity {

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("DemoAppActivity", "onCreate()...");

        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //服务器地址
        final EditText editText = (EditText) findViewById(R.id.edit_serverip);

        //是否保存服务器地址
        final AppCompatCheckBox remmeberAddress = (AppCompatCheckBox)findViewById(R.id.saveaddress);
        // Settings
        Button okButton = (Button) findViewById(R.id.btn_settings);

        Button historyButton = (Button) findViewById(R.id.btn_histories);

        Button image = (Button) findViewById(R.id.image);

        Button test = (Button) findViewById(R.id.test);

        Button con_btn=(Button)findViewById(R.id.btn_connect);

        boolean isRememberAddress = mSharedPreferences.getBoolean(Constants.IS_SAVE_ADDRESS_SERVICE , false);

        if (isRememberAddress){
            editText.setText(mSharedPreferences.getString(Constants.SERVICE_ADDRESS , ""));
            remmeberAddress.setChecked(true);
        }

        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ServiceManager.viewNotificationSettings(DemoAppActivity.this);
            }
        });

		historyButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(DemoAppActivity.this,
						NotificationHistoryActivity.class);
				startActivity(intent);
			}
		});

		image.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(DemoAppActivity.this , SkimActivity.class));

			}
		});


		test.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(DemoAppActivity.this , NotificationDetailsActivity.class));
			}
		});


        con_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
		        Properties props = new Properties();
		        /*try {
		            int id = DemoAppActivity.this.getResources().getIdentifier("androidpn", "raw",
		            		DemoAppActivity.this.getPackageName());
		            props.load(DemoAppActivity.this.getResources().openRawResource(id));
		        } catch (Exception e) {
		            Log.e("ZLJ", "Could not find the properties file.", e);
		            // e.printStackTrace();
		        }*/
		        //����ļ���androidpn.properties
		        File propfile=new File(Environment.getExternalStorageDirectory(),"androidpn.properties");
		        
		        try {
		        	//�ж��ļ����Ƿ���ڣ�����������½�һ���ļ���
		        	if (!propfile.exists())
			        	propfile.createNewFile();
					FileOutputStream fileOutputStream = new FileOutputStream(propfile);
					//���rawĿ¼��androidpn��Դid
					int id = DemoAppActivity.this.getResources().getIdentifier("androidpn", "raw",
		            		DemoAppActivity.this.getPackageName());
					//��ȡ��Դ
					InputStream inStream = DemoAppActivity.this.getResources().openRawResource(id);
					//ÿ�ζ�ȡ10���ֽ�
					byte[] buffer = new byte[10];  
			        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			        int len = 0;  
			        while((len = inStream.read(buffer)) != -1) {  
			            outStream.write(buffer, 0, len);  
			        }  
			        byte[] bs = outStream.toByteArray(); 
			        //�Ѷ�ȡ����Դд�뵽android.properties
			        fileOutputStream.write(bs);  
			        outStream.close();  
			        inStream.close();  
			        fileOutputStream.flush();  
			        fileOutputStream.close(); 
			        props.load(new FileInputStream(Environment.getExternalStorageDirectory()+"/androidpn.properties"));
			        EditText serverip=(EditText)findViewById(R.id.edit_serverip);
			        //��ȡ����ķ�������ַ
			        String str_serverip=serverip.getText().toString();
			        Log.d("ZLJ","SERVERIP =="+str_serverip);
			        props.setProperty("xmppHost", str_serverip);
			        OutputStream fos;
			        fos = new FileOutputStream(Environment.getExternalStorageDirectory()+"/androidpn.properties");
			        //���� Properties ���е������б�����Ԫ�ضԣ�д���������
        			props.store(fos, null);
        			fos.flush();
        			fos.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        

		     // Start the service
		        ServiceManager serviceManager = new ServiceManager(DemoAppActivity.this);
		        serviceManager.setNotificationIcon(R.drawable.notification);
		        //������ȡ��̨����
		        serviceManager.startService();
		        serviceManager.setAlias("test123");
				List<String> tagsList = new ArrayList<String>();
				tagsList.add("sport");
				tagsList.add("music");
				// tagsList.add("");
				serviceManager.setTags(tagsList);

                mEditor = mSharedPreferences.edit();
                if (remmeberAddress.isChecked()){
                    String address = editText.getText().toString().trim();
                    mEditor.putString(Constants.SERVICE_ADDRESS , address);
                    mEditor.putBoolean(Constants.IS_SAVE_ADDRESS_SERVICE , true);

                }else {
                    mEditor.clear();
                }
                mEditor.commit();
			}
		});
        
        
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void JumpToSkimActivity(ConnectSuccess connectSuccess){
        Toast.makeText(this , connectSuccess.getConnectSuccess() , Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this , SkimActivity.class));

    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}