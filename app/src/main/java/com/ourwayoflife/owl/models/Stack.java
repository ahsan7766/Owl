package com.ourwayoflife.owl.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAutoGeneratedKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Zach on 6/27/17.
 */

@DynamoDBTable(tableName = "Stack")
public class Stack {

    private String mStackId;
    private String mUserId;
    private String mCreatedDate;
    private String mName;

    public Stack() {
    }

    public Stack(String stackId, String userId, String createdDate, String name) {
        mStackId = stackId;
        mUserId = userId;
        mCreatedDate = createdDate;
        mName = name;
    }

    @DynamoDBHashKey(attributeName = "StackId")
    @DynamoDBAutoGeneratedKey
    public String getStackId() {
        return mStackId;
    }

    public void setStackId(String stackId) {
        mStackId = stackId;
    }

    @DynamoDBAttribute(attributeName = "UserId")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "UserId-CreatedDate-index", attributeName = "UserId")
    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    @DynamoDBAttribute(attributeName = "CreatedDate")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "UserId-CreatedDate-index", attributeName = "CreatedDate")
    public String getCreatedDate() {
        return mCreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        mCreatedDate = createdDate;
    }

    @DynamoDBAttribute(attributeName = "Name")
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}
