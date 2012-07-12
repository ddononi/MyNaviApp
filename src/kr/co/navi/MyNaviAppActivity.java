package kr.co.navi;

import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.navi.utils.CoordinateTransformation;
import kr.co.navi.utils.GPoint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MyNaviAppActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		test();

	}

	private void test() {
		Log.i("naviApp", "start");
		GeoRouteSearch.Params parmas = new GeoRouteSearch.Params();

		// 테스트
		//37.562362,127.156524
		// 좌표 변환  wgs -> tm
		GPoint pnt = new GPoint(Double.valueOf(127.156524), Double.valueOf(37.562362));
		GPoint outPoint = CoordinateTransformation.convert(CoordinateTransformation.WGS84, CoordinateTransformation.TM, pnt);
		parmas.SX = "" + outPoint.x;
		parmas.SY = "" + outPoint.y;

		// 좌표 변환  wgs -> tm
		//37.546372,127.143521
		pnt = new GPoint(Double.valueOf(127.143521), Double.valueOf(37.546372));
		outPoint = CoordinateTransformation.convert(CoordinateTransformation.WGS84, CoordinateTransformation.TM, pnt);
		parmas.EX = "" + outPoint.x;
		parmas.EY = "" + outPoint.y;
		parmas.RPTYPE = "0";
		parmas.COORDTYPE = "2";
		parmas.PRIORITY = "0";
		parmas.timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		GeoRouteSearch t = new GeoRouteSearch(parmas);
		TextView logTv = (TextView)findViewById(R.id.log);
		logTv.setText("payload : " + t.execute());
	}

}