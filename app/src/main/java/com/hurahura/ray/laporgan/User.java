package com.hurahura.ray.laporgan;

public class User {
    private String id;
    private String email;
    private String name;

    User(String i, String e, String n) {
        this.id = i;
        this.email = e;
        this.name = n;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
