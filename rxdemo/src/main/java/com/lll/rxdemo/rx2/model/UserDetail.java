package com.lll.rxdemo.rx2.model;

/**
 * Created by amitshekhar on 04/02/17.
 */

public class UserDetail {

    public long id;
    public String firstname;
    public String lastname;

    @Override
    public String toString() {
        return "UserDetail{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                '}';
    }
}
