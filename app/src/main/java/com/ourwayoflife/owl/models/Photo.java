package com.ourwayoflife.owl.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAutoGeneratedKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Zach on 6/5/17.
 */

@DynamoDBTable(tableName = "Photo")
public class Photo {
    private String mPhotoId;
    private String mUploadDate;
    private String mUserId;
    private String mPhoto;

    public Photo() {
    }


    @DynamoDBHashKey(attributeName = "PhotoId")
    @DynamoDBAutoGeneratedKey
    public String getPhotoId() {
        return mPhotoId;
    }

    public void setPhotoId(String photoId) {
        mPhotoId = photoId;
    }

    @DynamoDBAttribute(attributeName = "UserId")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "UserId-UploadDate-index", attributeName = "UserId")
    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    @DynamoDBAttribute(attributeName = "UploadDate")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "UserId-UploadDate-index", attributeName = "UploadDate")
    public String getUploadDate() {
        return mUploadDate;
    }

    public void setUploadDate(String uploadDate) {
        mUploadDate = uploadDate;
    }

    @DynamoDBAttribute(attributeName = "Photo")
    public String getPhoto() {
        return mPhoto;
    }

    public void setPhoto(String photo) {
        mPhoto = photo;
    }
}
