package com.net.business.beans;

import com.net.business.beans.redis.RedisOperate;
import com.net.business.spring.SpringCtxUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service("beanManager")
public class BeanManager {

	@Autowired
	RedisOperate redisOperate;
	@Autowired
	MongoTemplate mongoTemplate;
	

	static BeanManager instance = null;

	public static BeanManager getInstance() {
		if (instance == null) {
			instance = (BeanManager) SpringCtxUtil.getBean("beanManager");
		}
		return instance;
	}

	public RedisOperate getRedisOperate() {
		return redisOperate;
	}

	public MongoTemplate getMongoTemplate() {
		return mongoTemplate;
	}

	public void setMongoTemplate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
}
