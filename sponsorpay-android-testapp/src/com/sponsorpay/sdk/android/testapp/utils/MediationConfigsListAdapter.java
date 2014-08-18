/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.testapp.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.sponsorpay.mediation.SPMediationConfigurator;


public class MediationConfigsListAdapter extends BaseExpandableListAdapter {

	private static ArrayList<String> adapters = new ArrayList<String>(0);

	private static boolean hasMockMediatedNetwork;
	
	public static void setAdapters(List<String> list) {
		adapters = new ArrayList<String>(list);
		hasMockMediatedNetwork = adapters.remove("mockmediatednetwork");
	}
	
	public static boolean hasAdapter() {
		return adapters.size() > 0;
	}
	
	public static boolean hasMock() {
		return hasMockMediatedNetwork;
	}
	
	private LayoutInflater inflater;

	private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
		}
	};
	
	public MediationConfigsListAdapter(Activity activity) {
		inflater = activity.getLayoutInflater();
	  }
	
	@Override
	public Object getChild(int group, int child) {
		return SPMediationConfigurator.INSTANCE
				.getConfigurationForAdapter(adapters.get(group)).entrySet()
				.toArray()[child];
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		@SuppressWarnings("unchecked")
		Entry<String, Object> child = (Entry<String, Object>) getChild(groupPosition, childPosition);
	    TextView text = null;
	    if (convertView == null) {
	      convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
	      convertView.setOnClickListener(listener);
	    }
	    convertView.setTag(child);
	    text = (TextView) convertView.findViewById(android.R.id.text1);
	    text.setText(child.getKey() + " - " + child.getValue().toString());

	    return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return SPMediationConfigurator.INSTANCE.getConfigurationForAdapter(adapters.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return adapters.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return adapters.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_2, null);
		}
		TextView text = null;
		text = (TextView) convertView.findViewById(android.R.id.text1);
		text.setText(getGroup(groupPosition).toString());

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
		
}
