package com.tao.controller;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class UpdateService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new ListenThread(new MsgHandler())).start();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@SuppressLint("HandlerLeak")
	private class MsgHandler extends Handler {
		public void handleMessage(Message msg) {
			String result = msg.obj.toString();
			Intent intent = new Intent();
			intent.putExtra("data", result);
			if (msg.what == CmdType.UPDATE_STATUS) {
				intent.setAction("update_status");
			} else if (msg.what == CmdType.UPDATE_DEVICE) {
				intent.setAction("update_device");
			} else if (msg.what == CmdType.ROUTING_DEVICE) {
				intent.setAction("routing_device");
			} else if (msg.what == CmdType.UPDATE_TASK) {
				intent.setAction("update_task");
			} else if (msg.what == CmdType.DEBUG_INFO) {
				intent.setAction("debug_info");
			} else if (msg.what == CmdType.SERVER_NOT_FOUND) {
				intent.setAction("server_not_found");
			} else if (msg.what == CmdType.SERVER_SEARCHING) {
				intent.setAction("server_searching");
			} else if (msg.what == CmdType.SERVER_FOUND) {
				intent.setAction("server_found");
			}
			sendBroadcast(intent);
		}
	}
}
