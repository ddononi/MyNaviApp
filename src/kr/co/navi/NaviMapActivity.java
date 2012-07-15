package kr.co.navi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * 
 */
public class NaviMapActivity extends MapActivity implements iConstant {
	private MapView mMapView;
	private Drawable drawable;
	private CustomItemizedOverlay<CustomOverlayItem> itemizedOverlay;
	private List<Overlay> mapOverlays; // 오버레이 아이템 리스트
	private Projection projection;
	private VerTexData verTexData;

	// location
	private GeoPoint startPoint;
	private GeoPoint endPoint;

	private String startAddress;
	private String endAddress;

	private String priority;
	@Override
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		setContentView(R.layout.map_layout);
		getIntent();
		initLayout();
	}

	/**
	 * 맵뷰 초기화 및 레이아웃 초기화
	 */
	private void initLayout() {
		// 맵뷰 설정
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.setBuiltInZoomControls(true); // 줌 컨트롤
		mMapView.setTraffic(true);

	}

	private void addPointOverlayItem() {
		mapOverlays = mMapView.getOverlays();
		// first overlay
		drawable = getResources().getDrawable(R.drawable.marker);
		itemizedOverlay = new CustomItemizedOverlay<CustomOverlayItem>(
				drawable, mMapView);
		CustomOverlayItem startPointOverlayItem = new CustomOverlayItem(
				startPoint, "출발장소", startAddress,
				"http://www.nemopan.com/files/attach/images/1122470/210/313/006/%EB%AF%B8%EC%8A%A4%EC%BD%94%EB%A6%AC%EC%95%84-%EC%9D%B4%EC%A0%95%EB%B9%88.jpg");
		itemizedOverlay.addOverlay(startPointOverlayItem);

		CustomOverlayItem endPointOverlayItem = new CustomOverlayItem(
				endPoint, "도착장소", endAddress,
				"http://www.nemopan.com/files/attach/images/1122470/210/313/006/%EB%AF%B8%EC%8A%A4%EC%BD%94%EB%A6%AC%EC%95%84-%EC%9D%B4%EC%A0%95%EB%B9%88.jpg");
		itemizedOverlay.addOverlay(endPointOverlayItem);

		itemizedOverlay
				.setOnItemClickListener(new OnOverlayItemClickListener() {
					public void onClick(CustomOverlayItem item) {
						// 오버레이 클리처리
					}
				});

		mapOverlays.add(itemizedOverlay);
		final MapController mc = mMapView.getController();
		mc.animateTo(startPoint); // 시작점으로 좌표 이동
		mc.setZoom(16); // 줌 컨트롤 초기값
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
			Intent intent = new Intent(this, NaviSearchActivity.class);
			startActivityForResult(intent, 11);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "일반지도");
		menu.add(0, 1, 1, "위성지도");
		menu.add(0, 2, 2, "경로탐색");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * NaviSearchActivity에서 주소변환 결과 처리후 넘어온 결과 처리
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 정상 검색 처리면
		if (resultCode == RESULT_OK) {
			// 출발점 가져오기
			startPoint = new GeoPoint(data.getIntExtra("startLat", -1),
					data.getIntExtra("startLng", DEFAULT_LAT));
			// 도착점 가져오기
			endPoint = new GeoPoint(data.getIntExtra("endLat", -1),
					data.getIntExtra("endLng", DEFAULT_LNG));

			// 출발 주소명
			startAddress = data.getStringExtra("startPlace");
			// 도착 주소명
			endAddress = data.getStringExtra("endPlace");
			
			priority = data.getStringExtra("priority");
			new PathLoadTask().execute(); // 경로 찾기 실행
		}
	}

	/**
	 * 경로 탐색 GeoRouteSearch 파라미터 설정후 경로 탐색 시작
	 * 
	 * @return 응답된 json 문자열
	 */
	private String searchNaviPath() {
		Log.i("naviApp", "start");
		// 경로 파라미터 객체
		GeoRouteSearch.Params parmas = new GeoRouteSearch.Params();

		// 출발점
		parmas.SX = "" + startPoint.getLongitudeE6() / 1E6;
		parmas.SY = "" + startPoint.getLatitudeE6() / 1E6;
		// 도착점
		parmas.EX = "" + endPoint.getLongitudeE6() / 1E6;
		parmas.EY = "" + endPoint.getLatitudeE6() / 1E6;
		// 이동 타입
		parmas.RPTYPE = "0";
		// 좌표 타입
		parmas.COORDTYPE = "0";
		// 경로 타입 설정
		parmas.PRIORITY =priority;
		// 현재 시각
		parmas.timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS")
				.format(new Date());
		// 경로 찾기 수행
		GeoRouteSearch t = new GeoRouteSearch(parmas);
		String jsonText = t.execute();

		return jsonText; // 경로 처리 json 응답값
	}

	/**
	 * draw를 이용하여 지도에 경로를 그려준다.
	 */
	class MyOverlay extends Overlay {

		public MyOverlay() {

		}

		public void draw(Canvas canvas, MapView mapv, boolean shadow) {
			super.draw(canvas, mapv, shadow);
			// 경로 선 속성
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

				// geopoint 좌표를 mapview projection 위치로 변환시킨다.
				projection.toPixels(gP1, p1);
				projection.toPixels(gP2, p2);

				// path 설정
				path.moveTo(p2.x, p2.y);
				path.lineTo(p1.x, p1.y);
			}
			// 캔버스에 그려준다.
			canvas.drawPath(path, mPaint);			
		}
	}

	/**
	 * 경로 받아오기 쓰레드 처리 클래스
	 */
	private class PathLoadTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog progress;
		private String jsonText;

		@Override
		protected void onPostExecute(Boolean result) {
			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}

			if (result) {
				parseJson(jsonText.trim());
				mapOverlays = mMapView.getOverlays();
				projection = mMapView.getProjection();
				mapOverlays.add(new MyOverlay());
				
				addPointOverlayItem();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = ProgressDialog.show(NaviMapActivity.this, "경로 탐색중",
					"잠시만 기다려 주세요 경로를 탐색중입니다.");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			jsonText = searchNaviPath();
			return true;
		}

		/**
		 * json을 파싱하여 경로정보를가져와 컬랙션에 좌표를 저장한다.
		 * 
		 * @param json
		 *            원격지에서 받은 json String
		 */
		private void parseJson(String json) {
			verTexData = new VerTexData();
			ArrayList<GPoint> list = new ArrayList<GPoint>();
			try {
				// json 이외 문자 제거
				json = json.replaceFirst("\"", "");
				json = json.substring(0, json.lastIndexOf("\"") + 1);

				JSONObject RESDATA = (new JSONObject(json))
						.getJSONObject("RESDATA");
				JSONObject SROUTE = RESDATA.getJSONObject("SROUTE");
				JSONObject LINKS = SROUTE.getJSONObject("LINKS");
				JSONArray links = LINKS.getJSONArray("link");

				for (int i = 0; i < links.length(); i++) { // 경로 배열
					JSONObject arr = links.getJSONObject(i);
					JSONArray vertex = arr.getJSONArray("vertex"); // 좌표 버택스 배열
					for (int j = 0; j < vertex.length(); j++) {
						JSONObject obj = vertex.getJSONObject(j);

						String y = obj.getString("y"); // y 좌표 얻기
						String x = obj.getString("x"); // x 좌표 얻기
						// 좌표를 저장
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
