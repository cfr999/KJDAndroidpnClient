package org.androidpn.demoapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.androidpn.client.ServiceManager;
import org.androidpn.event.SearchSuccess;
import org.androidpn.view.RadarView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SearchActivity extends AppCompatActivity {

    RadarView mRadarView;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_service);

        mRadarView= (RadarView) findViewById(R.id.radar_view);
        mRadarView.setSearching(true);
        ServiceManager serviceManager = new ServiceManager(this);
        serviceManager.setNotificationIcon(R.drawable.notification);
        //������ȡ��̨����
        serviceManager.startService();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void searchSuccess(SearchSuccess searchSuccess){
        Toast.makeText(this , searchSuccess.getSearch() , Toast.LENGTH_SHORT).show();
        mRadarView.addPoint();
        startActivity(new Intent(this , SkimActivity.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRadarView.closeInvalidateThread();
        EventBus.getDefault().unregister(this);
    }
}
