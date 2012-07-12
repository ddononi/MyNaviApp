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

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * @author ddononi
 * 
 */
public class NaviMapActivity extends MapActivity {
	private MapView mMapView;
	private Drawable drawable;
	private CustomItemizedOverlay<CustomOverlayItem> itemizedOverlay;
	private List<Overlay> mapOverlays;
	private Projection projection;
	private VerTexData verTexData;

	@Override
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		setContentView(R.layout.map);
		Intent intent = getIntent();

		initMap();

		test();
	}

	private void initMap() {
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.setBuiltInZoomControls(true);
		// mMapView.setSatellite(true);
		mMapView.setTraffic(true);
	}

	private void initOverLayItem() {
		ArrayList<GPoint> list = verTexData.getList();
		mapOverlays = mMapView.getOverlays();
		// first overlay
		drawable = getResources().getDrawable(R.drawable.marker);
		itemizedOverlay = new CustomItemizedOverlay<CustomOverlayItem>(
				drawable, mMapView);
		GPoint p = list.get(0);

		// ÃàÁ¦Àå¼Ò À§°æµµ ÁÂÇ¥ ¾ò±â
		int latitude = (int) (p.y * 1E6);
		int longitude = (int) (p.x * 1E6);
		GeoPoint point = new GeoPoint(latitude, longitude);
		CustomOverlayItem overlayItem = new CustomOverlayItem(
				point,
				"test",
				"tsetset",
				"http://www.nemopan.com/files/attach/images/1122470/210/313/006/%EB%AF%B8%EC%8A%A4%EC%BD%94%EB%A6%AC%EC%95%84-%EC%9D%B4%EC%A0%95%EB%B9%88.jpg");
		// ?¤ë²„?ˆì´ ?„ì´??ë¦¬ìŠ¤?¸ì— ?£ì–´ì¤?‹¤.
		itemizedOverlay.addOverlay(overlayItem);
		itemizedOverlay
				.setOnItemClickListener(new OnOverlayItemClickListener() {
					public void onClick(CustomOverlayItem item) {
						// Toast.makeText(KostivalMaps.this, item.getTitle(),
						// Toast.LENGTH_SHORT).show();
						finish();
					}
				});
		mapOverlays.add(itemizedOverlay);
		// ì»¨íŠ¸ë¡¤ëŸ¬ ?»ê¸°
		final MapController mc = mMapView.getController();
		mc.animateTo(point); // ì¶•ì œ?¥ì†Œ ì¢Œí‘œë¡??´ë™
		mc.setZoom(16); // ì§?„ ì¤??¤ì •
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
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "?¼ë°˜ì§?„");
		menu.add(0, 1, 1, "?„ì„±ì§?„");
		return super.onCreateOptionsMenu(menu);
	}

	private void test() {
		Log.i("naviApp", "start");
		GeoRouteSearch.Params parmas = new GeoRouteSearch.Params();

		// Å×½ºÆ®
		// 37.562362,127.156524
		// ÁÂÇ¥ º¯È¯ wgs -> tm
		GPoint pnt = new GPoint(Double.valueOf(127.156524),
				Double.valueOf(37.562362));
		// GPoint outPoint =
		// CoordinateTransformation.convert(CoordinateTransformation.WGS84,
		// CoordinateTransformation.TM, pnt);
		parmas.SX = "" + pnt.x;
		parmas.SY = "" + pnt.y;

		// ÁÂÇ¥ º¯È¯ wgs -> tm
		// 37.546372,127.143521
		pnt = new GPoint(Double.valueOf(127.143521), Double.valueOf(37.546372));
		// outPoint =
		// CoordinateTransformation.convert(CoordinateTransformation.WGS84,
		// CoordinateTransformation.TM, pnt);
		parmas.EX = "" + pnt.x;
		parmas.EY = "" + pnt.y;
		parmas.RPTYPE = "0";
		parmas.COORDTYPE = "0";
		parmas.PRIORITY = "0";
		parmas.timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS")
				.format(new Date());
		GeoRouteSearch t = new GeoRouteSearch(parmas);
		// logTv = (TextView)findViewById(R.id.log);
		// logTv.setText("payload : " + t.execute());
		String jsonText = t.execute();
		parseJson(jsonText.trim());
		initOverLayItem();
		
		 mapOverlays = mMapView.getOverlays();        
		    projection = mMapView.getProjection();
		    mapOverlays.add(new MyOverlay());    
		    
	}

	private void parseJson(String json) {
		verTexData = new VerTexData();
		ArrayList<GPoint> list = new ArrayList<GPoint>();
		try {
			json = json.replaceFirst("\"", "");
			json = json.substring(0, json.lastIndexOf("\"") + 1);
			Log.i("navi", "json : " + json);
			JSONObject RESDATA = (new JSONObject(json))
					.getJSONObject("RESDATA");
			JSONObject SROUTE = RESDATA.getJSONObject("SROUTE");
			JSONObject LINKS = SROUTE.getJSONObject("LINKS");
			JSONArray links = LINKS.getJSONArray("link");

			for (int i = 0; i < links.length(); i++) {
				JSONObject arr = links.getJSONObject(i);
				JSONArray vertex = arr.getJSONArray("vertex");
				for (int j = 0; j < vertex.length(); j++) {
					JSONObject obj = vertex.getJSONObject(j);

					String y = obj.getString("y");
					String x = obj.getString("x");

					list.add(new GPoint(Double.valueOf(x), Double.valueOf(y)));
					// GPoint pnt = new GPoint(Double.valueOf(y),
					// Double.valueOf(x));
					// GPoint outPoint =
					// CoordinateTransformation.convert(CoordinateTransformation.TM,
					// CoordinateTransformation.WGS84, pnt);

					Log.i("navi", "y : " + y);
					Log.i("navi", "x : " + x);
					// logTv.setText(logTv.getText() + "y : " + y +"   x :   " +
					// x + "\n");
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		verTexData.setList(list);
	}

	class MyOverlay extends Overlay {

		public MyOverlay() {

		}

		public void draw(Canvas canvas, MapView mapv, boolean shadow) {
			super.draw(canvas, mapv, shadow);

			Paint mPaint = new Paint();
			mPaint.setDither(true);
			mPaint.setColor(Color.RED);
			mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			mPaint.setStrokeJoin(Paint.Join.ROUND);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(2);
			ArrayList<GPoint> list = verTexData.getList();
			for(int i=0; i <list.size()-1; i++){
				GPoint gPoint1 = list.get(i);
				int latitude = (int) (gPoint1.y * 1E6);
				int longitude = (int) (gPoint1.x * 1E6);
				GeoPoint gP1 = new GeoPoint(latitude, longitude);
				
				GPoint gPoint2 = list.get(i + 1);
				latitude = (int) (gPoint2.y * 1E6);
				longitude = (int) (gPoint2.x * 1E6);
				GeoPoint gP2 = new GeoPoint(latitude, longitude);
	
				Point p1 = new Point();
				Point p2 = new Point();
				Path path = new Path();
	
				projection.toPixels(gP1, p1);
				projection.toPixels(gP2, p2);
	
				path.moveTo(p2.x, p2.y);
				path.lineTo(p1.x, p1.y);

				canvas.drawPath(path, mPaint);				
			}
		}
	}

}
