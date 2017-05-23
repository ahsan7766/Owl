package com.example.owl.models;

/**
 * Created by Zach on 5/23/17.
 */

public class FeedCategory {

    private String mHeader;
    private int mPostCount;

    public FeedCategory() {
    }

    public FeedCategory(String header, int postCount) {
        mHeader = header;
        mPostCount = postCount;
    }

    public String getHeader() {
        return mHeader;
    }

    public void setHeader(String header) {
        mHeader = header;
    }

    public int getPostCount() {
        return mPostCount;
    }

    public void setPostCount(int postCount) {
        mPostCount = postCount;
    }
}
