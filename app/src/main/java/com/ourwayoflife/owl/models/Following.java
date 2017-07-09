package com.ourwayoflife.owl.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Zach on 7/9/17.
 */

@DynamoDBTable(tableName = "Following")
public class Following {

    private String mUserId;
    private String mFollowingId;
    private String mFollowDate;

    public Following() {}


    @DynamoDBHashKey(attributeName = "UserId")
    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    @DynamoDBRangeKey(attributeName = "FollowingId")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "FollowingId-FollowDate-index", attributeName = "FollowingId")
    public String getFollowingId() {
        return mFollowingId;
    }

    public void setFollowingId(String followingId) {
        mFollowingId = followingId;
    }

    @DynamoDBAttribute(attributeName = "FollowDate")
    @DynamoDBIndexRangeKey(localSecondaryIndexName = "UserId-FollowDate-index", globalSecondaryIndexName = "FollowingId-FollowDate-index", attributeName = "FollowDate")
    public String getFollowDate() {
        return mFollowDate;
    }

    public void setFollowDate(String followDate) {
        mFollowDate = followDate;
    }
}
