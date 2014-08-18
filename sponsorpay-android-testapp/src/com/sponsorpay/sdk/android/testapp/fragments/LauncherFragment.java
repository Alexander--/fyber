package com.sponsorpay.sdk.android.testapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sponsorpay.sdk.android.testapp.R;
import com.sponsorpay.sdk.android.testapp.utils.MediationConfigsListAdapter;

public class LauncherFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings_launcher, container, false);
		
		return view;
	}
	
	
	@Override
	public void onResume() {
		checkButtons();
		super.onResume();
	}
	
	public void refreshLayout() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				checkButtons();
			}
		});

	}
	
	private void checkButtons() {
		Button videoMockSettingView = (Button) getActivity().findViewById(R.id.video_mock_mediation_button);
		videoMockSettingView.setVisibility(MediationConfigsListAdapter.hasMock() ? View.VISIBLE : View.GONE);
		
		Button interstitialoMockSettingView = (Button) getActivity().findViewById(R.id.interstitial_mock_mediation_button);
		interstitialoMockSettingView.setVisibility(MediationConfigsListAdapter.hasMock() ? View.VISIBLE : View.GONE);
		
		Button mediationSettingView = (Button) getActivity().findViewById(R.id.mediation_settings_button);
		mediationSettingView.setVisibility(MediationConfigsListAdapter.hasAdapter() ? View.VISIBLE : View.GONE);
	}
	
}
