
package com.sponsorpay.sdk.android.testapp;

import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sponsorpay.SponsorPay;
import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.mediation.SPMediationConfigurationFiles;
import com.sponsorpay.mediation.SPMediationConfigurator;
import com.sponsorpay.publisher.SponsorPayPublisher;
import com.sponsorpay.publisher.SponsorPayPublisher.UIStringIdentifier;
import com.sponsorpay.publisher.currency.SPCurrencyServerErrorResponse;
import com.sponsorpay.publisher.currency.SPCurrencyServerListener;
import com.sponsorpay.publisher.currency.SPCurrencyServerSuccesfulResponse;
import com.sponsorpay.publisher.currency.SPVirtualCurrencyConnector;
import com.sponsorpay.sdk.android.testapp.fragments.ActionsSettingsFragment;
import com.sponsorpay.sdk.android.testapp.fragments.InterstitialFragment;
import com.sponsorpay.sdk.android.testapp.fragments.LauncherFragment;
import com.sponsorpay.sdk.android.testapp.fragments.MBESettingsFragment;
import com.sponsorpay.sdk.android.testapp.utils.MediationConfigsListAdapter;
import com.sponsorpay.sdk.android.testapp.utils.TelnetLogger;
import com.sponsorpay.sdk.android.testapp.utils.TestAppCountryParameterProvider;
import com.sponsorpay.sdk.android.testapp.utils.TestAppParametersProvider;
import com.sponsorpay.sdk.android.testapp.utils.TestAppUrlsProvider;
import com.sponsorpay.sdk.android.testapp.utils.TextViewLogger;
import com.sponsorpay.utils.SponsorPayBaseUrlProvider;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.SponsorPayParametersProvider;
import com.sponsorpay.utils.StringUtils;

/**
 * Example activity in order to show the usage of Sponsorpay Android SDK.
 */
public class SponsorpayAndroidTestAppActivity extends FragmentActivity {
	private static final String TAG = SponsorpayAndroidTestAppActivity.class.getSimpleName();

	/**
	 * Shared preferences file name. Stores the values entered into the UI
	 * fields.
	 */
	private static final String PREFERENCES_FILE_NAME = "SponsorPayTestAppState";

	private static final String APP_ID_PREFS_KEY = "APP_ID";
	private static final String USER_ID_PREFS_KEY = "USER_ID";
	private static final String SECURITY_TOKEN_PREFS_KEY = "SECURITY_TOKEN";
	private static final String CURRENCY_NAME_PREFS_KEY = "CURRENCY_NAME";
	private static final String USE_STAGING_URLS_PREFS_KEY = "USE_STAGING_URLS";
	private static final String USE_PLAIN_HTTPS_PREF_KEY = "USE_PLAIN_HTTP";

	private static final int MAIN_SETTINGS_ACTIVITY_CODE = 3962;

	private String mCurrencyName;

	private EditText mAppIdField;
	private EditText mUserIdField;
	private EditText mSecurityTokenField;
	private EditText mCurrencyNameField;
	private EditText mCountryCodeField;
	private TextView mCredentialsInfo;

	private CheckBox mUseStagingUrlsCheckBox;
	private CheckBox mUsePlainHttpCheckBox;

	private boolean mShouldStayOpen;
	private boolean mShowToastOnSuccessfullVCSRequest;

	private TelnetLogger mTelnetLogger;

	private Button btnUp;
	private Button btnDown;
	
	private ExecutorService singleThreadExecutor;
	
	private Runnable logScrollUp = new Runnable() {
	    @Override
	    public void run() {
	        if (btnUp.isPressed()) {
				TextViewLogger.INSTANCE.scrollUp();
	            btnUp.postDelayed(logScrollUp, 100);
	        }
	    }
	};
	
	private Runnable logScrollDown = new Runnable() {
	    @Override
	    public void run() {
	        if (btnDown.isPressed()) {
				TextViewLogger.INSTANCE.scrollDown();
	            btnDown.postDelayed(logScrollDown, 100);
	        }
	    }
	};

	/**
	 * Called when the activity is first created. See {@link Activity}.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SponsorPayLogger.enableLogging(true);
		SponsorPayBaseUrlProvider.setProviderOverride(TestAppUrlsProvider.INSTANCE);
		SponsorPayParametersProvider.addParametersProvider(TestAppParametersProvider.INSTANCE);		
		
		
		SponsorPayParametersProvider.addParametersProvider(TestAppCountryParameterProvider.INSTANCE);


		setContentView(R.layout.main);

		bindViews();
		setCustomErrorMessages();

		createLauncherFragment();
		
		setApplicationTitle();
		
		((TextView) findViewById(R.id.sdk_version_string))
				.setText("SponsorPay SDK v. "
						+ SponsorPay.RELEASE_VERSION_STRING);

		TextView loggerView = (TextView) findViewById(R.id.log_text_view);
		TextViewLogger.INSTANCE.setTextView(loggerView);
		SponsorPayLogger.addLoggerListener(TextViewLogger.INSTANCE);
		
		mTelnetLogger = new TelnetLogger();
		
		singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(mTelnetLogger);
		
		SponsorPayLogger.addLoggerListener(mTelnetLogger);

		btnUp = ((Button) findViewById(R.id.btnUp));
		btnUp.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					v.postDelayed(logScrollUp, 300);
		        }
				return false;
			}
		});

		btnDown = ((Button) findViewById(R.id.btnDown));
		btnDown.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					v.postDelayed(logScrollDown, 300);
		        }
				return false;
			}
		});

	}

	private void setApplicationTitle() {

		PackageInfo pInfo = null;
		ApplicationInfo aInfo = null;
		String title = getTitle().toString() + " (";
		Boolean addBeta = false;

		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			aInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			title += pInfo.versionCode;
			addBeta = aInfo.metaData.getBoolean("beta");
		} catch (NameNotFoundException e) {
			SponsorPayLogger.e(TAG, e.getMessage(), e);
		} catch (NullPointerException e) {
			// Meta data not found
		}
		
		Spannable titleSpan = new SpannableString(title + (addBeta ? " BETA" : "") + ")");

		titleSpan.setSpan(new ForegroundColorSpan(Color.GRAY), getTitle().length(), getTitle().length() + (addBeta ? 10 : 5), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		setTitle(titleSpan);
	}
	
	private void createLauncherFragment() {
		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.fragment_placeholder);
		if (fragment == null) {
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			LauncherFragment launcherFragment = new LauncherFragment();
			transaction.add(R.id.fragment_placeholder, launcherFragment);
			transaction.commit();
		}
	}

	protected void bindViews() {
		mAppIdField = (EditText) findViewById(R.id.app_id_field);
		mUserIdField = (EditText) findViewById(R.id.user_id_field);
		mSecurityTokenField = (EditText) findViewById(R.id.security_token_field);
		mCurrencyNameField = (EditText) findViewById(R.id.currency_name_field);
		mCountryCodeField = (EditText) findViewById(R.id.country_code_field);

		mUseStagingUrlsCheckBox = (CheckBox) findViewById(R.id.use_staging_urls_checkbox);
		mUsePlainHttpCheckBox = (CheckBox) findViewById(R.id.use_http_checkbox);		
		if (!TestAppUrlsProvider.INSTANCE.isStagingAvailble()) {
			mUseStagingUrlsCheckBox.setVisibility(View.GONE);
			mUsePlainHttpCheckBox.setVisibility(View.GONE);
		}

		mCredentialsInfo = (TextView) findViewById(R.id.credentials_info);
	}

	public void onSettingsButtonClick(View v) {
		fetchValuesFromFields();
		Intent intent = new Intent(getApplicationContext(),
				MainSettingsActivity.class);
		intent.putExtra(MainSettingsActivity.PREFERENCES_EXTRA,
				PREFERENCES_FILE_NAME);
		startActivityForResult(intent, MAIN_SETTINGS_ACTIVITY_CODE);
	}

	@Override
	protected void onDestroy() {
		SponsorPayLogger.removeLoggerListener(mTelnetLogger);
		shutdownAndAwaitTermination(singleThreadExecutor);
		super.onDestroy();
	}
	
	private void shutdownAndAwaitTermination(ExecutorService pool){
		pool.shutdown();
		try{
			if(!pool.awaitTermination(15, TimeUnit.SECONDS)){
				pool.shutdownNow();
			}
		} catch(InterruptedException ie){
			ie.printStackTrace();
			pool.shutdownNow();
		}
	}

	@Override
	protected void onPause() {
		// Save the state of the UI fields into the app preferences.
		fetchValuesFromFields();

		SharedPreferences prefs = getSharedPreferences(PREFERENCES_FILE_NAME,
				Context.MODE_PRIVATE);
		Editor prefsEditor = prefs.edit();

		try {
			SPCredentials credentials = SponsorPay.getCurrentCredentials();
			prefsEditor.putString(APP_ID_PREFS_KEY, credentials.getAppId());
			prefsEditor.putString(USER_ID_PREFS_KEY, credentials.getUserId());
			prefsEditor.putString(SECURITY_TOKEN_PREFS_KEY,
					credentials.getSecurityToken());
		} catch (RuntimeException e) {
			SponsorPayLogger.d(TAG, "There's no current credentials.");
		}

		prefsEditor.putString(CURRENCY_NAME_PREFS_KEY, mCurrencyName);
		prefsEditor.putBoolean(USE_STAGING_URLS_PREFS_KEY,
				mUseStagingUrlsCheckBox.isChecked());
		prefsEditor.putBoolean(USE_PLAIN_HTTPS_PREF_KEY, 
				mUsePlainHttpCheckBox.isChecked());

		prefsEditor.commit();

		super.onPause();
	}

	@Override
	protected void onResume() {

		super.onResume();
	
		//If the executor is shutdown
		if(singleThreadExecutor.isShutdown()){
			//instantiate a new one
			singleThreadExecutor = Executors.newSingleThreadExecutor();
			
			//if it has finished with all the tasks
			if(singleThreadExecutor.isTerminated()){
			
				//then instantiate a new telnet logger
				mTelnetLogger = new TelnetLogger();
				
				singleThreadExecutor.execute(mTelnetLogger);
				SponsorPayLogger.addLoggerListener(mTelnetLogger);
			}
		}
		
		// Recover the state of the UI fields from the app preferences.
		SharedPreferences prefs = getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);

		String overridingAppId = prefs.getString(APP_ID_PREFS_KEY, StringUtils.EMPTY_STRING);
		String userId = prefs.getString(USER_ID_PREFS_KEY, StringUtils.EMPTY_STRING);
		String securityToken = prefs.getString(SECURITY_TOKEN_PREFS_KEY, StringUtils.EMPTY_STRING);
		
		String mOverridingUrl = prefs.getString(MainSettingsActivity.OVERRIDING_URL_PREFS_KEY, StringUtils.EMPTY_STRING);
		
		mCurrencyName = prefs.getString(CURRENCY_NAME_PREFS_KEY, StringUtils.EMPTY_STRING);
		
		mShouldStayOpen = prefs.getBoolean(MainSettingsActivity.KEEP_OFFERWALL_OPEN_PREFS_KEY, true);
		mShowToastOnSuccessfullVCSRequest = prefs.getBoolean(MainSettingsActivity.SHOW_TOAST_VCS_REQUEST_PREFS_KEY, true);

		ImageView settingsButton = (ImageView ) findViewById(R.id.settings_button);
		
		updateVCSToastNotification();

		mUseStagingUrlsCheckBox.setChecked(prefs.getBoolean(USE_STAGING_URLS_PREFS_KEY, false));
		mUsePlainHttpCheckBox.setChecked(prefs.getBoolean(USE_PLAIN_HTTPS_PREF_KEY, false));

		if (mOverridingUrl.equals(StringUtils.EMPTY_STRING)) {
			settingsButton.setBackgroundResource(Color.TRANSPARENT);
		} else {
			settingsButton.setBackgroundColor(Color.parseColor("#FBC809"));
		}

		TestAppUrlsProvider.INSTANCE.setOverridingUrl(mOverridingUrl);
        TestAppParametersProvider.INSTANCE.setParameters(MainSettingsActivity.readParameters(prefs));
	
		TestAppUrlsProvider.INSTANCE.shouldUseStaging(mUseStagingUrlsCheckBox.isChecked());
		TestAppUrlsProvider.INSTANCE.shouldUsePlainHttp(mUsePlainHttpCheckBox.isChecked());
		
		String infoLocation = prefs.getString(MainSettingsActivity.ADAPTERS_INFO_LOCATION_PREFS_KEY, "");
		String configLocation = prefs.getString(MainSettingsActivity.ADAPTERS_CONFIG_LOCATION_PREFS_KEY, "");

		SPMediationConfigurationFiles.setAdaptersInfoLocation(infoLocation);
		SPMediationConfigurationFiles.setAdaptersConfigLocation(configLocation);

		try {
			SponsorPay.start(overridingAppId, userId, securityToken, this);
		} catch (RuntimeException e) {
			SponsorPayLogger.d(TAG, e.getLocalizedMessage());
		}

		setValuesInFields();

	}

	private void updateVCSToastNotification() {
		SponsorPayPublisher
				.displayNotificationForSuccessfullCoinRequest(mShowToastOnSuccessfullVCSRequest);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Is this response intended for a fragment?
		// from http://blog.tgrigsby.com/2012/04/18/android-fragment-frustration.aspx
        int fragmentindex = ( requestCode >> 16 );
        if ( fragmentindex != 0 ) {
            // Yes.  Pass it on...
            super.onActivityResult( requestCode, resultCode, data );
        } else if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case MAIN_SETTINGS_ACTIVITY_CODE:
				mShouldStayOpen = data.getBooleanExtra(
						MainSettingsActivity.KEEP_OFFERWALL_OPEN_EXTRA, true);
				mShowToastOnSuccessfullVCSRequest = data
						.getBooleanExtra(
								MainSettingsActivity.SHOW_TOAST_VCS_REQUEST_EXTRA,
								true);
				updateVCSToastNotification();
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Sets one custom UI message in the SDK to demonstrate the use of
	 * SponsorPayPublisher.setCustomUIStrings();
	 */
	private void setCustomErrorMessages() {
		EnumMap<UIStringIdentifier, Integer> customUIStrings = new EnumMap<UIStringIdentifier, Integer>(
				UIStringIdentifier.class);
		customUIStrings.put(UIStringIdentifier.ERROR_DIALOG_TITLE,
				R.string.custom_error_message);
		SponsorPayPublisher.setCustomUIStrings(customUIStrings,
				getApplicationContext());
	}

	/**
	 * Fetches user provided values from the state of the UI text fields and
	 * text boxes.
	 */
	public void fetchValuesFromFields() {

		mCurrencyName = mCurrencyNameField.getText().toString();

		TestAppUrlsProvider.INSTANCE.shouldUseStaging(mUseStagingUrlsCheckBox.isChecked());
		TestAppUrlsProvider.INSTANCE.shouldUsePlainHttp(mUsePlainHttpCheckBox.isChecked());
		TestAppCountryParameterProvider.INSTANCE.useCountry(mCountryCodeField.getText().toString());

	}

	/**
	 * Sets values in the state of the UI text fields and text boxes.
	 */
	private void setValuesInFields() {
		try {
			SPCredentials credentials = SponsorPay.getCurrentCredentials();
			mAppIdField.setText(credentials.getAppId());
			mUserIdField.setText(credentials.getUserId());
			mSecurityTokenField.setText(credentials.getSecurityToken());
		} catch (RuntimeException e) {
			SponsorPayLogger.d(TAG, "There's no current credentials.");
		}
		mCurrencyNameField.setText(mCurrencyName);
		setCredentialsInfo();
	}

	private void setCredentialsInfo() {
		try {
			mCredentialsInfo.setText(SponsorPay.getCurrentCredentials()
					.toString());
		} catch (RuntimeException e) {
			SponsorPayLogger
					.d(TAG, "There are no credentials yet, unable to send the callback.");
		}
	}

	/**
	 * Triggered when the user clicks on the Up btn to scroll the log view up
	 * 
	 * @param v
	 */
	public void onScrollUpClick(View v) {
		TextViewLogger.INSTANCE.scrollUp();
	}

	/**
	 * Triggered when the user clicks on the Down btn to scroll the log view
	 * down
	 * 
	 * @param v
	 */
	public void onScrollDownClick(View v) {
		TextViewLogger.INSTANCE.scrollDown();
	}

	/**
	 * Triggered when the user clicks on the Top btn to scroll the log view to the very top
	 * 
	 * @param v
	 */
	public void onScrollTopClick(View v) {
		TextViewLogger.INSTANCE.scrollTop();
	}

	/**
	 * Triggered when the user clicks on the Bottom btn to scroll the log view to the very bottom
	 * 
	 * @param v
	 */
	public void onScrollBottomClick(View v) {
		TextViewLogger.INSTANCE.scrollBottom();
	}

	/**
	 * Triggered when the user clicks on the "C" btn to clear the scroll view
	 * 
	 * @param v
	 */
	public void onClearLogClick(View v) {
		TextViewLogger.INSTANCE.clear();
	}

	/**
	 * Triggered when the user clicks on the create new credentials button.
	 * 
	 * @param v
	 */
	public void onCreateNewCredentialsClick(View v) {
		TextViewLogger.INSTANCE.reset();
		try {
			String overridingAppId = mAppIdField.getText().toString();
			String userId = mUserIdField.getText().toString();
			String securityToken = mSecurityTokenField.getText().toString();
			SponsorPay.start(overridingAppId, userId, securityToken, this);
			SponsorPayLogger.d(TAG, "Credentials updated");
		} catch (RuntimeException e) {
			showCancellableAlertBox("Exception from SDK", e.getMessage());
			SponsorPayLogger.e(TAG, "SponsorPay SDK Exception: ", e);
		}
		setCredentialsInfo();
	}

	/**
	 * Triggered when the user clicks on the launch offer wall button.
	 * 
	 * @param v
	 */
	public void onLaunchOfferwallClick(View v) {
		TextViewLogger.INSTANCE.reset();
		fetchValuesFromFields();
		try {
			startActivityForResult(
					SponsorPayPublisher.getIntentForOfferWallActivity(
							getApplicationContext(), mShouldStayOpen,
							mCurrencyName, null), 5689);
		} catch (RuntimeException ex) {
			showCancellableAlertBox("Exception from SDK", ex.getMessage());
			SponsorPayLogger.e(TAG, "SponsorPay SDK Exception: ", ex);
		}
	}

	/**
	 * Triggered when the user clicks on the send action button.
	 * 
	 */
	public void onSendActionClick(View v) {
		getCurrentFragment(ActionsSettingsFragment.class).sendActionCompleted();
	}

	/**
	 * Triggered when the user clicks on the Request New Coins button. Will send
	 * a request for delta of coins to the currency server and register a
	 * callback object to show the result in a dialog box. Uses the values
	 * entered for User ID, App ID and Security Token.
	 * 
	 * @param v
	 */
	public void onRequestNewCoinsClick(View v) {
		fetchValuesFromFields();

		try {
			SPCredentials credentials = SponsorPay.getCurrentCredentials();

			final String usedTransactionId = SPVirtualCurrencyConnector
					.fetchLatestTransactionId(getApplicationContext(),
							credentials.getCredentialsToken());

			SPCurrencyServerListener requestListener = new SPCurrencyServerListener() {

				@Override
				public void onSPCurrencyServerError(
						SPCurrencyServerErrorResponse response) {
					showCancellableAlertBox(
							"Response or Request Error",
							String.format("%s\n%s\n%s\n",
									response.getErrorType(),
									response.getErrorCode(),
									response.getErrorMessage()));
				}

				@Override
				public void onSPCurrencyDeltaReceived(
						SPCurrencyServerSuccesfulResponse response) {
					showCancellableAlertBox("Response From Currency Server",
							String.format("Delta of Coins: %s\n\n"
									+ "Used Latest Transaction ID: %s\n\n"
									+ "Returned Latest Transaction ID: %s\n\n",
									response.getDeltaOfCoins(),
									usedTransactionId,
									response.getLatestTransactionId()));
				}
			};
			SponsorPayPublisher.requestNewCoins(getApplicationContext(),
					requestListener, mCurrencyName);
		} catch (RuntimeException ex) {
			showCancellableAlertBox("Exception from SDK", ex.getMessage());
			SponsorPayLogger.e(TAG, "SponsorPay SDK Exception: ", ex);
		}
	}

	public void onKeepLogsThroughSessionClick(View v) {
		TextViewLogger.INSTANCE.setKeepLogsThroughSessions(((CompoundButton) v).isChecked());
	}

	// MBE

	public void onMockMediationSettingsClick(View v) {
		Intent intent = new Intent(getApplicationContext(),
				MediationConfigsActivity.class);
		startActivity(intent);
	}

	//Interstitial
	
	public void onRequestAdsClick(View v) {
		getCurrentFragment(InterstitialFragment.class).requestAds();
	}
	
	public void onShowAdClick(View v) {
		getCurrentFragment(InterstitialFragment.class).showAds();
	}
	

	public String getCurrencyName() {
		return mCurrencyName;
	}
	
	/**
	 * Shows an alert box with the provided title and message and a unique
	 * button to cancel it.
	 * 
	 * @param title
	 *            The title for the alert box.
	 * @param text
	 *            The text message to show inside the alert box.
	 */
	public void showCancellableAlertBox(String title, String text) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(title).setMessage(text).setCancelable(true);
		dialogBuilder.show();
	}

	public SharedPreferences getPrefsStore() {
		return getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
	}

	// FRAGMENTS stuff

	@SuppressWarnings("unchecked")
	private <T extends Fragment> T getCurrentFragment(Class<T> type) {
		fetchValuesFromFields();
		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.fragment_placeholder);
		if (fragment.getClass().isAssignableFrom(type)) {
			return (T) fragment;
		}
		return null;
	}

	protected void replaceFragment(Fragment newFragment) {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();

		// Replace whatever is in the fragment_container view with this
		// fragment, and add the transaction to the back stack

		// transaction.setCustomAnimations(android.R.anim.slide_in_left,
		// android.R.anim.slide_out_right);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

		newFragment.setRetainInstance(true);
		transaction.replace(R.id.fragment_placeholder, newFragment);

		transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();
	}

	public void onActionsClick(View view) {
		replaceFragment(new ActionsSettingsFragment());
	}

	public void onMBEClick(View view) {
		replaceFragment(new MBESettingsFragment());
	}
	
	public void onInterstitialClick(View view) {
		replaceFragment(new InterstitialFragment());
	}

	public void onVideoMockMediationClick(View view) {
		startConfigurationActivity(SPMediationConfigurator.getConfiguration(
				"MockMediatedNetwork", "video.class.name", String.class));
	}

	public void onInterstitialMockMediationClick(View view) {
		startConfigurationActivity(SPMediationConfigurator.getConfiguration(
				"MockMediatedNetwork", "interstitial.class.name", String.class));
	}
	
	private void startConfigurationActivity(String className) {
		try {
			@SuppressWarnings("unchecked")
			Class<Activity> forName = (Class<Activity>) Class
					.forName(className);
			Intent intent = new Intent(getApplicationContext(), forName);
			startActivity(intent);
		} catch (ClassNotFoundException e) {
			SponsorPayLogger.e(TAG, e.getMessage(), e);
		}
	}

	//
	public void notifyMediationAdaptersList(List<String> adapters) {
		MediationConfigsListAdapter.setAdapters(adapters);
		LauncherFragment launcherFragment = getCurrentFragment(LauncherFragment.class);
		if (launcherFragment != null) {
			launcherFragment.refreshLayout();
		}
	}

}
