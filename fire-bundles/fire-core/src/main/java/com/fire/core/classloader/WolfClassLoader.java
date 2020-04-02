package com.fire.core.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public class WolfClassLoader extends URLClassLoader {

	public WolfClassLoader(URL[] urls, ClassLoader parent,
			URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
	}

	public WolfClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public WolfClassLoader(URL[] urls) {
		super(urls);
	}

	protected void add(URL url) {
		super.addURL(url);
	}

}
