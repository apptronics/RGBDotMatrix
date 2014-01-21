package com.example.rgbdotmatrix;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListActivity extends Activity {

	private ArrayAdapter<String> mArrayAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		//Intent intent = new Intent(ListActivity.this, MainActivity.class);
		Intent intent = getIntent();
		final String[] listData = intent.getStringArrayExtra("list");

		mArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listData);
		final ListView list = (ListView) findViewById(R.id.list);
		list.setAdapter(mArrayAdapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				MainActivity.mListItem = listData[arg2];
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_list, menu);
		return true;
	}
} //end of class
