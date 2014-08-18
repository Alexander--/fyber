package com.sponsorpay.sdk.android.testapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sponsorpay.publisher.SponsorPayPublisher;
import com.sponsorpay.publisher.interstitial.SPInterstitialActivity;
import com.sponsorpay.publisher.interstitial.SPInterstitialAdCloseReason;
import com.sponsorpay.publisher.interstitial.SPInterstitialRequestListener;
import com.sponsorpay.sdk.android.testapp.R;
import com.sponsorpay.sdk.android.testapp.SponsorpayAndroidTestAppActivity;
import com.sponsorpay.utils.SponsorPayLogger;

public class InterstitialFragment extends AbstractSettingsFragment implements SPInterstitialRequestListener {

	private static final String TAG = "InterstitialFragment";
	private static final int INTERSTITIAL_REQUEST_CODE = 2114;
	
	private Intent mIntent;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_interstitial, container, false);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case INTERSTITIAL_REQUEST_CODE:
				SPInterstitialAdCloseReason adStatus = (SPInterstitialAdCloseReason) data.getSerializableExtra(SPInterstitialActivity.SP_AD_STATUS);
				SponsorPayLogger.d(TAG, "SPInterstitial closed with status - " + adStatus);
				if (adStatus.equals(SPInterstitialAdCloseReason.ReasonError)) {
					String error = data.getStringExtra(SPInterstitialActivity.SP_ERROR_MESSAGE);
					SponsorPayLogger.d(TAG, "SPInterstitial closed and error - " + error);
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	protected void setValuesInFields() {
		// nothing to do
	}

	@Override
	protected String getFragmentTitle() {
		return getString(R.string.interstitial);
	}

	@Override
	protected void bindViews() {
	}

	@Override
	protected void fetchValuesFromFields() {
		// nothing to do
	}

	@Override
	protected void readPreferences(SharedPreferences prefs) {
		// nothing to do
	}

	@Override
	protected void storePreferences(Editor prefsEditor) {
		// nothing to do
	}
	
	@Override
	public void onSPInterstitialAdError(String error) {
		SponsorPayLogger.e(TAG, error);
	}

	public void showAds() {
		if (mIntent != null) {
			SponsorPayLogger.d(TAG, "Starting Interstitial Ad...");
			startActivityForResult(mIntent, INTERSTITIAL_REQUEST_CODE);
			mIntent = null;
		}
	}

	public void requestAds() {
		try {
			SponsorPayPublisher.getIntentForInterstitialActivity(getActivity(), this);
		} catch (RuntimeException ex) {
			showCancellableAlertBox("Exception from SDK", ex.getMessage());
			Log.e(SponsorpayAndroidTestAppActivity.class.toString(), "SponsorPay SDK Exception: ",
					ex);
		}
	}

	@Override
	public void onSPInterstitialAdAvailable(Intent interstitialActivity) {
		mIntent = interstitialActivity;
		SponsorPayLogger.i(TAG, "Ads are available");
	}

	@Override
	public void onSPInterstitialAdNotAvailable() {
		mIntent = null;
		SponsorPayLogger.i(TAG, "Ads are not available");
	}

	
}
