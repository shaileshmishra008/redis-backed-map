package com.example.redis.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DemoBean implements Serializable {
    String name = "";
    int age;

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "DemoBean{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", prefs=" + prefs +
                '}';
    }

    public List getPrefs() {
        return prefs;
    }

    List prefs = new ArrayList();

    public DemoBean(String name, int age, List prefs) {
        this.name = name;
        this.age = age;
        this.prefs = prefs;
    }

    public DemoBean(){

    }
}
