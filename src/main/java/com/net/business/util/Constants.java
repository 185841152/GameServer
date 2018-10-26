package com.net.business.util;

public class Constants {
	public static String COMMON_COST_KEY="cp";
	public static String COMMON_ADD_KEY="dp";
	
	public static String USER_REDIS_KEY="redis_user_";
	public static String REDIS_FRIEND_ROOM_COUNT="redis_friend_room_count";
	public static String REDIS_MATCHING_ROOM_COUNT="redis_matching_room_count";
	
	public static String REDIS_PLAYER_RECORD_KEY="redis_record_";
	public static String REDIS_RANKING_KEY="redis_ranking_";
	public static String REDIS_FREE_COUNT_KEY="redis_free_count";
	public static String REDIS_FREE_MATCHING_COUNT_KEY="redis_free_matching_count";
	public static String REDIS_NOTICE_KEY="redis_notice_key";
	public static String REDIS_NOTICE_RECHARGE_KEY="redis_notice_recharge_key";
	public static String REDIS_NOTICE_ROLL_KEY="redis_notice_roll_key";
	
	public enum UserStatus{
		normal(0),Frozen(1);
		
		private int status;
		
		private UserStatus(int status) {
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
