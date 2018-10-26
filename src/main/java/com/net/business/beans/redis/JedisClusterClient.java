package com.net.business.beans.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.business.util.ObjectUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisException;

/**
 * 集群方式redis客户端操作
 * 
 */
//@Service
public class JedisClusterClient implements IJedisClient {

	private Logger logger = LoggerFactory.getLogger(JedisClusterClient.class);

//	@Autowired
	private JedisCluster jedisCluster;
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#get(java.lang.String)
	 */
	@Override
	public String get(String key) {
		String value = null;
		try {
			if (jedisCluster.exists(key)) {
				value = jedisCluster.get(key);
				value = StringUtils.isNotBlank(value) && !"nil".equalsIgnoreCase(value) ? value : null;
				logger.debug("get {} = {}", key, value);
			}
		} catch (Exception e) {
			logger.warn("get {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#getObject(java.lang.String)
	 */
	@Override
	public Object getObject(String key) {
		Object value = null;
		try {
			if (jedisCluster.exists(getBytesKey(key))) {
				value = toObject(jedisCluster.get(getBytesKey(key)));
				logger.debug("getObject {} = {}", key, value);
			}
		} catch (Exception e) {
			logger.warn("getObject {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
			// try {
			// jedisCluster.close();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#set(java.lang.String,
	 * java.lang.String, int)
	 */
	@Override
	public String set(String key, String value, int cacheSeconds) {
		String result = null;

		try {

			result = jedisCluster.set(key, value);
			if (cacheSeconds != 0) {
				jedisCluster.expire(key, cacheSeconds);
			}
			logger.debug("set {} = {}", key, value);
		} catch (Exception e) {
			logger.warn("set {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#setObject(java.lang.String,
	 * java.lang.Object, int)
	 */
	@Override
	public String setObject(String key, Object value, int cacheSeconds) {
		String result = null;

		try {
			result = jedisCluster.set(getBytesKey(key), toBytes(value));
			if (cacheSeconds != 0) {
				jedisCluster.expire(key, cacheSeconds);
			}
			logger.debug("setObject {} = {}", key, value);
		} catch (Exception e) {
			logger.warn("setObject {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#getList(java.lang.String)
	 */
	@Override
	public List<String> getList(String key) {
		List<String> value = null;
		try {
			if (jedisCluster.exists(key)) {
				value = jedisCluster.lrange(key, 0, -1);
				logger.debug("getList {} = {}", key, value);
			}
		} catch (Exception e) {
			logger.warn("getList {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jeeplus.common.redis.IJedisClient#getObjectList(java.lang.String)
	 */
	@Override
	public List<Object> getObjectList(String key) {
		List<Object> value = null;
		try {
			if (jedisCluster.exists(getBytesKey(key))) {
				List<byte[]> list = jedisCluster.lrange(getBytesKey(key), 0, -1);
				value = new ArrayList<Object>();
				for (byte[] bs : list) {
					value.add(toObject(bs));
				}
				logger.debug("getObjectList {} = {}", key, value);
			}
		} catch (Exception e) {
			logger.warn("getObjectList {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#setList(java.lang.String,
	 * java.util.List, int)
	 */
	@Override
	public long setList(String key, List<String> value, int cacheSeconds) {
		long result = 0;
		try {
			if (jedisCluster.exists(key)) {
				jedisCluster.del(key);
			}
			result = jedisCluster.rpush(key, (String[]) value.toArray());
			if (cacheSeconds != 0) {
				jedisCluster.expire(key, cacheSeconds);
			}
			logger.debug("setList {} = {}", key, value);
		} catch (Exception e) {
			logger.warn("setList {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jeeplus.common.redis.IJedisClient#setObjectList(java.lang.String,
	 * java.util.List, int)
	 */
	@Override
	public long setObjectList(String key, List<Object> value, int cacheSeconds) {
		long result = 0;
		try {
			if (jedisCluster.exists(getBytesKey(key))) {
				jedisCluster.del(key);
			}
			List<byte[]> list = new ArrayList<byte[]>();
			for (Object o : value) {
				list.add(toBytes(o));
			}
			result = jedisCluster.rpush(getBytesKey(key), (byte[][]) list.toArray());
			if (cacheSeconds != 0) {
				jedisCluster.expire(key, cacheSeconds);
			}
			logger.debug("setObjectList {} = {}", key, value);
		} catch (Exception e) {
			logger.warn("setObjectList {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#listAdd(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public long listAdd(String key, String... value) {
		long result = 0;
		try {
			result = jedisCluster.rpush(key, value);
			logger.debug("listAdd {} = {}", key, value);
		} catch (Exception e) {
			logger.warn("listAdd {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jeeplus.common.redis.IJedisClient#listObjectAdd(java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public long listObjectAdd(String key, Object... value) {
		long result = 0;
		try {
			List<byte[]> list = new ArrayList<byte[]>();
			for (Object o : value) {
				list.add(toBytes(o));
			}
			result = jedisCluster.rpush(getBytesKey(key), (byte[][]) list.toArray());
			logger.debug("listObjectAdd {} = {}", key, value);
		} catch (Exception e) {
			logger.warn("listObjectAdd {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#getSet(java.lang.String)
	 */
	@Override
	public Set<String> getSet(String key) {
		Set<String> value = null;
		try {
			if (jedisCluster.exists(key)) {
				value = jedisCluster.smembers(key);
				logger.debug("getSet {} = {}", key, value);
			}
		} catch (Exception e) {
			logger.warn("getSet {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#getObjectSet(java.lang.String)
	 */
	@Override
	public Set<Object> getObjectSet(String key) {
		Set<Object> value = null;
		try {
			if (jedisCluster.exists(getBytesKey(key))) {
				value = new HashSet<Object>();
				Set<byte[]> set = jedisCluster.smembers(getBytesKey(key));
				for (byte[] bs : set) {
					value.add(toObject(bs));
				}
				logger.debug("getObjectSet {} = {}", key, value);
			}
		} catch (Exception e) {
			logger.warn("getObjectSet {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#setSet(java.lang.String,
	 * java.util.Set, int)
	 */
	@Override
	public long setSet(String key, Set<String> value, int cacheSeconds) {
		long result = 0;
		try {
			if (jedisCluster.exists(key)) {
				jedisCluster.del(key);
			}
			result = jedisCluster.sadd(key, (String[]) value.toArray());
			if (cacheSeconds != 0) {
				jedisCluster.expire(key, cacheSeconds);
			}
			logger.debug("setSet {} = {}", key, value);
		} catch (Exception e) {
			logger.warn("setSet {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#setObjectSet(java.lang.String,
	 * java.util.Set, int)
	 */
	@Override
	public long setObjectSet(String key, Set<Object> value, int cacheSeconds) {
		long result = 0;
		try {
			if (jedisCluster.exists(getBytesKey(key))) {
				jedisCluster.del(key);
			}
			Set<byte[]> set = new HashSet<byte[]>();
			for (Object o : value) {
				set.add(toBytes(o));
			}
			result = jedisCluster.sadd(getBytesKey(key), (byte[][]) set.toArray());
			if (cacheSeconds != 0) {
				jedisCluster.expire(key, cacheSeconds);
			}
			logger.debug("setObjectSet {} = {}", key, value);
		} catch (Exception e) {
			logger.warn("setObjectSet {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#setSetAdd(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public long setSetAdd(String key, String... value) {
		long result = 0;
		try {
			result = jedisCluster.sadd(key, value);
			logger.debug("setSetAdd {} = {}", key, value);
		} catch (Exception e) {
			logger.warn("setSetAdd {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jeeplus.common.redis.IJedisClient#setSetObjectAdd(java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public long setSetObjectAdd(String key, Object... value) {
		long result = 0;
		try {
			Set<byte[]> set = new HashSet<byte[]>();
			for (Object o : value) {
				set.add(toBytes(o));
			}
			result = jedisCluster.rpush(getBytesKey(key), (byte[][]) set.toArray());
			logger.debug("setSetObjectAdd {} = {}", key, value);
		} catch (Exception e) {
			logger.warn("setSetObjectAdd {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#getMap(java.lang.String)
	 */
	@Override
	public Map<String, String> getMap(String key) {
		Map<String, String> value = null;
		try {
			if (jedisCluster.exists(key)) {
				value = jedisCluster.hgetAll(key);
				logger.debug("getMap {} = {}", key, value);
			}
		} catch (Exception e) {
			logger.warn("getMap {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#getObjectMap(java.lang.String)
	 */
	@Override
	public Map<String, Object> getObjectMap(String key) {
		Map<String, Object> value = null;
		try {
			if (jedisCluster.exists(getBytesKey(key))) {
				value = new HashMap<String,Object>();
				Map<byte[], byte[]> map = jedisCluster.hgetAll(getBytesKey(key));
				for (Map.Entry<byte[], byte[]> e : map.entrySet()) {
					value.put(new String(e.getKey()), toObject(e.getValue()));
				}
				logger.debug("getObjectMap {} = {}", key, value);
			}
		} catch (Exception e) {
			logger.warn("getObjectMap {} = {}", key, value, e);
		} finally {
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#setMap(java.lang.String,
	 * java.util.Map, int)
	 */
	@Override
	public String setMap(String key, Map<String, String> value, int cacheSeconds) {
		String result = null;
		try {
			if (jedisCluster.exists(key)) {
				jedisCluster.del(key);
			}
			result = jedisCluster.hmset(key, value);
			if (cacheSeconds != 0) {
				jedisCluster.expire(key, cacheSeconds);
			}
			logger.debug("setMap {} = {}", key, value);
		} catch (Exception e) {
			logger.warn("setMap {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#setObjectMap(java.lang.String,
	 * java.util.Map, int)
	 */
	@Override
	public String setObjectMap(String key, Map<String, Object> value, int cacheSeconds) {
		String result = null;
		try {
			if (jedisCluster.exists(getBytesKey(key))) {
				jedisCluster.del(key);
			}
			Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();
			for (Map.Entry<String, Object> e : value.entrySet()) {
				map.put(getBytesKey(e.getKey()), toBytes(e.getValue()));
			}
			result = jedisCluster.hmset(getBytesKey(key), (Map<byte[], byte[]>) map);
			if (cacheSeconds != 0) {
				jedisCluster.expire(key, cacheSeconds);
			}
			logger.debug("setObjectMap {} = {}", key, value);
		} catch (Exception e) {
			logger.warn("setObjectMap {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#mapPut(java.lang.String,
	 * java.util.Map)
	 */
	@Override
	public String mapPut(String key, Map<String, String> value) {
		String result = null;
		try {
			result = jedisCluster.hmset(key, value);
			logger.debug("mapPut {} = {}", key, value);
		} catch (Exception e) {
			logger.warn("mapPut {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#mapObjectPut(java.lang.String,
	 * java.util.Map)
	 */
	@Override
	public String mapObjectPut(String key, Map<String, Object> value) {
		String result = null;
		try {
			Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();
			for (Map.Entry<String, Object> e : value.entrySet()) {
				map.put(getBytesKey(e.getKey()), toBytes(e.getValue()));
			}
			result = jedisCluster.hmset(getBytesKey(key), (Map<byte[], byte[]>) map);
			logger.debug("mapObjectPut {} = {}", key, value);
		} catch (Exception e) {
			logger.warn("mapObjectPut {} = {}", key, value, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#mapRemove(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public long mapRemove(String key, String mapKey) {
		long result = 0;
		try {
			result = jedisCluster.hdel(key, mapKey);
			logger.debug("mapRemove {}  {}", key, mapKey);
		} catch (Exception e) {
			logger.warn("mapRemove {}  {}", key, mapKey, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jeeplus.common.redis.IJedisClient#mapObjectRemove(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public long mapObjectRemove(String key, String mapKey) {
		long result = 0;
		try {
			result = jedisCluster.hdel(getBytesKey(key), getBytesKey(mapKey));
			logger.debug("mapObjectRemove {}  {}", key, mapKey);
		} catch (Exception e) {
			logger.warn("mapObjectRemove {}  {}", key, mapKey, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#mapExists(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean mapExists(String key, String mapKey) {
		boolean result = false;
		try {
			result = jedisCluster.hexists(key, mapKey);
			logger.debug("mapExists {}  {}", key, mapKey);
		} catch (Exception e) {
			logger.warn("mapExists {}  {}", key, mapKey, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jeeplus.common.redis.IJedisClient#mapObjectExists(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean mapObjectExists(String key, String mapKey) {
		boolean result = false;
		try {
			result = jedisCluster.hexists(getBytesKey(key), getBytesKey(mapKey));
			logger.debug("mapObjectExists {}  {}", key, mapKey);
		} catch (Exception e) {
			logger.warn("mapObjectExists {}  {}", key, mapKey, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#del(java.lang.String)
	 */
	@Override
	public long del(String key) {
		long result = 0;
		try {
			if (jedisCluster.exists(key)) {
				result = jedisCluster.del(key);
				logger.debug("del {}", key);
			} else {
				logger.debug("del {} not exists", key);
			}
		} catch (Exception e) {
			logger.warn("del {}", key, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#delObject(java.lang.String)
	 */
	@Override
	public long delObject(String key) {
		long result = 0;
		try {
			if (jedisCluster.exists(getBytesKey(key))) {
				result = jedisCluster.del(getBytesKey(key));
				logger.debug("delObject {}", key);
			} else {
				logger.debug("delObject {} not exists", key);
			}
		} catch (Exception e) {
			logger.warn("delObject {}", key, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#exists(java.lang.String)
	 */
	@Override
	public boolean exists(String key) {
		boolean result = false;
		try {
			result = jedisCluster.exists(key);
			logger.debug("exists {}", key);
		} catch (Exception e) {
			logger.warn("exists {}", key, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jeeplus.common.redis.IJedisClient#existsObject(java.lang.String)
	 */
	@Override
	public boolean existsObject(String key) {
		boolean result = false;
		try {
			result = jedisCluster.exists(getBytesKey(key));
			logger.debug("existsObject {}", key);
		} catch (Exception e) {
			logger.warn("existsObject {}", key, e);
		} finally {
			// returnResource(jedis);
		}
		return result;
	}

	/**
	 * 获取byte[]类型Key
	 * 
	 * @param key
	 * @return
	 */
	public static byte[] getBytesKey(Object object) {
		if (object instanceof String) {
			return ((String) object).getBytes();
		} else {
			return ObjectUtils.serialize(object);
		}
	}

	/**
	 * Object转换byte[]类型
	 * 
	 * @param key
	 * @return
	 */
	public static byte[] toBytes(Object object) {
		return ObjectUtils.serialize(object);
	}

	/**
	 * byte[]型转换Object
	 * 
	 * @param key
	 * @return
	 */
	public static Object toObject(byte[] bytes) {
		return ObjectUtils.unserialize(bytes);
	}

	@Override
	public boolean constailsSetMember(String key, String member) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void incrementKey(String key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Jedis getResource() throws JedisException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void returnResource(Jedis jedis) {
		// TODO Auto-generated method stub
		
	}

}