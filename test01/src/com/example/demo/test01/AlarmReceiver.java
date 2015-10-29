package com.example.demo.test01;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		//���յ�ǰһ��activity����Ϣ
		String time = intent.getStringExtra("time");
		String name = intent.getStringExtra("name");
		String phone = intent.getStringExtra("phone");
		Intent i = new Intent(context, LocktoCall.class);
		//ΪʲôҪ��������仰����startActivity?
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//�ٴ��ݸ���һ��activity
		i.putExtra("time", time);
		i.putExtra("name", name);
		i.putExtra("phone", phone);
		context.startActivity(i);
		Log.v("time", time);
	}
}
