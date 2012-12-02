package kr.co.navi;


import kr.co.utils.BaseActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

/**
 *	ù ���� ��Ƽ��Ƽ
 *
 */
public class IntroActivity extends BaseActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_layout);

    }

    // ���� ȭ������ �ѱ��
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		  if ( event.getAction() == MotionEvent.ACTION_DOWN ){
			 Intent intent =  new Intent(this, RouteMapActivity.class);  
			 startActivity(intent);
			 finish();
			 return true;
		  }
		  
		  return super.onTouchEvent(event);
		  
	}	
	
}