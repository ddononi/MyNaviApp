package kr.co.navi;

import java.io.IOException;
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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NaviSearchActivity extends Activity implements iConstant {

	// element
	private EditText startPlaceEt;
	private EditText endPlaceEt;
	private Button searchBtn;
	
	// location
	private GeoPoint startPoint = null;
	private GeoPoint endPoint = null;	
	private String priorty = GeoRouteSearch.Params.PRIORITY_SHOTCUT;
	private final int START_LOC_SEARCH = 1;
	private final int END_LOC_SEARCH = 2;	
	@Override
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		setContentView(R.layout.search_layout);
		

		// �ٸ���Ʈ
		startPlaceEt = (EditText) findViewById(R.id.start_input); 	// ���� ��ġ
		endPlaceEt = (EditText) findViewById(R.id.end_input); 	// ���� ��ġ
		Button searchStartBtn = (Button) findViewById(R.id.search_start_btn); 		// ��ġ ��ư
		// ���� �ּҺ�ȯ �˻� ��ư
		searchStartBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(TextUtils.isEmpty(startPlaceEt.getText())){
					Toast.makeText(NaviSearchActivity.this, "���� ��ġ�� �Է��ϼ���", Toast.LENGTH_SHORT).show();
					return;
				}
				new GeocodeLoadTask(START_LOC_SEARCH).execute();
			}
		});
		
		Button searchEndBtn = (Button) findViewById(R.id.search_end_btn); 		// ��ġ ��ư
		// ���� �ּҺ�ȯ �˻� ��ư
		searchEndBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(TextUtils.isEmpty(endPlaceEt.getText())){
					Toast.makeText(NaviSearchActivity.this, "���� ��ġ�� �Է��ϼ���", Toast.LENGTH_SHORT).show();
					return;
				}
				new GeocodeLoadTask( END_LOC_SEARCH).execute();
			}
		});		
		
		
		searchBtn = (Button) findViewById(R.id.search_btn);		 // �˻� ��ư
		searchBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// ������ġ�� ������ġ ������ ������ ��� ó��
				if( startPoint == null || endPoint == null){
					setResult(RESULT_CANCELED);
				}else{				
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
			}
		});		
		
		Button priorityBtn = (Button) findViewById(R.id.priority_btn);		 // �˻� ��ư
		priorityBtn.setOnClickListener(new OnClickListener() {		
			public void onClick(View v) {
				AlertDialog.Builder ad = new AlertDialog.Builder(NaviSearchActivity.this);
				ad.setTitle("").setSingleChoiceItems(R.array.pirority_label, -1, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String[] arr = getResources().getStringArray(R.array.pirority_value);
						priorty = arr[which];			//�켱 ���� ����
						dialog.dismiss();
					}
				}).setNegativeButton("���",null).show();
			}
		});

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
