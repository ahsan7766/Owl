package com.ourwayoflife.owl.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Zach on 7/8/17.
 */

@DynamoDBTable(tableName = "PhotoLike")
public class PhotoLike {

    private String mPhotoId;
    private String mUserId;
    private String mLikeDate;


    public PhotoLike () {}


    @DynamoDBHashKey(attributeName = "PhotoId")
    public String getPhotoId() {
        return mPhotoId;
    }

    public void setPhotoId(String photoId) {
        mPhotoId = photoId;
    }

    @DynamoDBRangeKey(attributeName = "UserId")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "UserId-LikeDate-index", attributeName = "UserId")
    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    @DynamoDBAttribute(attributeName = "LikeDate")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "UserId-LikeDate-index", attributeName = "LikeDate")
    public String getLikeDate() {
        return mLikeDate;
    }

    public void setLikeDate(String likeDate) {
        mLikeDate = likeDate;
    }
}
