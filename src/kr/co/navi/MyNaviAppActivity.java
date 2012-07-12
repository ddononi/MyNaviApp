package kr.co.navi;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kr.co.navi.utils.CoordinateTransformation;
import kr.co.navi.utils.GPoint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MyNaviAppActivity extends Activity {
	private TextView logTv;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		test();

	}

	private void test() {
		Log.i("naviApp", "start");
		GeoRouteSearch.Params parmas = new GeoRouteSearch.Params();

		// 테스트
		//37.562362,127.156524
		// 좌표 변환  wgs -> tm
		GPoint pnt = new GPoint(Double.valueOf(127.156524), Double.valueOf(37.562362));
		//GPoint outPoint = CoordinateTransformation.convert(CoordinateTransformation.WGS84, CoordinateTransformation.TM, pnt);
		parmas.SX = "" + pnt.x;
		parmas.SY = "" + pnt.y;

		// 좌표 변환  wgs -> tm
		//37.546372,127.143521
		pnt = new GPoint(Double.valueOf(127.143521), Double.valueOf(37.546372));
		//outPoint = CoordinateTransformation.convert(CoordinateTransformation.WGS84, CoordinateTransformation.TM, pnt);
		parmas.EX = "" + pnt.x;
		parmas.EY = "" + pnt.y;
		parmas.RPTYPE = "0";
		parmas.COORDTYPE = "0";
		parmas.PRIORITY = "0";
		parmas.timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		GeoRouteSearch t = new GeoRouteSearch(parmas);
		logTv = (TextView)findViewById(R.id.log);
		//logTv.setText("payload : " + t.execute());
		String jsonText =  t.execute();
		parseJson(jsonText.trim());
	}

	private void parseJson(String json) {
		try {
			 json = json.replaceFirst("\"", "");
			 json = json.substring(0, json.lastIndexOf("\"") + 1);
			Log.i("navi", "json : " +  json);
			JSONObject RESDATA =  (new JSONObject(json)).getJSONObject("RESDATA");
			JSONObject SROUTE =  RESDATA.getJSONObject("SROUTE");		
			JSONObject LINKS = SROUTE.getJSONObject("LINKS");					
			JSONArray links = LINKS.getJSONArray("link");

			for(int i = 0; i < links.length(); i++){
				JSONObject arr = links.getJSONObject(i);
				JSONArray vertex = arr.getJSONArray("vertex");
				for(int j = 0; j < vertex.length(); j++){
					JSONObject obj =  vertex.getJSONObject(j);
			
					String y = obj.getString("y");
					String x = obj.getString("x");
					
					
				//	GPoint pnt = new GPoint(Double.valueOf(y), Double.valueOf(x));
				//	GPoint outPoint = CoordinateTransformation.convert(CoordinateTransformation.TM,  CoordinateTransformation.WGS84, pnt);	
					
					Log.i("navi", "y : " + y);
					Log.i("navi", "x : " + x);
					logTv.setText(logTv.getText() +  "y : " +  y +"   x :   " +  x + "\n");
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}