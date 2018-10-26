package com.net.engine.io;

import com.net.engine.io.filter.IFilterChain;

public abstract interface IFilterSupport {
	public abstract IFilterChain getPreFilterChain();

	public abstract IFilterChain getPostFilterChain();
}