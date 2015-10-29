package com.example.demo.test01;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MusicService extends Service
{
	//Ϊ��־������ӱ��
	private static String TAG = "MusicService";
	//�������ֲ���������
	private MediaPlayer mediaplayer;
	//���������ļ�·��
	private Uri mMusicpath;
	private String data;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		/*��ȡactivity���ݹ�������Ϣ*/
		data = intent.getStringExtra("uri");
		mMusicpath = Uri.parse(data);
		//Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
		mediaplayer = MediaPlayer.create(getApplicationContext(), mMusicpath);
		if (mediaplayer == null)
			Toast.makeText(MusicService.this, "null!~~~", Toast.LENGTH_SHORT).show();
		/*��ʼ����*/
		mediaplayer.start();
	}
	
	@Override
	public void onDestroy()
	{
		mediaplayer.stop();
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		//mediaplayer.start();
		return null;
	}
	
	public Boolean onUnBind(Intent intent)
	{
		//mediaplayer.stop();
		return super.onUnbind(intent);
	}
}