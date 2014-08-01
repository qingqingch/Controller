package com.tao.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;

import org.apache.http.conn.util.InetAddressUtils;

import android.os.Handler;
import android.os.Message;

public class ListenThread implements Runnable 
{
	private Handler handler;
	public ListenThread(Handler msgHandler) 
	{
		handler = msgHandler;
	}

	private String getLocalIP() 
	{
	    try 
	    {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
	        {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) 
	            {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) 
	                {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } 
	    catch (SocketException ex) 
	    {
	        ex.printStackTrace();
	    }
	    return null;
	}
	private String getServerIP () 
	{
		//如果本地保存有ip，先测试
		String ip = MainActivity.ipaddr;
		if (!ip.equals("")) 
		{
			try 
			{
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(ip, 55000), 1000);
				socket.close();
				return ip;
			} 
			catch (UnknownHostException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		//否则获取局域网段，并在其中搜索主机
		String localIP = getLocalIP();
		if (localIP == null) return null;
		String[] localIPArray = localIP.split("\\.");
		for (int i = 2; i < 254; i++) 
		{
			StringBuilder sb = new StringBuilder();
			sb.append(localIPArray[0]);
			sb.append(".");
			sb.append(localIPArray[1]);
			sb.append(".");
			sb.append(localIPArray[2]);
			sb.append(".");
			sb.append(i);
			
			try 
			{
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(sb.toString(), 55000), 1000);
				
				//搜索到主机,存储ip到本地
				socket.close();
				MainActivity.ipaddr = sb.toString();
				//storeIP(sb.toString());
				//通知已经搜索成功
				Message msg = new Message();
				msg.what = CmdType.SERVER_FOUND;
				msg.obj = "server_found";
				handler.sendMessage(msg);
				
				return sb.toString();
			} 
			catch (UnknownHostException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			//通知搜索状态
			Message msg = new Message();
			msg.what = CmdType.SERVER_SEARCHING;
			msg.obj = String.valueOf(i);
			handler.sendMessage(msg);	
		}
		return null;
	}
	@Override
	public void run() 
	{
		try 
		{
			MainActivity.ipaddr = getServerIP();
			if (MainActivity.ipaddr != null) 
			{
				Socket socket = SingleSocket.getInstance();
				PrintStream ps = new PrintStream(socket.getOutputStream());
				ps.println("cmd_type=update_device");
				BufferedReader bufReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				for (String content = null; (content = bufReader.readLine()) != null; )
				{
					Map<String, String> args = Util.splitQueryString(content);
					Message message = new Message();
					message.obj = args.get("data");
					
					String cmdType = args.get("cmd_type");
					if (cmdType.equals("update_device"))
						message.what = CmdType.UPDATE_DEVICE;
					else if (cmdType.equals("update_status"))
						message.what = CmdType.UPDATE_STATUS;
					else if (cmdType.equals("update_task"))
						message.what = CmdType.UPDATE_TASK;
					else if (cmdType.equals("routing_device"))
						message.what = CmdType.ROUTING_DEVICE;
					else if (cmdType.equals("debug_info"))
						message.what = CmdType.DEBUG_INFO;
					handler.sendMessage(message);
				}
			} 
			else 
			{
				Message message = new Message();
				message.what = CmdType.SERVER_NOT_FOUND;
				message.obj = "server_not_found";
				handler.sendMessage(message);
			}
			
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
