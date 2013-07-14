package com.shs.framework.dao;


public abstract class IEventHandler {
	public abstract boolean before(Event e);
	public abstract void after(Event e);
	protected Object extraParam;
	public Object getExtraParam() {
		return extraParam;
	}
	public IEventHandler setExtraParam(Object extraParam) {
		this.extraParam = extraParam;
		return this;
	}
}
