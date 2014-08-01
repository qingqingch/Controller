package com.tao.controller;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TaskList extends Activity {
	
	private ListView listView;
	private ArrayList<HashMap<String, String>> listData;
	//private SimpleAdapter listAdapter;
	private TaskAdapter listAdapter;
	private Receiver recver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_list);
		listView = (ListView) findViewById(R.id.schedule_list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) 
			{
				String rowid = listData.get(position).get("rowid");
				String request = "cmd_type=toggle_task&rowid=" + rowid + "&ispaused=";
				CheckBox cb = (CheckBox)view.findViewById(R.id.item_cb);
				if (cb.isChecked()) 
				{
					cb.setChecked(false);
					request += "1";
					Toast.makeText(TaskList.this, "任务已关闭", Toast.LENGTH_SHORT).show();
				} 
				else 
				{
					cb.setChecked(true);
					request += "0&time=" + listData.get(position).get("time");
					request += "&dow=" + listData.get(position).get("dow");
					request += "&ids=" + listData.get(position).get("ids");
					request += "&lightness=" + listData.get(position).get("lightness");
					Toast.makeText(TaskList.this, "任务已开启", Toast.LENGTH_SHORT).show();
				}
				new Thread(new SendCmdThread(request)).start();
			}
		});
		listData = new ArrayList<HashMap<String, String>>();
		listAdapter = new TaskAdapter(this,  listData);
		listView.setAdapter(listAdapter);
	}
	protected void onStart() 
	{
		super.onStart();
		registerForContextMenu(listView);
		registRecer();
		new Thread(new SendCmdThread("cmd_type=update_task")).start();
	}
	private void registRecer() 
	{
		recver = new Receiver();
		IntentFilter intfilter = new IntentFilter();
		intfilter.addAction("update_task");
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
		public void onReceive(Context context, Intent intent) 
		{
			listData.clear();
			if (intent.getStringExtra("data").equals("")) 
			{
				Toast.makeText(TaskList.this, "没有计划", Toast.LENGTH_SHORT).show();
			} 
			else 
			{
				String[] items = intent.getStringExtra("data").split("\\(");
				
				for (String item : items) {
					String[] fields = item.split("\\|");
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("rowid", fields[0]);
					map.put("ids", fields[1]);
					map.put("time", fields[2]);
					map.put("dow", fields[3]);
					map.put("lightness", fields[4]);
					map.put("ispaused", fields[5]);
					map.put("main", String.format("时间:%02d:%02d 亮度:%03d", Integer.parseInt(fields[2].split(":")[0]),
							Integer.parseInt(fields[2].split(":")[1]),Integer.parseInt(fields[4])));
					String sub = "";
					if (fields[3].contains("1")) sub += "星期天 ";
					if (fields[3].contains("2")) sub += "星期一 ";
					if (fields[3].contains("3")) sub += "星期二 ";
					if (fields[3].contains("4")) sub += "星期三 ";
					if (fields[3].contains("5")) sub += "星期四 ";
					if (fields[3].contains("6")) sub += "星期五 ";
					if (fields[3].contains("7")) sub += "星期六 ";
					map.put("sub", sub);
					listData.add(map);
				}
			}
			listAdapter.notifyDataSetChanged();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.tasklist_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.add_task) 
		{
			Intent intent = new Intent(TaskList.this, TaskEdit.class);
			intent.putExtra("rowid", "");
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
		String rowid = listData.get(acmi.position).get("rowid");
		switch(item.getItemId()) 
		{
		case 1:
			Intent intent = new Intent(TaskList.this, TaskEdit.class);
			intent.putExtra("rowid", rowid);
			intent.putExtra("ids", listData.get(acmi.position).get("ids"));
			intent.putExtra("time", listData.get(acmi.position).get("time"));
			intent.putExtra("dow", listData.get(acmi.position).get("dow"));
			intent.putExtra("lightness", listData.get(acmi.position).get("lightness"));
			startActivity(intent);
			break;
		case 2:
			String request = "cmd_type=delete_task&rowid=" + rowid;
			new Thread(new SendCmdThread(request)).start();
			break;
		case 3:
			String msg = String.format("星期：\n%s\n\n设备：\n%s", listData.get(acmi.position).get("sub"),
					listData.get(acmi.position).get("ids"));
			new AlertDialog.Builder(TaskList.this).setTitle("任务详情：")
			.setMessage(msg).setPositiveButton("确认", null).show();
		}
		return super.onContextItemSelected(item);
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, 1, 0, "编辑");
		menu.add(0, 2, 0, "删除");
		menu.add(0, 3, 0, "查看详情");
	}
}

class TaskAdapter extends BaseAdapter 
{

	private ArrayList<HashMap<String, String>> data;
	private LayoutInflater inflater;
	
	public TaskAdapter(Context context, ArrayList<HashMap<String, String>> data) 
	{
		this.data = data;
		this.inflater = LayoutInflater.from(context);
	}
	@Override
	public int getCount() 
	{
		return data.size();
	}

	@Override
	public Object getItem(int position)
	{
		return data.get(position);
	}

	@Override
	public long getItemId(int position) 
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		ViewHolder holder = null;
		if (convertView == null) 
		{
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.task_item, null);
			holder.text = (TextView) convertView.findViewById(R.id.item_tv);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.item_cb);
			convertView.setTag(holder);
		} 
		else 
		{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.text.setText(data.get(position).get("main"));
		if (data.get(position).get("ispaused").equals("0")) holder.checkbox.setChecked(true);
		else holder.checkbox.setChecked(false);
		return convertView;
	}
	
	final class ViewHolder 
	{
		TextView text;
		CheckBox checkbox;
	}
}
