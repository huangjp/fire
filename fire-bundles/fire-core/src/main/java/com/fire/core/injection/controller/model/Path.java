package com.fire.core.injection.controller.model;

import com.fire.core.annotation.RequestMethod;

/**
 * 控制类请求路径封装
 * 
 * @author Administrator
 *
 */
public class Path {

	private String path;
	
	private RequestMethod method;

	public Path(String path, RequestMethod method) {
		super();
		this.path = path;
		this.method = method;
	}

	public Path(String path) {
		super();
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public RequestMethod getMethod() {
		return method;
	}

	public void setMethod(RequestMethod method) {
		this.method = method;
	}

	@Override
	public String toString() {
		return "Path [path=" + path + ", method=" + method + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Path other = (Path) obj;
		if (method != other.method)
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

}
