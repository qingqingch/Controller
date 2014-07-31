package com.tao.controller;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ListView dev_list;
	private EditText devName;
	private ProgressDialog progressDialog;
	private ProgressDialog findServerDialog;
	public static ArrayList<HashMap<String, String>> listData;
	private SimpleAdapter listAdapter;
	private Receiver recver;
	public static String ipaddr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dev_list);
		setTitle("设备列表");
		dev_list = (ListView) findViewById(R.id.dev_list);
		dev_list.setOnItemClickListener(new ItemPressHandler());
		dev_list.setOnItemLongClickListener(new ItemPressHandler());
		
		listData = new ArrayList<HashMap<String, String>>();
		listAdapter = new SimpleAdapter(this, listData, android.R.layout.simple_list_item_2, 
				new String[]{"name", "lightness"}, new int[]{android.R.id.text1, android.R.id.text2});
		dev_list.setAdapter(listAdapter);
	}
	@Override
	protected void onStart() {
		super.onStart();
		registRecer();
		if (ipaddr == null) {
			SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
			ipaddr = prefs.getString("ipaddr", "");
			startService(new Intent(this, UpdateService.class));
		}
		else {
			new Thread(new SendCmdThread("cmd_type=update_device")).start();
		}
	}
	private void registRecer() {
		recver = new Receiver();
		IntentFilter intfilter = new IntentFilter();
		intfilter.addAction("update_device");
		intfilter.addAction("routing_device");
		intfilter.addAction("debug_info");
		intfilter.addAction("server_not_found");
		intfilter.addAction("server_found");
		intfilter.addAction("server_searching");
		registerReceiver(recver, intfilter);
	}
	
	protected void onStop() {
		super.onStop();
		if (recver != null)
			unregisterReceiver(recver);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			if (SingleSocket.getInstance().isConnected())
				SingleSocket.getInstance().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Intent i = new Intent(this, UpdateService.class);
		stopService(i);
		SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
		Editor mEditor = prefs.edit();
		mEditor.putString("ipaddr", ipaddr);
		mEditor.commit();
		ipaddr = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.dev_list_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.update_dev) {
			Builder dialog = new AlertDialog.Builder(MainActivity.this);
			devName = new EditText(MainActivity.this);
			dialog.setTitle("输入设备数量").setView(devName);
			dialog.setPositiveButton("确定", new ButtonClick(-1));
			dialog.setNegativeButton("取消", null);
			dialog.show();
		} else if (id == R.id.display_tasks) {
			Intent intent = new Intent(MainActivity.this, TaskList.class);
			startActivity(intent);
		} else if (id == R.id.all_on) {
			new Thread(new SendCmdThread("cmd_type=commandAll&lightness=127")).start();
		} else if (id == R.id.all_off) {
			new Thread(new SendCmdThread("cmd_type=commandAll&lightness=0")).start();
		}
		return super.onOptionsItemSelected(item);
	}

	
	private class Receiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String result = intent.getStringExtra("data");
			if (intent.getAction().equals("update_device")) {
				if (result.equals("")) {
					Toast.makeText(MainActivity.this, "No device!", Toast.LENGTH_SHORT).show();
					return;
				}
				String[] arr = result.split("\\)");
				listData.clear();
				for (String s : arr)
				{
					HashMap<String, String> map = new HashMap<String, String>();
					int index = s.indexOf("[");
					int eindex = s.indexOf("]");
					int dotIndex = s.indexOf(",");
					
					String name = String.format("%s(%s)", s.substring(0, index),
							dotIndex == -1 ? s.substring(index+1, eindex) : s.substring(index+1, dotIndex)); 
					map.put("name", name);
					map.put("path", s.substring(index+1,eindex));
					map.put("lightness", s.substring(eindex+2,s.length()));
					listData.add(map);
				}
				listAdapter.notifyDataSetChanged();
			} else if (intent.getAction().equals("routing_device")) {
				int progress = Integer.parseInt(result);
				if (progressDialog == null) {
					progressDialog = new ProgressDialog(MainActivity.this);
					progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					progressDialog.setMessage("处理设备");
					progressDialog.setCancelable(true);
					progressDialog.setMax(100);
					progressDialog.show();
					progressDialog.setProgress(progress);
					if (progress == 100) progressDialog.cancel();
				} else {
					progressDialog.show();
					progressDialog.setProgress(progress);
					if (progress == 100) progressDialog.cancel();
				}
			} else if (intent.getAction().equals("debug_info")) {
				progressDialog.cancel();
				Toast.makeText(MainActivity.this, "没找到这些设备"+result, Toast.LENGTH_LONG).show();
			} else if (intent.getAction().equals("server_not_found")) {
				//Toast.makeText(MainActivity.this, "没找到这些设备", Toast.LENGTH_LONG).show();
				findServerDialog.cancel();
				new AlertDialog.Builder(MainActivity.this).setTitle("提醒").setMessage("没有搜索到服务器！").
				setPositiveButton("确认", new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						MainActivity.this.finish();
					}
				}).show();
			} else if (intent.getAction().equals("server_searching")) {
				//int progress = Integer.parseInt(result);
				if (findServerDialog == null) {
					findServerDialog = new ProgressDialog(MainActivity.this);
					findServerDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					findServerDialog.setMessage("正在搜索主机...");
					findServerDialog.show();
				} else {
					findServerDialog.show();
				}
			} else if (intent.getAction().equals("server_found")) {
				findServerDialog.cancel();
			}
		}
	}
	
	private class ItemPressHandler implements OnItemClickListener, OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			Builder dialog = new AlertDialog.Builder(MainActivity.this);
			devName = new EditText(MainActivity.this);
			dialog.setTitle("输入新设备名").setView(devName);
			dialog.setPositiveButton("确定", new ButtonClick(arg2));
			dialog.setNegativeButton("取消", new ButtonClick(arg2));
			dialog.show();
			return true;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			Intent intent = new Intent(MainActivity.this, DevControlActivity.class);
			intent.putExtra("device_index", arg2);
			intent.putExtra("device_name", listData.get(arg2).get("name"));
			intent.putExtra("device_path", listData.get(arg2).get("path"));
			startActivity(intent);
		}
		
	}
	private class ButtonClick implements OnClickListener {
		private int itemIndex;
		public ButtonClick(int index) {
			itemIndex = index;
		}
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == DialogInterface.BUTTON_POSITIVE) {
				String name = devName.getText().toString();
				if (!name.isEmpty()) {
					if (itemIndex >= 0)
						new Thread(new SendCmdThread("cmd_type=change_name&id="+listData.get(itemIndex).get("path").substring(0, 1)+"&name="+name)).start();
					else {
						try {
							Integer.parseInt(name);
							new Thread(new SendCmdThread("cmd_type=routing_device&device_number=" + name)).start();
						} catch (NumberFormatException e) {
							Toast.makeText(MainActivity.this, "请输入一个正整数", Toast.LENGTH_SHORT).show();
						} catch (Exception e) {
							Toast.makeText(MainActivity.this, "发生错误", Toast.LENGTH_SHORT).show();
						}
					}
				}else
					Toast.makeText(getApplicationContext(), "不能为空", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
