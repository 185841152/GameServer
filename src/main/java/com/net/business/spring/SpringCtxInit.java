package com.net.business.spring;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * spring容器初始化类
 * @author kevin
 *
 */
public class SpringCtxInit {
	final static Logger LOGGER = LoggerFactory.getLogger(SpringCtxInit.class);
	/**
	 * 系统应用spring环境
	 */
	private static ApplicationContext ctx;

	/**
	 * 单实例对象
	 */
	private static SpringCtxInit instance = null;

	private SpringCtxInit() {
		init();
	}

	/**
	 * 获得单实例对象
	 * 
	 * @return
	 */
	public static synchronized SpringCtxInit instance() {
		if (instance == null) {
			instance = new SpringCtxInit();
		}
		return instance;
	}

	/**
	 * 初始化Spring组件
	 */
	public void init() {
		loadContextXML();
	}

	/**
	 * 加载spring对象
	 * 
	 * @param props
	 */
	private void loadContextXML() {
		try {
			ctx = new ClassPathXmlApplicationContext("classpath:spring/applicationContext.xml");
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("init spring config fail.");
		}

	}

	/**
	 * 得到一个spring的配置对象
	 * 
	 * @param name
	 * @return
	 */
	public Object getBean(String name) {
		return ctx.getBean(name);
	}

	/**
	 * 获取单个信息
	 * 
	 * @param key
	 * @param object
	 * @param request
	 * @return
	 */
	public String getMessage(String key, Object[] object, Locale locale) {
		return ctx.getMessage(key, object, locale);
	}
}
