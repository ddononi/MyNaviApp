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
		

		// 앨리먼트
		startPlaceEt = (EditText) findViewById(R.id.start_input); 	// 시작 위치
		endPlaceEt = (EditText) findViewById(R.id.end_input); 	// 도착 위치
		Button searchStartBtn = (Button) findViewById(R.id.search_start_btn); 		// 위치 버튼
		// 시작 주소변환 검색 버튼
		searchStartBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(TextUtils.isEmpty(startPlaceEt.getText())){
					Toast.makeText(NaviSearchActivity.this, "시작 위치를 입력하세요", Toast.LENGTH_SHORT).show();
					return;
				}
				new GeocodeLoadTask(START_LOC_SEARCH).execute();
			}
		});
		
		Button searchEndBtn = (Button) findViewById(R.id.search_end_btn); 		// 위치 버튼
		// 시작 주소변환 검색 버튼
		searchEndBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(TextUtils.isEmpty(endPlaceEt.getText())){
					Toast.makeText(NaviSearchActivity.this, "도착 위치를 입력하세요", Toast.LENGTH_SHORT).show();
					return;
				}
				new GeocodeLoadTask( END_LOC_SEARCH).execute();
			}
		});		
		
		
		searchBtn = (Button) findViewById(R.id.search_btn);		 // 검색 버튼
		searchBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 시작위치나 도착위치 정보가 없으면 취소 처리
				if( startPoint == null || endPoint == null){
					setResult(RESULT_CANCELED);
				}else{				
					Intent intent = new Intent();
					intent.putExtra("startPlace", startPlaceEt.getText().toString());	// 출발 위치명
					intent.putExtra("endPlace", endPlaceEt.getText().toString());		// 도착 위치명
					
					intent.putExtra("startLat", startPoint.getLatitudeE6() );				// 출발 위도
					intent.putExtra("startLng", startPoint.getLongitudeE6() );			// 출발 경도
					intent.putExtra("endLat", endPoint.getLatitudeE6() );				// 도착 위도
					intent.putExtra("endLng", endPoint.getLongitudeE6() );			// 도착 경도
					
					intent.putExtra("priority", priorty);										// 경로탐색방법					
					setResult(RESULT_OK, intent);	// 정상 검색 완료 처리
					finish();
				}
			}
		});		
		
		Button priorityBtn = (Button) findViewById(R.id.priority_btn);		 // 검색 버튼
		priorityBtn.setOnClickListener(new OnClickListener() {		
			public void onClick(View v) {
				AlertDialog.Builder ad = new AlertDialog.Builder(NaviSearchActivity.this);
				ad.setTitle("").setSingleChoiceItems(R.array.pirority_label, -1, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String[] arr = getResources().getStringArray(R.array.pirority_value);
						priorty = arr[which];			//우선 순위 설정
						dialog.dismiss();
					}
				}).setNegativeButton("취소",null).show();
			}
		});

	}
	
	
	/**
	 * 주소-> 좌표 변환 처리 쓰레드 
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
				if( parseGeoCoderJson(jsonBody)){	// 좌표변환이 정상이면

				}
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = ProgressDialog.show(NaviSearchActivity.this, "주소변환",
					"잠시만 기다려 주세요\n  주소변환중입니다.");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			String address;
			if(mode == START_LOC_SEARCH){	// 출발 플래그이면 출발 장소검색 아니면 도착 장소 검색
				address = startPlaceEt.getText().toString();
			}else{
				address = endPlaceEt.getText().toString();
			}			
	        vars.add(new BasicNameValuePair("address", address));
	        // url get  파라미터 인코딩후 url 생성
            String url = GEOCODE_URL + URLEncodedUtils.format(vars, "UTF-8");
			// HTTP get 메서드를 이용하여 데이터 업로드 처리            
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
				JSONObject rootJson = (new JSONObject(jsonText));	// root json 얻기
				String status = rootJson.getString("status");		// 요청 결과 값
				if(status.equals(STATUS_OK) == false){
					Log.i("navi", "geocoder fail");
					return false;
				}
				JSONArray results = rootJson.getJSONArray("results");		// 결과 내용
				JSONObject result = results.getJSONObject(0);
				String  address = result.getString("formatted_address");		// 세부 주소 얻기
			
				JSONObject geometry = result.getJSONObject("geometry");	// 좌표 내용
				JSONObject location = geometry.getJSONObject("location");	// 좌표 내용
				
				// 좌표 얻기
				int lat = (int)(location.getDouble("lat") * 1E6);
				int lng = (int)(location.getDouble("lng") * 1E6);
				if(mode  ==  START_LOC_SEARCH){	// 출발 장소이면
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
