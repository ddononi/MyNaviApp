package kr.co.navi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

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
		GeoRouteSearch t = new GeoRouteSearch(parmas);
		t.execute();
	}

}