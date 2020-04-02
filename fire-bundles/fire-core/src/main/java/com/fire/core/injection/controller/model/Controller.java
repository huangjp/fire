package com.fire.core.injection.controller.model;

import java.util.Map;

/**
 * 控制类实体
 * 
 * @author Administrator
 *
 */
public class Controller {

	private Path path;
	
	private Map<Path, ControllerMethod> methods;
	
	private Class<?> c;

	public Controller(Path path, Map<Path, ControllerMethod> methods) {
		super();
		this.path = path;
		this.methods = methods;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public Map<Path, ControllerMethod> getMethods() {
		return methods;
	}

	public void setMethods(Map<Path, ControllerMethod> methods) {
		this.methods = methods;
	}

	public Class<?> getC() {
		return c;
	}

	public void setC(Class<?> c) {
		this.c = c;
	}
}
