package com.net.business.extensions.listener;

import com.net.business.basedata.BaseDataManager;
import com.net.business.extensions.core.BaseServerEventHandler;
import com.net.server.core.ISFSEvent;

/**
 * 用户启动完成事件监听
 * @author caipeiping
 *
 */
public class ServerStartListener extends BaseServerEventHandler {

	public void handleServerEvent(ISFSEvent paramISFSEvent) throws Exception {
		BaseDataManager baseDataManager = BaseDataManager.getInstance();
		baseDataManager.loadBaseData();
	}
	
	public static void main(String[] args) {
		int number=(int) (596/100.0*30);
		double d=596/100.0*30;
		System.out.println(number);
		System.out.println(d);
	}

}
