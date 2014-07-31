package com.tao.controller;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class SendCmdThread implements Runnable {
	private String command;
	public SendCmdThread(String cmd) {
		command = cmd;
	}
	@Override
	public void run() {
		Socket socket;
		try {
			socket = SingleSocket.getInstance();
			PrintStream ps = new PrintStream(socket.getOutputStream());
			ps.println(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
