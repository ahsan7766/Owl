package com.example.owl.models;

import java.util.Date;

/**
 * Created by Zach on 5/25/17.
 */

public class Comment {

    private User mUser;
    private String mText;
    private Date mDate;


    public Comment () {}


    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }
}
