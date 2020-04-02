/**
  * Copyright 2019 bejson.com 
  */
package com.besjon.pojo;
import java.util.Date;
import java.util.List;

/**
 * Auto-generated: 2019-08-02 23:45:45
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Prarms {

    private String par_sst;
    private boolean par_Aff;
    private Date par_date;
    private List<Date> dates;
    public void setPar_sst(String par_sst) {
         this.par_sst = par_sst;
     }
     public String getPar_sst() {
         return par_sst;
     }

    public void setPar_Aff(boolean par_Aff) {
         this.par_Aff = par_Aff;
     }
     public boolean getPar_Aff() {
         return par_Aff;
     }

    public void setPar_date(Date par_date) {
         this.par_date = par_date;
     }
     public Date getPar_date() {
         return par_date;
     }

    public void setDates(List<Date> dates) {
         this.dates = dates;
     }
     public List<Date> getDates() {
         return dates;
     }

}