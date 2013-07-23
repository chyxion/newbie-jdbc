package com.shs.framework.dao;

/**
 * @version 0.1
 * @author chyxion
 * @describe: 
 * @date created: Jul 23, 2013 10:27:55 AM
 * @support: chyxion@163.com
 * @date modified: 
 * @modified by: 
 */
public abstract class IEventHandler {
	/**
	 * 操作数据库前置调用，如果返回false，将不继续执行
	 * @param e
	 * @return
	 */
	public abstract boolean before(Event e);
	/**
	 * 操作数据库后置调用
	 * @param e
	 */
	public abstract void after(Event e);
	/**
	 * 附加参数
	 */
	protected Object extraParam;
	public Object getExtraParam() {
		return extraParam;
	}
	public IEventHandler setExtraParam(Object extraParam) {
		this.extraParam = extraParam;
		return this;
	}
}
