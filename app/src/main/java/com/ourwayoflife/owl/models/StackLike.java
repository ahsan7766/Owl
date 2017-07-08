package com.ourwayoflife.owl.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Zach on 7/8/17.
 */

@DynamoDBTable(tableName = "StackLike")
public class StackLike {

    private String mStackId;
    private String mUserId;
    private String mLikeDate;


    public StackLike () {}


    @DynamoDBHashKey(attributeName = "StackId")
    public String getStackId() {
        return mStackId;
    }

    public void setStackId(String stackId) {
        mStackId = stackId;
    }

    @DynamoDBRangeKey(attributeName = "UserId")
    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    @DynamoDBAttribute(attributeName = "LikeDate")
    public String getLikeDate() {
        return mLikeDate;
    }

    public void setLikeDate(String likeDate) {
        mLikeDate = likeDate;
    }

}
