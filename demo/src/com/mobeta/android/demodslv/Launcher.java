package com.mobeta.android.demodslv;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class Launcher extends ListActivity {

	private ArrayList<ActivityInfo> mActivities = null;

	private String[] mActTitles;
	private String[] mActDescs;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launcher);

		try {
			PackageInfo pi = getPackageManager().getPackageInfo(
					"com.mobeta.android.demodslv", PackageManager.GET_ACTIVITIES);

			mActivities = new ArrayList<ActivityInfo>(Arrays.asList(pi.activities));
			String[] excludeList = new String[]{getClass().getName(),
					MainSettingsActivity.class.getName()};
			for (int i = 0; i < mActivities.size(); ++i) {
				for (String name: excludeList) {
					if (name.equals(mActivities.get(i).name)) {
						mActivities.remove(i);
						break;
					}
				}
			}
		} catch (PackageManager.NameNotFoundException e) {
			// Do nothing. Adapter will be empty.
		}

		mActTitles = getResources().getStringArray(R.array.activity_titles);
		mActDescs = getResources().getStringArray(R.array.activity_descs);

		setListAdapter(new MyAdapter());
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent();
		intent.setClassName(this, mActivities.get(position).name);
		startActivity(intent);
	}

	private class MyAdapter extends ArrayAdapter<ActivityInfo> {
		MyAdapter() {
			super(Launcher.this, R.layout.launcher_item, R.id.activity_title, mActivities);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);

			TextView title = (TextView) v.findViewById(R.id.activity_title);
			TextView desc = (TextView) v.findViewById(R.id.activity_desc);

			title.setText(mActTitles[position]);
			desc.setText(mActDescs[position]);
			return v;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Intent intent;
		switch (item.getItemId()) {
		case R.id.menu_settings:
			intent =new Intent(this,MainSettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
