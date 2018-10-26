package com.net.business.spring;

/**
 * spring容器实例操作工具类
 * @author kevin
 *
 */
public class SpringCtxUtil {
	/**
	 * spring管理类实例
	 */
	private static SpringCtxInit sctx = SpringCtxInit.instance();

	/**
	 * 得到一个系统配置 bean
	 * 
	 * @param name
	 *            bean的配置名称
	 * @return 如果系统没有加载返回 null
	 */
	public static Object getBean(String name) {
		return sctx.getBean(name);
	}
}
