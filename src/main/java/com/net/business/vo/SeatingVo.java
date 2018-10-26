package com.net.business.vo;

public class SeatingVo {
	public int [] seating=new int[5];
	
	public int addUser(int userId){
		int index=0;
		for (int i = 0; i < seating.length; i++) {
			if (seating[i]==0) {
				seating[i]=userId;
				index=i;
				break;
			}
		}
		return index;
	}
	
	public void addUser(int index,int userId){
		seating[index]=userId;
	}
	
	public void clear(){
		for (int i = 0; i < seating.length; i++) {
			seating[i]=0;
		}
	}
	
	public void removeUser(int userId){
		for (int i = 0; i < seating.length; i++) {
			if (seating[i]==userId) {
				seating[i]=0;
				break;
			}
		}
	}
	
	public int getUserCount(){
		int userCount=0;
		for (int i : seating) {
			if (i!=0) {
				userCount++;
			}
		}
		return userCount;
	}


}
