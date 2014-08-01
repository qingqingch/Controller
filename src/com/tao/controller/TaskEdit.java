package com.tao.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TimePicker;
import android.widget.Toast;

public class TaskEdit extends Activity 
{
	private Button selDevices, setTime, setDow, setLightness, confirm, cancel;
	private String[] names, ids;
	private String devIds, time, dow, lightness, rowid; 
	private boolean[] isDevCheck, isDowCheck = new boolean[7];
	private SeekBar sb;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_edit);
		getViewObjs();
		ArrayList<String> items = new ArrayList<String>();
		for (HashMap<String, String> map : MainActivity.listData)
		{
			items.add(map.get("name"));
		}
		int len = items.size();
		names = items.toArray(new String[len]);
		isDevCheck = new boolean[len];
		for (int i = 0; i < len; i ++) isDevCheck[i] = true;
		items.clear();
		for (HashMap<String, String> map : MainActivity.listData) 
		{
			items.add(map.get("path").substring(0,1));
		}
		ids = items.toArray(new String[len]);
		for (int i = 0; i < 7; i ++) isDowCheck[i] = true;
	}
	protected void onStart() 
	{
		super.onStart();
		devIds = time = dow = lightness = "";
		Intent intent = getIntent();
		rowid = intent.getStringExtra("rowid");
		if (rowid.equals("")) return;
		devIds = intent.getStringExtra("ids");
		time = intent.getStringExtra("time");
		dow = intent.getStringExtra("dow");
		lightness = intent.getStringExtra("lightness");
		selDevices.setText(devIds);
		setTime.setText(time);
		setDow.setText(dow);
		setLightness.setText(lightness);
	}
	private void getViewObjs() 
	{
		selDevices = (Button) findViewById(R.id.select_devices_btn);
		setTime = (Button) findViewById(R.id.time_btn);
		setDow = (Button) findViewById(R.id.dow_btn);
		setLightness = (Button) findViewById(R.id.lightness_btn);
		confirm = (Button) findViewById(R.id.confim_btn);
		cancel = (Button) findViewById(R.id.cancel_btn);
		selDevices.setOnClickListener(new BtnClickHandler());
		setTime.setOnClickListener(new BtnClickHandler());
		setDow.setOnClickListener(new BtnClickHandler());
		setLightness.setOnClickListener(new BtnClickHandler());
		confirm.setOnClickListener(new BtnClickHandler());
		cancel.setOnClickListener(new BtnClickHandler());
	}
	private class BtnClickHandler implements OnClickListener 
	{
		@Override
		public void onClick(View v) 
		{
			int btnId = v.getId();
			if (btnId == R.id.select_devices_btn) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(TaskEdit.this);
				builder.setTitle("选择设备");
				builder.setMultiChoiceItems(names, isDevCheck, new OnMultiChoiceClickListener() 
				{
					@Override
					public void onClick(DialogInterface view, int which, boolean isChecked) 
					{
						if (isChecked) isDevCheck[which] = true;
						else isDevCheck[which] = false;
					}
				});
				builder.setPositiveButton("确认", new DialogInterface.OnClickListener() 
				{
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						devIds = "";
						int len = isDevCheck.length;
						for (int i = 0; i < len; i ++) 
						{
							if (isDevCheck[i]) devIds += ids[i] + ",";
						}
						selDevices.setText(devIds);
					}
				});
				builder.setNegativeButton("取消", null);
				builder.create().show();
			} 
			else if (btnId == R.id.time_btn) 
			{
				int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
				int minute = Calendar.getInstance().get(Calendar.MINUTE);
				new TimePickerDialog(TaskEdit.this, new OnTimeSetListener()
				{	
					@Override
					public void onTimeSet(TimePicker view, int hour, int minute) 
					{
						time = hour + ":" + minute;
						setTime.setText(time);
					}
				}, hour, minute, true).show();
			} 
			else if (btnId == R.id.dow_btn) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(TaskEdit.this);
				builder.setTitle("选择星期");
				builder.setMultiChoiceItems(new String[]{"星期天","星期一","星期二","星期三","星期四","星期五","星期六"}, 
						isDowCheck, new OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface view, int which, boolean isChecked) 
					{
						if (isChecked) isDowCheck[which] = true;
						else isDowCheck[which] = false;
					}
				});
				builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						dow = "";
						for (int i = 0; i < 7; i ++)
						{
							if (isDowCheck[i]) dow += (i + 1) + ",";
						}
						setDow.setText(dow);
					}
				});
				builder.setNegativeButton("取消", null);
				builder.create().show();
			} 
			else if (btnId == R.id.lightness_btn) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(TaskEdit.this);
				builder.setTitle("设置亮度");
				sb = new SeekBar(TaskEdit.this);
				sb.setMax(127);
				sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					public void onStopTrackingTouch(SeekBar arg0) 
					{
						Toast.makeText(TaskEdit.this, String.valueOf(arg0.getProgress()), Toast.LENGTH_SHORT).show();
					}
					public void onStartTrackingTouch(SeekBar arg0) 
					{	
					}
					public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2)
					{
					}
				});
				builder.setView(sb);
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) 
					{
						lightness = String.valueOf(sb.getProgress());
						setLightness.setText(lightness);
					}
				});
				builder.setNegativeButton("取消", null);
				builder.create().show();
			} 
			else if (btnId == R.id.confim_btn) {
				if (devIds.equals("") || time.equals("") || dow.equals("") || lightness.equals("")) 
				{
					Toast.makeText(TaskEdit.this, "选项不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				String request;
				if (rowid.equals("")) 
				{
					request = "cmd_type=add_task&";
					
				} 
				else 
				{
					request = "cmd_type=modify_task&";
					request += "rowid=" + rowid + "&";
				}
				request += "ids=" + devIds + "&";
				request += "time=" + time + "&";
				request += "dow=" + dow + "&";
				request += "lightness=" + lightness;
				new Thread(new SendCmdThread(request)).start();
				Intent intent = new Intent(TaskEdit.this, TaskList.class);
				startActivity(intent);
			} 
			else if (btnId == R.id.cancel_btn) 
			{
				Intent intent = new Intent(TaskEdit.this, TaskList.class);
				startActivity(intent);
			}
		}
	}
}
