package com.lll.rxdemo.rx2.model;

/**
 * Created by amitshekhar on 27/08/16.
 */
public class User {
    public long id;
    public String firstname;
    public String lastname;
    public boolean isFollowing;

    public User() {
    }

    public User(ApiUser apiUser) {
        this.id = apiUser.id;
        this.firstname = apiUser.firstname;
        this.lastname = apiUser.lastname;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", isFollowing=" + isFollowing +
                '}';
    }
}
