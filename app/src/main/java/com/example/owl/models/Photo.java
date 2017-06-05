package com.example.owl.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Zach on 6/5/17.
 */

@DynamoDBTable(tableName = "Photo")
public class Photo {
    private String PhotoId;
    private String UploadDate;


    public Photo() {
    }


    @DynamoDBHashKey(attributeName = "PhotoId")
    public String getPhotoId() {
        return PhotoId;
    }

    public void setPhotoId(String photoId) {
        PhotoId = photoId;
    }

    @DynamoDBRangeKey(attributeName = "UploadDate")
    public String getUploadDate() {
        return UploadDate;
    }

    public void setUploadDate(String uploadDate) {
        UploadDate = uploadDate;
    }
}
