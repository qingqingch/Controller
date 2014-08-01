package com.tao.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class DevControlActivity extends Activity implements OnClickListener, OnSeekBarChangeListener
{

	private String device_path;
	private String device_name;
	private int device_index;
	private Button btn_on, btn_off;
	private SeekBar seekBar_lightness;
	private ImageView imageView_lights;
	private Receiver recver;
	private int[] imgs = new int[]{R.drawable.d0,
	                             R.drawable.d1,
	                             R.drawable.d2,
	                             R.drawable.d3,
	                             R.drawable.d4,
	                             R.drawable.d5,
	                             R.drawable.d6,
	                             R.drawable.d7,
	                             R.drawable.d8,
	                             R.drawable.d9,
	                             R.drawable.d10};
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dev_control);
		btn_on = (Button) findViewById(R.id.btn_on);
		btn_off = (Button) findViewById(R.id.btn_off);
		seekBar_lightness = (SeekBar) findViewById(R.id.seekBar_lightness);
		imageView_lights = (ImageView) findViewById(R.id.imageView_lights);
		btn_on.setOnClickListener(this);
		btn_off.setOnClickListener(this);
		seekBar_lightness.setOnSeekBarChangeListener(this);
	}

	@Override
	protected void onStart() 
	{
		super.onStart();
		Intent intent = getIntent();
		device_name = intent.getStringExtra("device_name");
		setTitle(device_name);
		device_path = intent.getStringExtra("device_path");
		device_index = intent.getIntExtra("device_index", -1);
		registRecer();
		new Thread(new SendCmdThread("cmd_type=update_status")).start();
	}
	
	private void registRecer() 
	{
		recver = new Receiver();
		IntentFilter intfilter = new IntentFilter();
		intfilter.addAction("update_status");
		registerReceiver(recver, intfilter);
	}
	protected void onStop() 
	{
		super.onStop();
		unregisterReceiver(recver);
	}
	private class Receiver extends BroadcastReceiver 
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			String[] result = intent.getStringExtra("data").split("\\|");
			int lightness = Integer.valueOf(result[device_index]);
			imageView_lights.setImageResource(imgs[(int)Math.ceil(lightness/12)]);
			//imageView_lights.setBackgroundResource(imgs[(int)Math.ceil(lightness/12)]);
			seekBar_lightness.setProgress(lightness);
		}
		
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
	{
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) 
	{
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) 
	{
		imageView_lights.setImageResource(imgs[(int)Math.ceil(seekBar.getProgress()/12)]);
		new Thread(new SendCmdThread("cmd_type=change_lightness&lightness="+seekBar.getProgress()+"&device_path="+device_path)).start();
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId())
		{
		case R.id.btn_on:
			imageView_lights.setImageResource(R.drawable.d10);
			seekBar_lightness.setProgress(127);
			new Thread(new SendCmdThread("cmd_type=change_lightness&lightness=127&device_path="+device_path)).start();
			break;
		case R.id.btn_off:
			imageView_lights.setImageResource(R.drawable.d0);
			seekBar_lightness.setProgress(0);
			new Thread(new SendCmdThread("cmd_type=change_lightness&lightness=0&device_path="+device_path)).start();
			break;
		}
	}
}
