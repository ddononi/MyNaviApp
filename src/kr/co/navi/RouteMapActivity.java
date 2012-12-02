package kr.co.navi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.co.navi.data.VerTexData;
import kr.co.navi.overlay.CustomItemizedOverlay;
import kr.co.navi.overlay.CustomItemizedOverlay.OnOverlayItemClickListener;
import kr.co.navi.overlay.CustomOverlayItem;
import kr.co.navi.utils.GPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * 
 */
public class RouteMapActivity extends MapActivity implements iConstant {
	private MapView mMapView;
	private Drawable drawable;
	private CustomItemizedOverlay<CustomOverlayItem> itemizedOverlay;
	private List<Overlay> mapOverlays; // �������� ������ ����Ʈ
	private Projection projection;
	private VerTexData verTexData;
	
	private LinearLayout infoBox;			// ��� ���� �ڽ�
	private TextView startPlaceTv;			// ��� ��ġ
	private TextView endPlaceTv;			// ���� ��ġ	
	private TextView totalDistanceTv;		// �� �Ÿ�
	private TextView totalTimeTv;			// ���� �ð�	
	private TextView arriveTimeTv;			// ��������ð�
	private TextView chargeTv;				// �ýÿ��
	
	// location
	private GeoPoint startPoint;
	private GeoPoint endPoint;

	private String startAddress;
	private String endAddress;
	private String totalDistance;
	private String totalTime;	

	private String priority;

	@Override
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		setContentView(R.layout.map_layout);
		getIntent();
		initLayout();
		
		/*
		try {
			parseBuildingInfoJson();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	/**
	 * �ʺ� �ʱ�ȭ �� ���̾ƿ� �ʱ�ȭ
	 */
	private void initLayout() {
		// �ʺ� ����
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.setBuiltInZoomControls(true); // �� ��Ʈ��
		mMapView.setTraffic(true);
		infoBox = (LinearLayout)findViewById(R.id.navi_info_box);
		// ��� �׺� ���� �ؽ�Ʈ�� ������Ʈ
		startPlaceTv = (TextView)findViewById(R.id.start_place);
		endPlaceTv = (TextView)findViewById(R.id.end_place);		
		totalTimeTv = (TextView)findViewById(R.id.total_time);				
		totalDistanceTv = (TextView)findViewById(R.id.distance);			
		arriveTimeTv = (TextView)findViewById(R.id.arrive_time);
		chargeTv = (TextView)findViewById(R.id.charge);		
		
		mapOverlays = mMapView.getOverlays();
		// first overlay
		drawable = getResources().getDrawable(R.drawable.marker);
		itemizedOverlay = new CustomItemizedOverlay<CustomOverlayItem>(
				drawable, mMapView);		
	}

	private void addPointOverlayItem() {
		CustomOverlayItem startPointOverlayItem = new CustomOverlayItem(
				startPoint, "������", startAddress, "http://www.tjeju.kr/Helper/UC/ImgLink/noimg/NoImage356.jpg");
		itemizedOverlay.addOverlay(startPointOverlayItem);

		CustomOverlayItem endPointOverlayItem = new CustomOverlayItem(
				endPoint, "�������", endAddress, "http://www.tjeju.kr/Helper/UC/ImgLink/noimg/NoImage356.jpg");
		itemizedOverlay.addOverlay(endPointOverlayItem);

		itemizedOverlay.setOnItemClickListener(new OnOverlayItemClickListener() {

			public void onClick(CustomOverlayItem item,
					CustomItemizedOverlay cio) {
				cio.hideBalloon();
			}

		});

		mapOverlays.add(itemizedOverlay);
		final MapController mc = mMapView.getController();
		mc.animateTo(startPoint); // ���������� ��ǥ �̵�
		mc.setZoom(16); // �� ��Ʈ�� �ʱⰪ
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			mMapView.setSatellite(false);
			break;
		case 1:
			mMapView.setSatellite(true);
			break;
		case 2:
			infoBox.setVisibility(View.GONE);
			Intent intent = new Intent(this, NaviSearchActivity.class);
			startActivityForResult(intent, 11);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "�Ϲ�����");
		menu.add(0, 1, 1, "��������");
		menu.add(0, 2, 2, "���Ž��");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * NaviSearchActivity���� �ּҺ�ȯ ��� ó���� �Ѿ�� ��� ó��
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// ���� �˻� ó����
		if (resultCode == RESULT_OK) {
			// ����� ��������
			startPoint = new GeoPoint(data.getIntExtra("startLat", -1),
					data.getIntExtra("startLng", DEFAULT_LAT));
			// ������ ��������
			endPoint = new GeoPoint(data.getIntExtra("endLat", -1),
					data.getIntExtra("endLng", DEFAULT_LNG));

			// ��� �ּҸ�
			startAddress = data.getStringExtra("startPlace");
			// ���� �ּҸ�
			endAddress = data.getStringExtra("endPlace");
			
			priority = data.getStringExtra("priority");
			new PathLoadTask().execute(); // ��� ã�� ����
		}
	}

	/**
	 * ��� Ž�� GeoRouteSearch �Ķ���� ������ ��� Ž�� ����
	 * 
	 * @return ����� json ���ڿ�
	 */
	private String searchNaviPath() {
		Log.i("naviApp", "start");
		// ��� �Ķ���� ��ü
		GeoRouteSearch.Params parmas = new GeoRouteSearch.Params();

		// �����
		parmas.SX = "" + startPoint.getLongitudeE6() / 1E6;
		parmas.SY = "" + startPoint.getLatitudeE6() / 1E6;
		// ������
		parmas.EX = "" + endPoint.getLongitudeE6() / 1E6;
		parmas.EY = "" + endPoint.getLatitudeE6() / 1E6;
		// �̵� Ÿ��
		parmas.RPTYPE = "0";
		// ��ǥ Ÿ��
		parmas.COORDTYPE = "0";
		// ��� Ÿ�� ����
		parmas.PRIORITY =priority;
		// ���� �ð�
		parmas.timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS")
				.format(new Date());
		// ��� ã�� ����
		GeoRouteSearch t = new GeoRouteSearch(parmas);
		String jsonText = t.execute();

		return jsonText; // ��� ó�� json ���䰪
	}

	/**
	 * draw�� �̿��Ͽ� ������ ��θ� �׷��ش�.
	 */
	class MyOverlay extends Overlay {

		public MyOverlay() {

		}

		public void draw(Canvas canvas, MapView mapv, boolean shadow) {
			super.draw(canvas, mapv, shadow);
			// ��� �� �Ӽ�
			Paint mPaint = new Paint();
			mPaint.setDither(true);
			mPaint.setColor(Color.RED);
			mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			mPaint.setStrokeJoin(Paint.Join.ROUND);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(6);

			ArrayList<GPoint> list = verTexData.getList();
			int latitude;
			int longitude;
			GPoint gPoint1, gPoint2;
			GeoPoint gP1, gP2;
			Point p1, p2;
			Path path = new Path();
			for (int i = 0; i < list.size() - 1; i++) {
				gPoint1 = list.get(i);
				latitude = (int) (gPoint1.y * 1E6);
				longitude = (int) (gPoint1.x * 1E6);
				gP1 = new GeoPoint(latitude, longitude);

				gPoint2 = list.get(i + 1);
				latitude = (int) (gPoint2.y * 1E6);
				longitude = (int) (gPoint2.x * 1E6);
				gP2 = new GeoPoint(latitude, longitude);

				p1 = new Point();
				p2 = new Point();

				// geopoint ��ǥ�� mapview projection ��ġ�� ��ȯ��Ų��.
				projection.toPixels(gP1, p1);
				projection.toPixels(gP2, p2);

				// path ����
				path.moveTo(p2.x, p2.y);
				path.lineTo(p1.x, p1.y);
			}
			// ĵ������ �׷��ش�.
			canvas.drawPath(path, mPaint);			
		}
	}
	
	/**
	 * asset�� ����� �б����� json�� �Ľ��Ͽ� 
	 * �б� ���� Ŀ���� �������� ���������� ����� �ش�.
	 * @throws IOException
	 */
	private void parseBuildingInfoJson() throws IOException {
		AssetManager am = getResources().getAssets();
		InputStream is = am.open("building_info.json");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String data;
		while( (data = br.readLine()) != null){
			sb.append(data);
		}
		String json = sb.toString();
		
		try {
			// json �̿� ���� ����

			JSONArray jsonArray = (new JSONObject(json)).getJSONArray("buildings");
			CustomOverlayItem overLay;
			GeoPoint point;
			for (int i = 0; i < jsonArray.length(); i++) { // ��� �迭
				JSONObject obj = jsonArray.getJSONObject(i);

					String name = obj.getString("name"); // y ��ǥ ���
					String info = obj.getString("info"); 		// x ��ǥ ���
					String img = obj.getString("img"); 		// x ��ǥ ���
					int lat = (int)(Double.parseDouble(obj.getString("lat")) * 1E6); 		// x ��ǥ ���
					int lng = (int)(Double.parseDouble(obj.getString("lng")) *1E6); 		// x ��ǥ ���
					point = new GeoPoint(lat, lng);
					overLay = new CustomOverlayItem(point, name, info, img);
					itemizedOverlay.addOverlay(overLay);
					mapOverlays.add(itemizedOverlay);
					itemizedOverlay.setOnItemClickListener(new OnOverlayItemClickListener() {
						public void onClick(CustomOverlayItem item,
								CustomItemizedOverlay cio) {
							cio.hideBalloon();
						}
					});
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean isTwoClickBack = false;
	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		/*
		 * back ��ư�̸� Ÿ�̸�(2��)�� �̿��Ͽ� �ٽ��ѹ� �ڷ� ���⸦
		 * ������ ���ø����̼��� ���� �ǵ����Ѵ�.
		 */
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (!isTwoClickBack) {
					Toast.makeText(this, "'�ڷ�' ��ư�� �ѹ� �� ������ ����˴ϴ�.",
							Toast.LENGTH_SHORT).show();
					CntTimer timer = new CntTimer(2000, 1);
					timer.start();
				} else {
					moveTaskToBack(true);
	                finish();
					return true;
				}

			}
		}
		return false;
	}

	// �ڷΰ��� ���Ḧ ���� Ÿ�̸�
	class CntTimer extends CountDownTimer {
		public CntTimer(final long millisInFuture, final long countDownInterval) {
			super(millisInFuture, countDownInterval);
			isTwoClickBack = true;
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			isTwoClickBack = false;
		}

		@Override
		public void onTick(final long millisUntilFinished) {
		}
	}	
	

	/**
	 * ��� �޾ƿ��� ������ ó�� Ŭ����
	 */
	private class PathLoadTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog progress;
		private String jsonText;

		@Override
		protected void onPostExecute(Boolean result) {
			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}

			if (result && jsonText != null) {	// ���� ����
				parseJson(jsonText.trim());
				mapOverlays = mMapView.getOverlays();
				mapOverlays.clear();	// ���� �������̴� ����
				projection = mMapView.getProjection();
				mapOverlays.add(new MyOverlay());
				
				addPointOverlayItem();
				
				// ��� ���� 
				startPlaceTv.setText("��� : " + startAddress.replace("���ѹα�", " "));
				endPlaceTv.setText("���� : " +endAddress.replace("���ѹα�", " "));
				// ���� �ð� ����
				String hour = "";	// 60�� �̻��ϰ�� �ð����� ��ȯó��
				long time = Math.round( Double.valueOf(totalTime) * 1.1) ;
				if( time >  60){	// 1�ð� �̻��̸� �ð����� ��ȯ
					hour = String.valueOf(time / 60);
					totalTime = hour +"�ð� " + String.valueOf(time % 60) +"��";
				}else{
					totalTime = time + "��";
				}
				totalTimeTv.setText("����ð� : �� " +totalTime) ;
				totalDistanceTv.setText("�Ÿ� : ��" 
							+ Math.round( Double.valueOf(totalDistance) / 1000) + "Km \n");
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MINUTE, (int)time);
				arriveTimeTv.setText("�����ð� : " + cal.get(Calendar.HOUR)+"�� " +cal.get(Calendar.MINUTE)+"�� ");						
				chargeTv.setText("��� : ��" + calcTaxiCharge(Integer.valueOf(totalDistance)));			
				infoBox.setVisibility(View.VISIBLE);	// �ּ����� �ڽ��� �����ش�.
			}
		}
		
		/**
		 * �ý� ��� ����ϱ�
		 * @param meter
		 * @return
		 */
		private String calcTaxiCharge(int meter){
			// �⺻��� �Ÿ� �����϶�
			if(meter - 2000 < 0){
				return  "2,400��";
			}
			// �⺻ �Ÿ� 2000m, 144m ���� 100�� 	+ �⺻���		
			int charge = ((meter - 2000) / 144 ) * 100 + 2400;
			charge *= 1.3;		// ��ȣ�ð��� �����Ͽ� �� 0.3������ �߰����ش�.
			return NumberFormat.getCurrencyInstance().format(charge) + "��";
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = ProgressDialog.show(RouteMapActivity.this, "��� Ž����",
					"��ø� ��ٷ� �ּ��� ��θ� Ž�����Դϴ�.");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			jsonText = searchNaviPath();
			return true;
		}

		/**
		 * json�� �Ľ��Ͽ� ��������������� �÷��ǿ� ��ǥ�� �����Ѵ�.
		 * 
		 * @param json
		 *            ���������� ���� json String
		 */
		private void parseJson(String json) {
			verTexData = new VerTexData();
			ArrayList<GPoint> list = new ArrayList<GPoint>();
			try {
				// json �̿� ���� ����
				json = json.replaceFirst("\"", "");
				json = json.substring(0, json.lastIndexOf("\"") + 1);

				JSONObject RESDATA = (new JSONObject(json))
						.getJSONObject("RESDATA");
				JSONObject SROUTE = RESDATA.getJSONObject("SROUTE");
				JSONObject ROUTE = SROUTE.getJSONObject("ROUTE");
				// ���� �ð�
				totalTime =  ROUTE.getString("total_time");
				// �Ÿ�
				totalDistance = ROUTE.getString("total_dist");
				
				/*
				ROUTE":
				{
				"total_time":"14.28",
				"total_dist":"2956",
				"rg_count":"8",				
				*/
				JSONObject LINKS = SROUTE.getJSONObject("LINKS");
				JSONArray links = LINKS.getJSONArray("link");

				for (int i = 0; i < links.length(); i++) { // ��� �迭
					JSONObject arr = links.getJSONObject(i);
					JSONArray vertex = arr.getJSONArray("vertex"); // ��ǥ ���ý� �迭
					for (int j = 0; j < vertex.length(); j++) {
						JSONObject obj = vertex.getJSONObject(j);

						String y = obj.getString("y"); // y ��ǥ ���
						String x = obj.getString("x"); // x ��ǥ ���
						// ��ǥ�� ����
						list.add(new GPoint(Double.valueOf(x), Double
								.valueOf(y)));
					}
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			verTexData.setList(list);
		}

	}
}
