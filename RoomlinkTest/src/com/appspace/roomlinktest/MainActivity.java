package com.appspace.roomlinktest;

//String tagname = "roomlinktest";
//String webURL = "file:///android_asset/www/firstpage.html";

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationClient;
import com.google.gson.Gson;
import com.microsoft.windowsazure.messaging.NotificationHub;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	protected LocationManager locationManager;
	protected LocationListener locationListener;

	private ValueCallback<Uri> mUploadMessage;
	private final static int FILECHOOSER_RESULTCODE = 1;
	private static int TAKE_PICTURE = 123456789;

	String tagname = "roomlinktest";
	// String webURL = "file:///android_asset/www/firstpage.html";
	String webURL = "http://r2013.cloudapp.net/getlocation";

	protected Context context;
	protected String lat;
	protected String provider;
	protected double latitude;
	protected double longitude;
	protected boolean gps_enabled, network_enabled;

	WebView webView;

	ProgressDialog progress;

	Uri picUri;

	private String SENDER_ID = "942136024399";
	private GoogleCloudMessaging gcm;
	private NotificationHub hub;

	private LocationClient mLocationClient;
	private Location mCurrentLocation;
	// Global constants
	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// locationManager = (LocationManager)
		// getSystemService(Context.LOCATION_SERVICE);
		latitude = 0.0;
		longitude = 0.0;

		if (isInternetConnected()) {
			makeWebView();
		} else {
			alertRequireInternet();
		}

		// setup GoogleCloudMessaging
		gcm = GoogleCloudMessaging.getInstance(this);

		// setup Azure Hub
		new HubConnectionTask().execute("");

		// LocationClient for google play service
		mLocationClient = new LocationClient(this, this, this);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			// Log.i( "dd","Extra:" + extras.getString("messageId") );
			Log.d(tagname, "Extra:" + extras.getString("messageIdId"));
		}

	}

	private void registerWithNotificationHubs() {
		Log.d(tagname, "registerWithNotificationHubs");
		new AsyncTask<Object, Object, Object>() {
			@Override
			protected Object doInBackground(Object... params) {
				// boolean isDone = false;
				try {
					String regid = gcm.register(SENDER_ID);
					Log.d(tagname, "regid=" + regid);
					hub.register(regid, tagname);
					Log.d(tagname, "hub.register(regid)");
				} catch (Exception e) {
					Log.d(tagname, "Exception=" + e.getMessage());
					e.printStackTrace(System.out);
					// this.execute(null, null, null);
					return e;
				}
				return null;
			}
		}.execute(null, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		if (webView.canGoBack())
			webView.goBack();
		else
			super.onBackPressed();
	}

	@JavascriptInterface
	public void getAQRReader() {
		try {
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			// "PRODUCT_MODE for bar codes

			startActivityForResult(intent, 0);
		} catch (Exception e) {

			Uri marketUri = Uri
					.parse("market://details?id=com.google.zxing.client.android");
			Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
			startActivity(marketIntent);
		}
	}

	private void alertRequireInternet() {
		AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
		// notify user internet state
		Log.d(tagname, "notify user internet state");

		if (!isInternetConnected()) {
			builder1.setTitle("No Internet Connection");
			builder1.setMessage("Plase connect to internet");
			builder1.setCancelable(false);
			builder1.setPositiveButton("Retry",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
							alertRequireInternet();
						}
					});
			AlertDialog alert11 = builder1.create();
			alert11.show();
		} else {
			makeWebView();
		}
	}

	private void makeWebView() {
		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setGeolocationEnabled(true);
		// webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.setBackgroundColor(Color.parseColor("#FFFFFF"));
		webView.setWebViewClient(new myWebClient());
		webView.addJavascriptInterface((this), "Android");
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String temp = extras.getString("messageIdId");
			webView.loadUrl(temp);
			Log.d(tagname, "loading Url from messageIdId");
		} else {
			webView.loadUrl(webURL);
			Log.d(tagname, "loading default Url");
		}
		webView.setWebChromeClient(new WebChromeClient() {
			public void onGeolocationPermissionsShowPrompt(String origin,
					android.webkit.GeolocationPermissions.Callback callback) {
				Log.d("geolocation permission", "permission >>>" + origin);
				callback.invoke(origin, true, false);
			}

			// For Android 4.1
			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg,
					String acceptType, String capture) {
				mUploadMessage = uploadMsg;
				if (capture.equals("camera")) {
					Intent cameraIntent = new Intent(
							"android.media.action.IMAGE_CAPTURE");
					Calendar cal = Calendar.getInstance();
					cal.getTime();
					SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
					File photo = new File(Environment
							.getExternalStorageDirectory(), sdf.format(cal
							.getTime()) + ".jpg");
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(photo));
					picUri = Uri.fromFile(photo);
					startActivityForResult(cameraIntent, TAKE_PICTURE);
				} else {
					Intent i = new Intent(Intent.ACTION_GET_CONTENT);
					i.addCategory(Intent.CATEGORY_OPENABLE);
					i.setType("image/*");
					MainActivity.this.startActivityForResult(
							Intent.createChooser(i, "File Chooser"),
							MainActivity.FILECHOOSER_RESULTCODE);
				}
			}
		});
	}

	// check network connectivity
	private boolean isInternetConnected() {
		Log.d(tagname, "check network connectivity");
		final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isAvailable()) {
			// notify user you are online
			switch (activeNetwork.getType()) {
			case ConnectivityManager.TYPE_WIFI:
				// tvNetworkState.setText("WIFI");
				break;
			case ConnectivityManager.TYPE_MOBILE:
				// tvNetworkState.setText("Mobile");
				break;
			default:
				;
			}
			Log.d("active network type", "" + activeNetwork.getType());
			return true;
		}

		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == FILECHOOSER_RESULTCODE) {
			if (null == mUploadMessage)
				return;
			Uri result = intent == null || resultCode != RESULT_OK ? null
					: intent.getData();
			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;
		} else if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				webView.loadUrl("javascript:getAndroidQRReader(\"" + contents
						+ "\")");
			}
			if (resultCode == RESULT_CANCELED) {
				// handle cancel
			}
		} else if (requestCode == TAKE_PICTURE) {
			if (resultCode == RESULT_OK) {
				Log.d(tagname, "RESULT_OK");
				Uri mypic = picUri;
				mUploadMessage.onReceiveValue(mypic);
				mUploadMessage = null;
			} else {
				Log.d(tagname, "!RESULT_OK");
				mUploadMessage.onReceiveValue(null);
				mUploadMessage = null;
				return;
			}
		} else if (requestCode == CONNECTION_FAILURE_RESOLUTION_REQUEST) {
			if (requestCode == Activity.RESULT_OK) {
				return;
			}
		}
	}

	private class myWebClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith("tel:")) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
				return true;
			}
			view.loadUrl(url);
			return true;

		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.d(tagname, "urlnow=" + view.getUrl());
			Log.d(tagname, "getOriginalUrl=" + view.getOriginalUrl());
			super.onPageFinished(view, url);
		}
	}

	@JavascriptInterface
	public void setUserIdPreferences(String userId) {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = getPreferences(0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("userId", userId);

		// Commit the edits!
		editor.commit();
	}

	@JavascriptInterface
	public String getUserIdPreferences() {
		SharedPreferences settings = getPreferences(0);
		String userId = settings.getString("userId", "non");
		return userId;
	}

	@Override
	public void onLocationChanged(Location location) {
		progress.dismiss();

		latitude = location.getLatitude();
		longitude = location.getLongitude();

		Gson gson = new Gson();
		CustomLocation cLocation = new CustomLocation();
		cLocation.setAltitude(location.getAltitude());
		cLocation.setLatitude(location.getLatitude());
		cLocation.setLongitude(location.getLongitude());
		final String temp = gson.toJson(cLocation);
		Log.d("Location Updates", "temp=" + temp);

		// return current location to webview();
		MainActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				webView.loadUrl("javascript:getAndroidLocation('" + temp + "')");
			}
		});

		// stop getting location
		locationManager.removeUpdates(this);
		Log.d(tagname, "LocationChanged");
	}

	@JavascriptInterface
	public void getALocation() {
		progress = ProgressDialog.show(this, "Loading", "Getting Location",
				true);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager
				.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
						(LocationListener) this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, (LocationListener) this);

	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(tagname, "onProviderDisabled");
		progress.dismiss();
		// stop getting location
		locationManager.removeUpdates(this);
		AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
		builder1.setTitle("Need GPS Enable");
		builder1.setMessage("Do you want to enable GPS?");
		builder1.setCancelable(true);
		builder1.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(intent);
					}
				});
		builder1.setNegativeButton("No", null);
		AlertDialog alert11 = builder1.create();
		alert11.show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(tagname, "onProviderEnabled");

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(tagname, "onStatusChanged");

	}

	@SuppressWarnings("unused")
	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			Log.d("Location Updates", "Google Play services is not available.");

		}
		return false;
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// Display the connection status
		Toast.makeText(this, "onConnectionFailed. onConnectionFailed",
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onConnected(Bundle arg0) {
		// Display the connection status
		// Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		/*
		 * mCurrentLocation = mLocationClient.getLastLocation();
		 * webView.loadUrl("javascript:getAndroidLocationFromPlayService(\"" +
		 * mCurrentLocation.getLatitude() + "," +
		 * mCurrentLocation.getLongitude() + "\")"); Log.d("Location Updates",
		 * "mCurrentLocation.getLongitude()"+mCurrentLocation.getLongitude());
		 * Log.d("Location Updates",
		 * "mCurrentLocation.getLatitude()"+mCurrentLocation.getLatitude());
		 */
	}

	@Override
	public void onDisconnected() {
		// Display the connection status
		// Toast.makeText(this, "Disconnected. Please re-connect.",
		// Toast.LENGTH_SHORT).show();

	}

	/*
	 * Called when the Activity becomes visible.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		mLocationClient.connect();
	}

	/*
	 * Called when the Activity is no longer visible.
	 */
	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
		mLocationClient.disconnect();
		super.onStop();
	}

	@JavascriptInterface
	public void getLocationFromPlayService() {
		// mLocationClient.connect();
		// mCurrentLocation = mLocationClient.getLastLocation();
		// webView.loadUrl("javascript:getAndroidLocationFromPlayService(\"" +
		// mCurrentLocation.getLatitude() + ","
		// + mCurrentLocation.getLongitude() + "\")");
		// mLocationClient.disconnect();
		mCurrentLocation = mLocationClient.getLastLocation();
		// webView.loadUrl("javascript:getAndroidLocationFromPlayService(\"" +
		// mCurrentLocation.getLatitude() + ","
		// + mCurrentLocation.getLongitude() + "\")");

		Gson gson = new Gson();
		CustomLocation cLocation = new CustomLocation();
		cLocation.setAltitude(mCurrentLocation.getAltitude());
		cLocation.setLatitude(mCurrentLocation.getLatitude());
		cLocation.setLongitude(mCurrentLocation.getLongitude());
		final String temp = gson.toJson(cLocation);
		Log.d("Location Updates", "temp=" + temp);

		MainActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				webView.loadUrl("javascript:getAndroidLocationFromPlayService('"
						+ temp + "')");
			}
		});

		Log.d("Location Updates", "mCurrentLocation.getLongitude()"
				+ mCurrentLocation.getLongitude());
		Log.d("Location Updates", "mCurrentLocation.getLatitude()"
				+ mCurrentLocation.getLatitude());
		Log.d("Location Updates", "mCurrentLocation.getAltitude()"
				+ mCurrentLocation.getAltitude());
	}

	@JavascriptInterface
	public void showNoti(String title, String content) {
		Log.d("show noti", "show noti:" + title + "," + content);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title).setContentText(content);
		// Sets an ID for the notification
		int mNotificationId = 001;
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Builds the notification and issues it.
		mNotifyMgr.notify(mNotificationId, mBuilder.build());
	}

	class HubConnectionTask extends AsyncTask<String, Void, Void> {

		private HubConnectionData hubConnectionData;

		@Override
		protected Void doInBackground(String... arg0) {
			String json = null;
			try {
				BufferedReader reader = null;
				try {
					URL url = new URL(
							"http://r2013.cloudapp.net/resultconnect?vcheck=dbqbfPzjsbrkPdfFTMCPLSFkrzzKHw21");
					reader = new BufferedReader(new InputStreamReader(
							url.openStream()));
					StringBuffer buffer = new StringBuffer();
					int read;
					char[] chars = new char[1024];
					while ((read = reader.read(chars)) != -1)
						buffer.append(chars, 0, read);

					json = buffer.toString();
				} finally {
					if (reader != null)
						reader.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// auto mapping data from json to class
			hubConnectionData = new Gson().fromJson(json,
					HubConnectionData.class);
			// System.out.println(hubConnectionData.toString());
			// System.out.println(hubConnectionData.getDefaultListenSharedAccessSignature());
			return null;
		}

		protected void onPostExecute(Void result) {
			try {
				String connectionString = hubConnectionData
						.getDefaultListenSharedAccessSignature();
				hub = new NotificationHub(hubConnectionData.getHubName(),
						connectionString, MainActivity.this);
				System.out.println("Hub Connection Complete");
			} catch (NullPointerException e) {
				// execute("");
			}
			MainActivity.this.registerWithNotificationHubs();
		}

	}

	public void onClickGoBTN(View v) {

	}
}
