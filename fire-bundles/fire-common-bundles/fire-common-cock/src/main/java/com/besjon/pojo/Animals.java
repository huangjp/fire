/**
  * Copyright 2019 bejson.com 
  */
package com.besjon.pojo;
import java.util.List;

/**
 * Auto-generated: 2019-08-02 23:45:45
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Animals {

    private List<Dog> dog;
    private Cat cat;
    public void setDog(List<Dog> dog) {
         this.dog = dog;
     }
     public List<Dog> getDog() {
         return dog;
     }

    public void setCat(Cat cat) {
         this.cat = cat;
     }
     public Cat getCat() {
         return cat;
     }

}