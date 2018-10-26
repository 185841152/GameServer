package com.net.business.basedata;

import com.net.business.beans.BeanManager;
import com.net.business.entity.Chapter;
import com.net.business.entity.Monster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BaseDataManager {
	private Logger logger = LoggerFactory.getLogger(BaseDataManager.class);
	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private static BaseDataManager instance;

	private List<Chapter> chapters=new ArrayList<>();

	public static BaseDataManager getInstance() {
		if (instance == null) {
			instance = new BaseDataManager();
		}
		return instance;
	}

	public void loadBaseData() {
		try {
			// 加上写锁
			readWriteLock.writeLock().lock();
			chapters=BeanManager.getInstance().getMongoTemplate().findAll(Chapter.class,Chapter.ENTITY_NAME);
			for (int i=0;i<chapters.size();i++){
				Chapter chapter=chapters.get(i);
				int total=0;
				List<Monster> monsters=chapter.getSpawns();
				for (int j = 0; j < monsters.size(); j++) {
					Monster monster=monsters.get(j);
					total+=monster.getCount();
				}
				chapter.setTotal(total);
			}
		} catch (Exception e) {
			logger.error("加载基础数据错误", e);
		} finally {
			// 释放写锁
			readWriteLock.writeLock().unlock();
		}
		logger.info("==============加载静态数据成功===============");
	}

	public Chapter getChapter(Integer idx) {
		try {
			readWriteLock.readLock().lock();
			return chapters.get(idx);
		} catch (Exception e) {
			logger.error("根据ID获取基础信息失败", e);
			return null;
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

}