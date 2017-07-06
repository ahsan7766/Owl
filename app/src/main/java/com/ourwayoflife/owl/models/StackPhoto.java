package com.ourwayoflife.owl.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Zach on 6/30/17.
 */

@DynamoDBTable(tableName="StackPhoto")
public class StackPhoto {

    private String mStackId;
    private String mPhotoId;
    private String mAddedDate;



    public StackPhoto() {

    }

    public StackPhoto(String stackId, String photoId, String addedDate) {
        mStackId = stackId;
        mPhotoId = photoId;
        mAddedDate = addedDate;
    }



    @DynamoDBHashKey(attributeName = "StackId")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "StackId-AddedDate-index", attributeName = "StackId")
    public String getStackId() {
        return mStackId;
    }

    public void setStackId(String stackId) {
        mStackId = stackId;
    }

    @DynamoDBRangeKey(attributeName = "PhotoId")
    public String getPhotoId() {
        return mPhotoId;
    }

    public void setPhotoId(String photoId) {
        mPhotoId = photoId;
    }

    @DynamoDBAttribute(attributeName = "AddedDate")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "StackId-AddedDate-index", attributeName = "AddedDate")
    public String getAddedDate() {
        return mAddedDate;
    }

    public void setAddedDate(String addedDate) {
        mAddedDate = addedDate;
    }
}
