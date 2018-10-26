package com.net.business.extensions.handler;

public class Constants {
	public enum RoomStatus{
		Wait(-1),InGame(0),Stop(1),WaitNext(3),WaitReplay(4);
		
		private int status;
		
		private RoomStatus(int status) {
			this.status=status;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}
	}
}
