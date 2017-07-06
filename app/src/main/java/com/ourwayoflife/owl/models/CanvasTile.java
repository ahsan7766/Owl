package com.ourwayoflife.owl.models;

/**
 * Created by Zach on 5/24/17.
 */

public class CanvasTile {

    private String mText;


    public CanvasTile() {

    }

    public CanvasTile(String text) {
        this.mText = text;
    }


    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }
}
