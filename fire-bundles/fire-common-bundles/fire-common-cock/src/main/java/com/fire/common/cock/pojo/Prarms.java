/**
 * Copyright 2019 bejson.com
 */
package com.fire.common.cock.pojo;

import java.util.Date;
import java.util.List;

/**
 * Auto-generated: 2019-08-02 23:45:45
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Prarms {

    private String parSst;
    private boolean parAff;
    private Date parDate;
    private List<Date> dates;

    public boolean isParAff() {
        return parAff;
    }

    public void setParAff(boolean parAff) {
        this.parAff = parAff;
    }

    public String getParSst() {
        return parSst;
    }

    public Date getParDate() {
        return parDate;
    }

    public void setParDate(Date parDate) {
        this.parDate = parDate;
    }

    public void setParSst(String parSst) {
        this.parSst = parSst;
    }

    public void setDates(List<Date> dates) {
        this.dates = dates;
    }

    public List<Date> getDates() {
        return dates;
    }

}