package com.example.demo.test01;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LocktoCall extends Activity implements OnTouchListener
{
	private TextView locktime;
	private TextView lockcontent;
	private TextView remark;
	private ViewGroup rootview;
	private ImageView phoneround;
	private int deltaX, deltaY, positionX, positionY;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lockscreen);
		
		locktime = (TextView)findViewById(R.id.locktime);
		lockcontent = (TextView)findViewById(R.id.lockcontent);
		phoneround = (ImageView)findViewById(R.id.unlock);
		rootview = (ViewGroup)findViewById(R.id.rootview);
		
		Intent intent = getIntent();
		String time = intent.getStringExtra("time");
		String name = intent.getStringExtra("name");
		//final String phone = intent.getStringExtra("phone");
		
		locktime.setText(time);
		lockcontent.setText("快给" + " " + name + " " + "打电话");
		
		phoneround.setOnTouchListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode==KeyEvent.KEYCODE_BACK)
		{
			return false;
		}
		if(keyCode==KeyEvent.KEYCODE_MENU);
		{
			return false;
		}
	}
	
	public boolean onTouch(View v, MotionEvent mv)
	{
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)v.getLayoutParams();
		switch (mv.getAction() & MotionEvent.ACTION_MASK)
		{
		case MotionEvent.ACTION_DOWN:
			//当前所在位置
			positionX = (int)mv.getRawX();
			positionY = (int)mv.getRawY();
	        break; 
	    case MotionEvent.ACTION_MOVE:
	    	//获得手指移动到的地方
	    	int movetoX = (int)mv.getRawX();
	    	int movetoY = (int)mv.getRawY();
	    	
	    	//计算移动距离
	    	deltaX = movetoX - positionX;
	    	deltaY = movetoY - positionY;
	    	
	    	Toast.makeText(getApplicationContext(), "x", Toast.LENGTH_SHORT).show();
	        break;
	    case MotionEvent.ACTION_UP:
	    	v.setLayoutParams(params);
	    	break;
	    }  
	    rootview.invalidate();  
	    return true;
	}
		/*
		@Override
		public void onClick(View v)
		{
			//Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
			//startActivity(intent);
		}*/
}
