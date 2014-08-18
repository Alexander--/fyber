package com.sponsorpay.sdk.android.testapp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sponsorpay.advertiser.SponsorPayAdvertiserState;
import com.sponsorpay.publisher.SponsorPayPublisher;
import com.sponsorpay.sdk.android.testapp.utils.TestAppParametersProvider;
import com.sponsorpay.sdk.android.testapp.utils.TestAppUrlsProvider;
import com.sponsorpay.utils.HostInfo;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;

import org.json.*;

public class MainSettingsActivity extends Activity {
	
	/**
	 * Shared preferences file name. Stores the values entered into the UI fields.
	 */
	private static final String TAG = MainSettingsActivity.class.getSimpleName();

	private static Boolean mRefuseCookies = false;

	public static final String PREFERENCES_EXTRA = "prefs.extra";
	public static final String KEEP_OFFERWALL_OPEN_EXTRA = "keep.offerwall.open.extra";
	public static final String SHOW_TOAST_VCS_REQUEST_EXTRA = "show.toast.vcs.request.extra";
	
	public static final String OVERRIDING_URL_PREFS_KEY = "OVERRIDING_URL";
	public static final String OVERRIDING_URL_QUERY_PARAMS = "OVERRIDING_URL_QUERY_PARAMS";
	// this is public in order to be shared with the main activity 
	public static final String KEEP_OFFERWALL_OPEN_PREFS_KEY = "KEEP_OFFERWALL_OPEN";
	public static final String REFUSE_COOKIES_PREFS_KEY = "REFUSE_COOKIES_PREFS_KEY";
	public static final String SHOW_TOAST_VCS_REQUEST_PREFS_KEY = "SHOW_TOAST_VCS_REQUEST";
	public static final String ADAPTERS_INFO_LOCATION_PREFS_KEY = "ADAPTERS_INFO_LOCATION";
	public static final String ADAPTERS_CONFIG_LOCATION_PREFS_KEY = "ADAPTERS_CONFIG_LOCATION";
	
	private Button mBackButton;
	
	private CheckBox mKeepOfferwallOpenCheckBox;
	private CheckBox mRefuseCookiesCheckBox;
	private CheckBox mShowToastOnVCSRequestCheckBox;

	private CheckBox mSimulateNoPhoneStatePermissionCheckBox;
	private CheckBox mSimulateNoWifiStatePermissionCheckBox;
	private CheckBox mSimulateInvalidAndroidIdCheckBox;
	private CheckBox mSimulateNoSerialNumberCheckBox;
	private CheckBox mSimulateNoNetworkAccessCheckBox;

	private TextView mKeyValuesList;

	private EditText mOverridingUrlField;
	private EditText mCustomKeyField, mCustomValueField;
	
	private boolean mShouldStayOpen; 
	private boolean mShowToastOnSuccessfullVCSRequest; 
	
	private String mOverridingUrl;

	private String mPreferencesFileName;
	private EditText mAdaptersInfoLocation;
	private EditText mAdaptersConfigLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_settings);
		
		mPreferencesFileName = getIntent().getStringExtra(PREFERENCES_EXTRA);
		
		bindViews();
		
		updateCustomParametersList();
	}

	protected void bindViews() {
		mKeepOfferwallOpenCheckBox = (CheckBox) findViewById(R.id.keep_offerwall_open_checkbox);
		mRefuseCookiesCheckBox = (CheckBox) findViewById(R.id.refuse_cookies);
		mShowToastOnVCSRequestCheckBox = (CheckBox) findViewById(R.id.show_toast_on_vcs_successfull_checkbox);

		mSimulateNoPhoneStatePermissionCheckBox = (CheckBox) findViewById(R.id.simulate_no_phone_state_permission);
		mSimulateNoWifiStatePermissionCheckBox = (CheckBox) findViewById(R.id.simulate_no_wifi_state_permission);
		mSimulateInvalidAndroidIdCheckBox = (CheckBox) findViewById(R.id.simulate_invalid_android_id);
		mSimulateNoSerialNumberCheckBox = (CheckBox) findViewById(R.id.simulate_no_hw_serial_number);
		mSimulateNoNetworkAccessCheckBox = (CheckBox) findViewById(R.id.simulate_no_network_state_permission);
		
		mCustomKeyField = (EditText) findViewById(R.id.custom_key_field);
		mCustomValueField = (EditText) findViewById(R.id.custom_value_field);
		mOverridingUrlField = (EditText) findViewById(R.id.overriding_url_field);
		
		mAdaptersInfoLocation = (EditText) findViewById(R.id.adapters_info_url_field);
		mAdaptersConfigLocation = (EditText) findViewById(R.id.adapters_config_url_field);

		mKeyValuesList = (TextView) findViewById(R.id.key_values_list);
		
		mBackButton = (Button) findViewById(R.id.back_button);
		
		((TextView)findViewById(R.id.wifi_ip)).setText("Telnet endpoint: " + getIpAddr() + ":8088");
		
		setListenersInViews();
	}
	
	protected void setListenersInViews() {
		mCustomKeyField.setKeyListener(new KeyListener() {
			@Override
			public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					mCustomValueField.requestFocus();
					return true;
				}
				return false;
			}

			@Override
			public boolean onKeyOther(View view, Editable text, KeyEvent event) {
				return false;
			}

			@Override
			public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event) {
				return keyCode == KeyEvent.KEYCODE_ENTER;
			}

			@Override
			public int getInputType() {
				return InputType.TYPE_CLASS_TEXT;
			}

			@Override
			public void clearMetaKeyState(View view, Editable content, int states) {
			}
		});

		mCustomValueField.setKeyListener(new KeyListener() {
			@Override
			public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					MainSettingsActivity.this.onAddCustomParameterClick(null);
					mCustomKeyField.requestFocus();
					return true;
				}
				return false;
			}

			@Override
			public boolean onKeyOther(View view, Editable text, KeyEvent event) {
				return false;
			}

			@Override
			public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event) {
				return keyCode == KeyEvent.KEYCODE_ENTER;
			}

			@Override
			public int getInputType() {
				return InputType.TYPE_CLASS_TEXT;
			}

			@Override
			public void clearMetaKeyState(View view, Editable content, int states) {
			}
		});

		OnCheckedChangeListener simCheckboxesChangeListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (buttonView == mSimulateNoPhoneStatePermissionCheckBox) {
					HostInfo.setSimulateNoReadPhoneStatePermission(isChecked);
				} else if (buttonView == mSimulateNoNetworkAccessCheckBox) {
					HostInfo.setSimulateNoAccessNetworkState(isChecked);
				}
			}
		};
		
		OnCheckedChangeListener refuseCookiesCheckboxesChangeListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				mRefuseCookies = isChecked;
				
				try {
					CookieSyncManager.getInstance();
				} catch (IllegalStateException e) {
					CookieSyncManager.createInstance(MainSettingsActivity.this);
				}

				CookieManager cookieManager = CookieManager.getInstance();
				if (mRefuseCookies) {
					cookieManager.removeAllCookie();
					cookieManager.setAcceptCookie(false);
					SponsorPayLogger.d(TAG, "Cookies DISABLED for this session.");
				} else {
					SponsorPayLogger.d(TAG, "Cookies enabled for this session.");
					cookieManager.setAcceptCookie(true);
				}
			}
		};
		

		mSimulateNoPhoneStatePermissionCheckBox.setOnCheckedChangeListener(simCheckboxesChangeListener);
		mSimulateNoWifiStatePermissionCheckBox.setOnCheckedChangeListener(simCheckboxesChangeListener);
		mSimulateInvalidAndroidIdCheckBox.setOnCheckedChangeListener(simCheckboxesChangeListener);
		mSimulateNoSerialNumberCheckBox.setOnCheckedChangeListener(simCheckboxesChangeListener);
		mSimulateNoNetworkAccessCheckBox.setOnCheckedChangeListener(simCheckboxesChangeListener);
		mRefuseCookiesCheckBox.setOnCheckedChangeListener(refuseCookiesCheckboxesChangeListener);
	
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	/**
	 * Invoked when the user clicks on the "Add" button on the custom key/values area.
	 * 
	 * @param v
	 */
	public void onAddCustomParameterClick(View v) {

		if (StringUtils.nullOrEmpty(mCustomKeyField.getText().toString())) {
			Toast.makeText(getApplicationContext(),
					"Key field must contain a valid value", Toast.LENGTH_SHORT).show();
			return;
		}
		
		TestAppParametersProvider.INSTANCE.put(mCustomKeyField.getText().toString(), mCustomValueField
				.getText().toString());

		mCustomKeyField.setText(StringUtils.EMPTY_STRING);
		mCustomValueField.setText(StringUtils.EMPTY_STRING);

		updateCustomParametersList();
	}

	/**
	 * Invoked when the user clicks on the "Clear" button on the custom key/values area.
	 * 
	 * @param v
	 */
	public void onClearCustomParametersClick(View v) {
		TestAppParametersProvider.INSTANCE.clear();

		updateCustomParametersList();
	}
	
	/**
	 * Invoked when the user clicks on the "Clear url" button on the custom key/values area.
	 * 
	 * @param v
	 */
	public void onClearCustomUrlClick(View v) {
		mOverridingUrlField.setText("");
		TestAppUrlsProvider.INSTANCE.setOverridingUrl("");
	}
	
	public void onClearApplicationDataClick(View view) {
		SharedPreferences prefs = getSharedPreferences(SponsorPayPublisher.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
		Editor prefsEditor = prefs.edit();
		prefsEditor.clear();
		prefsEditor.commit();

		prefs = getSharedPreferences(SponsorPayAdvertiserState.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		prefsEditor = prefs.edit();
		prefsEditor.clear();
		prefsEditor.commit();
	}
 
	private void updateCustomParametersList() {
		String text = StringUtils.EMPTY_STRING;

		for (Entry<String, String> entry : TestAppParametersProvider.INSTANCE
				.getParameters().entrySet()) {
			text += String.format("%s = %s\n", entry.getKey(), entry.getValue());
		}

		mKeyValuesList.setText(text);
	}
	
	@Override
	protected void onDestroy() {
		Intent intent = new Intent();
		intent.putExtra(KEEP_OFFERWALL_OPEN_EXTRA, mShouldStayOpen);
		setIntent(intent);
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		// Save the state of the UI fields into the app preferences.
		fetchValuesFromFields();

		SharedPreferences prefs = getSharedPreferences(mPreferencesFileName, Context.MODE_PRIVATE);
		Editor prefsEditor = prefs.edit();
		
		prefsEditor.putString(OVERRIDING_URL_PREFS_KEY, mOverridingUrl);
		prefsEditor.putBoolean(KEEP_OFFERWALL_OPEN_PREFS_KEY, mShouldStayOpen);
		prefsEditor.putBoolean(SHOW_TOAST_VCS_REQUEST_PREFS_KEY, mShowToastOnSuccessfullVCSRequest);
		
		prefsEditor.putString(ADAPTERS_INFO_LOCATION_PREFS_KEY, mAdaptersInfoLocation.getText().toString());
		prefsEditor.putString(ADAPTERS_CONFIG_LOCATION_PREFS_KEY, mAdaptersConfigLocation.getText().toString());

		prefsEditor.commit();
		
		final JSONObject jo = new JSONObject(TestAppParametersProvider.INSTANCE.getParameters());
		prefsEditor.putString(OVERRIDING_URL_QUERY_PARAMS, jo.toString());
	
		prefsEditor.commit();

		super.onPause();
	}

	private void fetchValuesFromFields() {
		mShouldStayOpen = mKeepOfferwallOpenCheckBox.isChecked();
		
		mShowToastOnSuccessfullVCSRequest = mShowToastOnVCSRequestCheckBox.isChecked();
		
		mOverridingUrl = mOverridingUrlField.getText().toString();
		TestAppUrlsProvider.INSTANCE.setOverridingUrl(mOverridingUrl);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// Recover the state of the UI fields from the app preferences.
		SharedPreferences prefs = getSharedPreferences(mPreferencesFileName, Context.MODE_PRIVATE);

		mOverridingUrl = prefs.getString(OVERRIDING_URL_PREFS_KEY, StringUtils.EMPTY_STRING);
		
		mShouldStayOpen = prefs.getBoolean(KEEP_OFFERWALL_OPEN_PREFS_KEY, true);
		mShowToastOnSuccessfullVCSRequest = prefs.getBoolean(SHOW_TOAST_VCS_REQUEST_PREFS_KEY, true);

        TestAppParametersProvider.INSTANCE.setParameters(readParameters(prefs));
		
		mAdaptersConfigLocation.setText(prefs.getString(ADAPTERS_CONFIG_LOCATION_PREFS_KEY, ""));
		mAdaptersInfoLocation.setText(prefs.getString(ADAPTERS_INFO_LOCATION_PREFS_KEY, ""));
		
		setValuesInFields();
	}

	public static final Map<String,	String> readParameters(SharedPreferences prefs) {

		JSONObject jo = null;
		try {
			jo = new JSONObject(prefs.getString(OVERRIDING_URL_QUERY_PARAMS, "{}"));
		} catch (JSONException e) {
		}
		
		Map<String,	String> map = new HashMap<String, String>(jo.length());
		@SuppressWarnings("unchecked")
		Iterator<String> itr = jo.keys();
		String key, value;
		while (itr.hasNext()) {
			key = itr.next();
			try {
				value = jo.getString(key);
			} catch (JSONException e) {
				value = "";
			}
			map.put(key, value);
		}
		return map;
	}
	
	/**
	 * Sets values in the state of the UI text fields and text boxes.
	 */
	private void setValuesInFields() {
		mOverridingUrlField.setText(mOverridingUrl);
		updateCustomParametersList();
		
		TestAppUrlsProvider.INSTANCE.setOverridingUrl(mOverridingUrl);
		
		mKeepOfferwallOpenCheckBox.setChecked(mShouldStayOpen);
		mRefuseCookiesCheckBox.setChecked(mRefuseCookies);
		mShowToastOnVCSRequestCheckBox.setChecked(mShowToastOnSuccessfullVCSRequest);

		mSimulateNoPhoneStatePermissionCheckBox.setChecked(ExtendedHostInfo
				.getSimulateNoReadPhoneStatePermission());
		mSimulateNoNetworkAccessCheckBox.setChecked(ExtendedHostInfo
				.getSimulateNoAccessNetworkState());
	}

	//refactor this, copied from SponsorpayAndroidTestAppActivity
	public void showCancellableAlertBox(String title, String text) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(title).setMessage(text).setCancelable(true);
		dialogBuilder.show();
	}
	
	private String getIpAddr() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String ipString = String.format("%d.%d.%d.%d", (ip & 0xff),
				(ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

		return ipString;
	}
	
	private static class ExtendedHostInfo extends HostInfo {

		public ExtendedHostInfo(Context context) {
			super(context);
		}
		
		public static boolean getSimulateNoReadPhoneStatePermission(){
			return sSimulateNoReadPhoneStatePermission;
		}
		
		public static boolean getSimulateNoAccessNetworkState(){
			return sSimulateNoAccessNetworkState;
		}
	}
	
}
