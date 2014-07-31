package com.tao.controller;

import java.io.IOException;
import java.net.Socket;

public class SingleSocket {
	private static Socket socket = null;
	public static synchronized Socket getInstance() throws IOException {
		if (socket == null || socket.isClosed()) {
			socket = new Socket(MainActivity.ipaddr, 55000);
		}
		return socket;
	}
}
