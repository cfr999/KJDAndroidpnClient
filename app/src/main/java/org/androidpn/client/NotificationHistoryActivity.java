package org.androidpn.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.androidpn.demoapp.R;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class NotificationHistoryActivity extends Activity {
	private ListView mListView;

	private NotificationHistoryAdapter mAdapter;

	private List<NotificationHistory> mList = new ArrayList<NotificationHistory>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notification_history);
		mList = DataSupport.findAll(NotificationHistory.class);
		mListView = (ListView) findViewById(R.id.listView1);
		mAdapter = new NotificationHistoryAdapter(this, 0, mList);
		mListView.setAdapter(mAdapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
			NotificationHistory history = mList.get(position);
			Intent intent = new Intent(NotificationHistoryActivity.this,
					NotificationDetailsActivity.class);
			intent.putExtra(Constants.NOTIFICATION_API_KEY,
					history.getApiKey());
			intent.putExtra(Constants.NOTIFICATION_TITLE,
					history.getTitle());
			intent.putExtra(Constants.NOTIFICATION_MESSAGE,
					history.getMessage());
			intent.putExtra(Constants.NOTIFICATION_URI, history.getUri());
			intent.putExtra(Constants.NOTIFICATION_IMAGE_URI,
					history.getImageUrl());
			startActivity(intent);
		}
	});
	registerForContextMenu(mListView);
}

@Override
public void onCreateContextMenu(ContextMenu menu, View v,
								ContextMenuInfo menuInfo) {
	// TODO Auto-generated method stub
	super.onCreateContextMenu(menu, v, menuInfo);
	menu.add(0, 0, 0, "Remove");
}

@Override
public boolean onContextItemSelected(MenuItem item) {
	// TODO Auto-generated method stub
	if (item.getItemId() == 0) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int index = menuInfo.position;
		NotificationHistory history = mList.get(index);
		history.delete();
		mList.remove(index);
		mAdapter.notifyDataSetChanged();
	}
	return super.onContextItemSelected(item);
}

	class NotificationHistoryAdapter extends ArrayAdapter<NotificationHistory> {

		public NotificationHistoryAdapter(Context context, int resource,
										  List<NotificationHistory> objects) {
			super(context, resource, objects);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			NotificationHistory history = getItem(position);
			View view = null;
			if (convertView == null) {
				view = LayoutInflater.from(getContext()).inflate(
						R.layout.notification_history_item, null);
			} else {
				view = convertView;
			}
			TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
			TextView tv_time = (TextView) view.findViewById(R.id.tv_time);
			TextView tv_videoUrl = (TextView) view.findViewById(R.id.videourl);
			TextView tv_musicUrl = (TextView) view.findViewById(R.id.musicurl);
			
			tv_title.setTextColor(Color.WHITE);
			tv_time.setTextColor(Color.WHITE);
			tv_videoUrl.setTextColor(Color.WHITE);
			tv_musicUrl.setTextColor(Color.WHITE);
			
			tv_title.setText(history.getTitle());
			tv_time.setText(history.getTime());
			
			tv_videoUrl.setText("videoUrl:"+history.getVideoUrl());
			tv_musicUrl.setText("musicUrl:"+history.getMusicUrl());
			
			Log.d("ZLJ","HISTORY TITLE==="+history.getTitle()+"history time ==="+history.getTime());
			return view;
		}

	}

}
