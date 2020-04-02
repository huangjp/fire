/**
 * Copyright 2019 bejson.com
 */
package com.fire.common.cock.pojo;

import java.util.List;

/**
 * Auto-generated: 2019-08-02 23:45:45
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class JsonRootBean {

    private List<Employees> employees;
    private List<Integer> age;
    private List<String> strs;
    private Config config;
    private String username;

    public void setEmployees(List<Employees> employees) {
        this.employees = employees;
    }

    public List<Employees> getEmployees() {
        return employees;
    }

    public void setAge(List<Integer> age) {
        this.age = age;
    }

    public List<Integer> getAge() {
        return age;
    }

    public void setStrs(List<String> strs) {
        this.strs = strs;
    }

    public List<String> getStrs() {
        return strs;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}