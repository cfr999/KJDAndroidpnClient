package org.androidpn.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.androidpn.demoapp.R;

public class BootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Toast.makeText(context, "Boot Completed ", Toast.LENGTH_LONG);
		SharedPreferences preferences = context.getSharedPreferences(
				Constants.SHARED_PREFERENCE_NAME, Context.MODE_APPEND);
		if (preferences.getBoolean(Constants.SETTINGS_AUTOSTART, true)) {
			ServiceManager serviceManager = new ServiceManager(context);
			serviceManager.setNotificationIcon(R.drawable.notification);
			serviceManager.startService();
			Log.d("BootCompletedReceiver",
					"boot compeleted,serviceManager startService");
		}

	}

}
