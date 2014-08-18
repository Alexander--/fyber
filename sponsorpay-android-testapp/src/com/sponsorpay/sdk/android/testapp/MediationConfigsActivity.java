package com.sponsorpay.sdk.android.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ExpandableListView;

import com.sponsorpay.sdk.android.testapp.utils.MediationConfigsListAdapter;

public class MediationConfigsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.mediation_configs);
		
		ExpandableListView listView = (ExpandableListView)findViewById(R.id.mediation_configurations);
		
		listView.setAdapter(new MediationConfigsListAdapter(this));
		
	}
	
}
