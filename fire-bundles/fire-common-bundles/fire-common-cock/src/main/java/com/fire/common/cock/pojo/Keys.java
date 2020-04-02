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
public class Keys {

    private String col;
    private String name;
    private List<String> roles;
    private List<Prarms> prarms;

    public void setCol(String col) {
        this.col = col;
    }

    public String getCol() {
        return col;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setPrarms(List<Prarms> prarms) {
        this.prarms = prarms;
    }

    public List<Prarms> getPrarms() {
        return prarms;
    }

}