package com.fire.core.thread;


import com.fire.core.entiy.UserLoginType;

import javax.security.auth.Subject;

/**
 * 自定义线程上下文，用于WEB用户的控制
 * 
 * @author Administrator
 *
 */
public class WolfWebContext {

	private String sessionId;

	private String userId;

	private Subject subject;

	private boolean isSqlSessionTransactional;

	private UserLoginType loginType;

	WolfWebContext() {
		super();
	}

	public WolfWebContext(String sessionId, String userId, Subject subject, UserLoginType loginType) {
		this();
		this.sessionId = sessionId;
		this.userId = userId;
		this.subject = subject;
		this.loginType = loginType;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public UserLoginType getLoginType() {
		return loginType;
	}

	public void setLoginType(UserLoginType loginType) {
		this.loginType = loginType;
	}

	public boolean isSqlSessionTransactional() {
		return isSqlSessionTransactional;
	}

	public void setSqlSessionTransactional(boolean isSqlSessionTransactional) {
		this.isSqlSessionTransactional = isSqlSessionTransactional;
	}

	@Override
	public String toString() {
		return "WolfWebContext [sessionId=" + sessionId + ", userId=" + userId + ", subject=" + subject
				+ ", isSqlSessionTransactional=" + isSqlSessionTransactional + ", loginType=" + loginType + "]";
	}

}
