package com.example.common.model;

import java.io.Serializable;

/**
 * 用户类
 */
public class User implements Serializable {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
