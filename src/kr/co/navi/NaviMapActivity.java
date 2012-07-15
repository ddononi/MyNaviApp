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
	private List<Overlay> mapOverlays; // �������� ������ ����Ʈ
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
	 * �ʺ� �ʱ�ȭ �� ���̾ƿ� �ʱ�ȭ
	 */
	private void initLayout() {
		// �ʺ� ����
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.setBuiltInZoomControls(true); // �� ��Ʈ��
		mMapView.setTraffic(true);

	}

	private void addPointOverlayItem() {
		mapOverlays = mMapView.getOverlays();
		// first overlay
		drawable = getResources().getDrawable(R.drawable.marker);
		itemizedOverlay = new CustomItemizedOverlay<CustomOverlayItem>(
				drawable, mMapView);
		CustomOverlayItem startPointOverlayItem = new CustomOverlayItem(
				startPoint, "������", startAddress,
				"http://www.nemopan.com/files/attach/images/1122470/210/313/006/%EB%AF%B8%EC%8A%A4%EC%BD%94%EB%A6%AC%EC%95%84-%EC%9D%B4%EC%A0%95%EB%B9%88.jpg");
		itemizedOverlay.addOverlay(startPointOverlayItem);

		CustomOverlayItem endPointOverlayItem = new CustomOverlayItem(
				endPoint, "�������", endAddress,
				"http://www.nemopan.com/files/attach/images/1122470/210/313/006/%EB%AF%B8%EC%8A%A4%EC%BD%94%EB%A6%AC%EC%95%84-%EC%9D%B4%EC%A0%95%EB%B9%88.jpg");
		itemizedOverlay.addOverlay(endPointOverlayItem);

		itemizedOverlay
				.setOnItemClickListener(new OnOverlayItemClickListener() {
					public void onClick(CustomOverlayItem item) {
						// �������� Ŭ��ó��
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
			progress = ProgressDialog.show(NaviMapActivity.this, "��� Ž����",
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
