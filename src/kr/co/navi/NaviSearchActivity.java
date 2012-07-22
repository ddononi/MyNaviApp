package kr.co.navi;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 *	�׺���̼� ��� �� ���� ��ġ ���� ��Ƽ��Ƽ
 */
public class NaviSearchActivity extends Activity implements iConstant, OnClickListener {

	// element
	private EditText startPlaceEt;				// ��� ��� �Է�â
	private EditText endPlaceEt;				// ���� ��� �Է�â
	private Button searchBtn;					// �˻� ��ư
	
	// location
	private GeoPoint startPoint = null;		//	��� ��ǥ
	private GeoPoint endPoint = null;		// ���� ��ǥ
	private String priorty = GeoRouteSearch.Params.PRIORITY_SHOTCUT;	// �켱 ����
	
	// ���or ���� �˻� ���а�
	private final int START_LOC_SEARCH = 1;
	private final int END_LOC_SEARCH = 2;	
	
	// map & geo
	private Location mLocation;
	private LocationManager locationManager;
	
	// ��ġ ������ ó��
	private final LocationListener loclistener = new LocationListener() {
		// ��ġ�� ����Ǹ�
		@Override
		public void onLocationChanged(final Location location) {
			// getLocation();
			// ��ġ ����
			mLocation = location;
		}

		@Override
		public void onProviderDisabled(final String provider) {
		}

		@Override
		public void onProviderEnabled(final String provider) {
		}

		@Override
		public void onStatusChanged(final String provider, final int status,
				final Bundle extras) {
		}
	};	
	
	@Override
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		setContentView(R.layout.search_layout);
		getLocation();
		// �ٸ���Ʈ
		startPlaceEt = (EditText) findViewById(R.id.start_input); 	// ���� ��ġ
		endPlaceEt = (EditText) findViewById(R.id.end_input); 	// ���� ��ġ
		Button searchStartBtn = (Button) findViewById(R.id.search_start_btn); 		// ��ġ ��ư
		Button searchEndBtn = (Button) findViewById(R.id.search_end_btn); 		// ��ġ ��ư
		
		Button locStartBtn = (Button) findViewById(R.id.loc_start_btn); 		// ��� ���� ��ġ ��ư
		Button locEndBtn = (Button) findViewById(R.id.loc_end_btn); 		// ��� ���� ��ġ ��ư

		searchBtn = (Button) findViewById(R.id.search_btn);					 // �˻� ��ư
		Button priorityBtn = (Button) findViewById(R.id.priority_btn);		 //	�켱 ���� ��ư		
		
		// ���� �ּҺ�ȯ �˻� ��ư
		searchStartBtn.setOnClickListener(this);		
		// ���� �ּҺ�ȯ �˻� ��ư
		searchEndBtn.setOnClickListener(this);		
		locEndBtn.setOnClickListener(this);
		locStartBtn.setOnClickListener(this);

		searchBtn.setOnClickListener(this);				
		priorityBtn.setOnClickListener(this);

	}
	

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.search_start_btn : 		// ������ġ �ּҰ����� ã��
			if(TextUtils.isEmpty(startPlaceEt.getText())){
				Toast.makeText(NaviSearchActivity.this, "���� ��ġ�� �Է��ϼ���", Toast.LENGTH_SHORT).show();
				return;
			}
			new GeocodeLoadTask(START_LOC_SEARCH).execute();
			break;
			
		case R.id.search_end_btn : 	// ������ġ �ּҰ����� ã��
			if(TextUtils.isEmpty(endPlaceEt.getText())){
				Toast.makeText(NaviSearchActivity.this, "���� ��ġ�� �Է��ϼ���", Toast.LENGTH_SHORT).show();
				return;
			}
			new GeocodeLoadTask( END_LOC_SEARCH).execute();
			break;
		
		case R.id.loc_start_btn : 		// ��� ���� ��ġ�� �ϱ�
			// ��ǥ ���
			if(mLocation != null){
				int lat = (int)(mLocation.getLatitude() * 1E6);
				int lng = (int)(mLocation.getLongitude() * 1E6);
				// ��ǥ�� �̿��Ͽ� �ּҷ� ��ȯ�Ѵ�.
				getAddressFromPoint(lat, lng, START_LOC_SEARCH);
			}else{
				Toast.makeText(NaviSearchActivity.this, "����  ��ġ�� ã���� �����ϴ�.", Toast.LENGTH_SHORT).show();					
			}		
			break;
			
		case R.id.loc_end_btn : 		// ���� ���� ��ġ�� �ϱ�
			// ��ǥ ���
			if(mLocation != null){
				int lat = (int)(mLocation.getLatitude() * 1E6);
				int lng = (int)(mLocation.getLongitude() * 1E6);
				// ��ǥ�� �̿��Ͽ� �ּҷ� ��ȯ�Ѵ�.
				getAddressFromPoint(lat, lng, END_LOC_SEARCH);
			}else{
				Toast.makeText(NaviSearchActivity.this, "����  ��ġ�� ã���� �����ϴ�.", Toast.LENGTH_SHORT).show();					
			}		
			break;			
			
			
		case R.id.search_btn : 	// �˻� ��ư
			// ������ġ�� ������ġ ������ ������ ��� ó��
			if( startPoint == null || endPoint == null){
				setResult(RESULT_CANCELED);
			}else{				
				// �ʿ�Ƽ��Ƽ�� ��� �� ���� ��ġ������ ���� ��θ� �׷��ټ� �ֵ��� �Ѵ�.
				Intent intent = new Intent();
				intent.putExtra("startPlace", startPlaceEt.getText().toString());	// ��� ��ġ��
				intent.putExtra("endPlace", endPlaceEt.getText().toString());		// ���� ��ġ��
				
				intent.putExtra("startLat", startPoint.getLatitudeE6() );				// ��� ����
				intent.putExtra("startLng", startPoint.getLongitudeE6() );			// ��� �浵
				intent.putExtra("endLat", endPoint.getLatitudeE6() );				// ���� ����
				intent.putExtra("endLng", endPoint.getLongitudeE6() );			// ���� �浵
				
				intent.putExtra("priority", priorty);										// ���Ž�����					
				setResult(RESULT_OK, intent);	// ���� �˻� �Ϸ� ó��
				finish();
			}
			break;
			
			
		case R.id.priority_btn :	// �켱 ����
			AlertDialog.Builder ad = new AlertDialog.Builder(NaviSearchActivity.this);
			ad.setTitle("").setSingleChoiceItems(R.array.pirority_label, -1, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String[] arr = getResources().getStringArray(R.array.pirority_value);
					priorty = arr[which];			//�켱 ���� ����
					dialog.dismiss();
				}
			}).setNegativeButton("���",null).show();			
		}
	}	
	

	/**
	 * ��ǥ�� �̿��Ͽ� �ּ������� ��´�.
	 * @param lat
	 * @param lng
	 * @param what
	 */
	private void getAddressFromPoint(int lat, int lng , int what) {
		Geocoder gc = new Geocoder(NaviSearchActivity.this,Locale.getDefault());
		List<Address> addresses = null;
		try {
			addresses = gc.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String addressStr = "����ġ";		// ����Ʈ �ּҸ�
		if(addresses != null && addresses.size()>0) {	// �ּҰ� ������
			// ù��° �ּ� �÷����� ������
			Address address = addresses.get(0);
			// ���ѹα����ڴ� �����ְ� ���� �ּҸ� �����´�. 
			addressStr = address.getAddressLine(0).replace("���ѹα�", "").trim();
		}			
		if(what == START_LOC_SEARCH){	// ��� �˻��̸�
			startPoint = new GeoPoint(lat, lng);
			startPlaceEt.setText(addressStr);
		}else{
			endPoint = new GeoPoint(lat, lng);
			endPlaceEt.setText(addressStr);	
		}
	}	
	
	/*
	 * ��ġ ���� �ʱ�ȭ
	 */
	private void getLocation() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// ������ ��ġ��ݰ����ڸ� �̿��Ͽ� �����ʸ� �����Ѵ�.
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);// ��Ȯ��
		criteria.setPowerRequirement(Criteria.POWER_HIGH); // ���� �Һ�
		criteria.setAltitudeRequired(false); // �� ��뿩��
		criteria.setBearingRequired(false); //
		criteria.setSpeedRequired(true); // �ӵ�
		criteria.setCostAllowed(true); // ���������

		String provider = locationManager.getBestProvider(criteria, true);
		// 1�� �̻� 10���� �̻� 
		locationManager.requestLocationUpdates(provider, 0, 0, loclistener);
		mLocation = locationManager.getLastKnownLocation(provider);
	}	
	

	/**
	 * ��Ƽ��Ƽ�� stop�� ��ġ ���� ����
	 */
	@Override
	protected void onStop() {
		super.onStop();
		locationManager.removeUpdates(loclistener);
	}
	
	
	
	/**
	 * �ּ�-> ��ǥ ��ȯ ó�� ������ 
	 */
	private class GeocodeLoadTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog progress;
		private String jsonBody;
		private int mode;
		public GeocodeLoadTask(int mode){
			this.mode = mode;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}

			if (result) {
				if( parseGeoCoderJson(jsonBody)){	// ��ǥ��ȯ�� �����̸�

				}
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = ProgressDialog.show(NaviSearchActivity.this, "�ּҺ�ȯ",
					"��ø� ��ٷ� �ּ���\n  �ּҺ�ȯ���Դϴ�.");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			String address;
			if(mode == START_LOC_SEARCH){	// ��� �÷����̸� ��� ��Ұ˻� �ƴϸ� ���� ��� �˻�
				address = startPlaceEt.getText().toString();
			}else{
				address = endPlaceEt.getText().toString();
			}			
	        vars.add(new BasicNameValuePair("address", address));
	        // url get  �Ķ���� ���ڵ��� url ����
            String url = GEOCODE_URL + URLEncodedUtils.format(vars, "UTF-8");
			// HTTP get �޼��带 �̿��Ͽ� ������ ���ε� ó��            
            HttpGet request = new HttpGet(url);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpClient client = new DefaultHttpClient();
			try {
				jsonBody = client.execute(request, responseHandler);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;				
			}	
    		 Log.i("navi", "response : " + jsonBody);
			return true;
		}
		
		private boolean parseGeoCoderJson(String jsonText){
			try {
				JSONObject rootJson = (new JSONObject(jsonText));	// root json ���
				String status = rootJson.getString("status");		// ��û ��� ��
				if(status.equals(STATUS_OK) == false){
					Log.i("navi", "geocoder fail");
					return false;
				}
				JSONArray results = rootJson.getJSONArray("results");		// ��� ����
				JSONObject result = results.getJSONObject(0);
				String  address = result.getString("formatted_address");		// ���� �ּ� ���
			
				JSONObject geometry = result.getJSONObject("geometry");	// ��ǥ ����
				JSONObject location = geometry.getJSONObject("location");	// ��ǥ ����
				
				// ��ǥ ���
				int lat = (int)(location.getDouble("lat") * 1E6);
				int lng = (int)(location.getDouble("lng") * 1E6);
				if(mode  ==  START_LOC_SEARCH){	// ��� ����̸�
					startPoint = new GeoPoint(lat, lng);
					startPlaceEt.setText(address);
				}else{
					endPoint = new GeoPoint(lat, lng);
					endPlaceEt.setText(address);
				}

				return true;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.i("navi", e.getMessage());
				e.printStackTrace();
				return false;
			}

		}		

	}
	
}
