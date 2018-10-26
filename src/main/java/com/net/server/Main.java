package com.net.server;

public class Main {
	public static void main(String[] args) {
		GameServer server = GameServer.getInstance();
		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}	