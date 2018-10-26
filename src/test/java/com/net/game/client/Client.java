package com.net.game.client;

import com.net.server.data.GameArray;
import com.net.server.data.GameObject;
import com.net.server.data.IGameArray;
import com.net.server.data.IGameObject;

import io.netty.channel.Channel;

public class Client {

	public static void main(String[] args) {
		final ClusterClient client=new ClusterClient();
		try {
			client.connect(9934, "127.0.0.1",new ConnectionCallBack() {
				public void loadPlayerSuccess(IGameObject params,Channel channel) {
					System.out.println(params.toJson());
					int userId=params.getInt("i");
					IGameObject gameObject=GameObject.newInstance();
					gameObject.putInt("ui", userId);
					gameObject.putUtfString("name", "testName");
					client.sendExt(gameObject, new Short("10000"));
					
//					fight(client);
//					buildingUp(client);
//					deputyShipUp(client);
//					createAmmunition(client);
//					createEquip(client);
//					equipLevelUp(client);
//					soldDeputyShip(client);
//					repairPlayerShip(client);
//					engineerLevelUp(client);
//					createRareResources(client);
//					sweepStage(client);
//					passStage(client);
//					soldEquipOrItem(client);
				}
				public void onServerResponse(IGameObject params, Channel channel) {
					System.out.println(params.toJson());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public interface ConnectionCallBack{
		void loadPlayerSuccess(IGameObject params,Channel channel);
		void onServerResponse(IGameObject params,Channel channel);
	}

	/**
	 * 修改阵型
	 * @return
	 */
	@SuppressWarnings("unused")
	private static IGameObject changeFormation() {
		IGameObject gameObject = GameObject.newInstance();
		gameObject.putInt("f", 16);
		gameObject.putInt("t", 1);
		gameObject.putUtfString("n", "随机阵型");
		IGameArray gameArray = GameArray.newInstance();
		
		IGameObject obj1 = GameObject.newInstance();
		obj1.putInt("si", 5);
		obj1.putBool("m", true);
		obj1.putInt("x", 4);
		obj1.putInt("y", 3);
		gameArray.addGameObject(obj1);
		
		IGameObject obj2 = GameObject.newInstance();
		obj2.putInt("si", 6);
		obj2.putBool("m", false);
		obj2.putInt("x", 7);
		obj2.putInt("y", 2);
		gameArray.addGameObject(obj2);
		
		IGameObject obj3 = GameObject.newInstance();
		obj3.putInt("si", 7);
		obj3.putBool("m", false);
		obj3.putInt("x", 7);
		obj3.putInt("y", 6);
		gameArray.addGameObject(obj3);
		
		gameObject.putGameArray("fl", gameArray);
		
		return gameObject;
	}
	
	/**
	 * 建筑升级
	 * @param client
	 * @return
	 */
	@SuppressWarnings("unused")
	private static IGameObject buildingUp(ClusterClient client) {
		IGameObject object = GameObject.newInstance();
		object.putInt("p", 27);
		client.sendExt(object, new Short("4001"));
		return object;
	}
	
	/**
	 * 副舰升级
	 * @param client
	 * @return
	 */
	@SuppressWarnings("unused")
	private static IGameObject deputyShipUp(ClusterClient client) {
		IGameObject object = GameObject.newInstance();
		object.putInt("p", 6);
		client.sendExt(object, new Short("3002"));
		return object;
	}
	
	/**
	 * 弹药生产
	 * @param client
	 * @return
	 */
	@SuppressWarnings("unused")
	private static IGameObject createAmmunition(ClusterClient client) {
		IGameObject object = GameObject.newInstance();
		object.putInt("ai", 2);
		client.sendExt(object, new Short("4004"));
		return object;
	}
	
	/**
	 * 制造装备
	 * @param client
	 * @return
	 */
	@SuppressWarnings("unused")
	private static IGameObject createEquip(ClusterClient client) {
		IGameObject object = GameObject.newInstance();
		object.putInt("ei", 3001);
		client.sendExt(object, new Short("4002"));
		return object;
	}
	/**
	 * 装备升级
	 * @param client
	 * @return
	 */
	@SuppressWarnings("unused")
	private static IGameObject equipLevelUp(ClusterClient client) {
		IGameObject object = GameObject.newInstance();
		object.putInt("pi", 94);
		client.sendExt(object, new Short("4003"));
		return object;
	}
	
	@SuppressWarnings("unused")
	private static IGameObject soldDeputyShip(ClusterClient client) {
		IGameObject object = GameObject.newInstance();
		object.putInt("p", 34);
		client.sendExt(object, new Short("3003"));
		return object;
	}
	
	
	@SuppressWarnings("unused")
	private static IGameObject repairPlayerShip(ClusterClient client) {
		IGameObject object = GameObject.newInstance();
		IGameArray gameArray = GameArray.newInstance();
		
		IGameObject object1 = GameObject.newInstance();
		object1.putInt("si", 1);
		gameArray.addGameObject(object1);
		
		object.putGameArray("pl", gameArray);
		
		client.sendExt(object, new Short("2007"));
		return object;
	}
	@SuppressWarnings("unused")
	private static IGameObject engineerLevelUp(ClusterClient client) {
		IGameObject object = GameObject.newInstance();
		object.putInt("e", 1);
		
		client.sendExt(object, new Short("4005"));
		return object;
	}
	@SuppressWarnings("unused")
	private static IGameObject createRareResources(ClusterClient client) {
		IGameObject object = GameObject.newInstance();
		object.putInt("ri", 2);
//		object.putBool("c", false);
//		object.putBool("d", false);
		object.putBool("c", true);
		object.putBool("d", true);
		client.sendExt(object, new Short("4006"));
		return object;
	}
	
	@SuppressWarnings("unused")
	private static IGameObject sweepStage(ClusterClient client) {
		IGameObject object = GameObject.newInstance();
		object.putInt("si", 1);
		client.sendExt(object, new Short("5002"));
		return object;
	}
	
	@SuppressWarnings("unused")
	private static IGameObject passStage(ClusterClient client) {
		IGameObject object = GameObject.newInstance();
		object.putInt("si", 1);
		client.sendExt(object, new Short("5001"));
		return object;
	}
	
	@SuppressWarnings("unused")
	private static IGameObject fight(ClusterClient client) {
		IGameObject gameObject = GameObject.newInstance();
		gameObject.putInt("r", 5);
		gameObject.putBool("b", false);
		client.sendExt(gameObject, new Short("9000"));
		
		return gameObject;
	}
	
	@SuppressWarnings("unused")
	private static IGameObject soldEquipOrItem(ClusterClient client) {
		IGameObject object = GameObject.newInstance();
		IGameArray gameArray = GameArray.newInstance();
		
		IGameObject object1 = GameObject.newInstance();
		object1.putInt("i", 166);
		object1.putInt("n", 1);
		
		gameArray.addGameObject(object1);
		
		object.putGameArray("pl", gameArray);
		
		client.sendExt(object, new Short("6001"));
		return object;
	}
}
