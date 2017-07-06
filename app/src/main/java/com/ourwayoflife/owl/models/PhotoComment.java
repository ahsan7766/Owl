package com.ourwayoflife.owl.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Zach on 5/25/17.
 */

@DynamoDBTable(tableName = "PhotoComment")
public class PhotoComment {

    private String mCommentId;
    private String mPhotoId;
    private String mCommentDate;
    private String mUserId;
    private String mComment;


    public PhotoComment() {}


    @DynamoDBHashKey(attributeName = "CommentId")
    public String getCommentId() {
        return mCommentId;
    }

    public void setCommentId(String commentId) {
        mCommentId = commentId;
    }

    @DynamoDBAttribute(attributeName = "PhotoId")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "PhotoId-CommentDate-index", attributeName = "PhotoId")
    public String getPhotoId() {
        return mPhotoId;
    }

    public void setPhotoId(String photoId) {
        mPhotoId = photoId;
    }

    @DynamoDBAttribute(attributeName = "CommentDate")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "PhotoId-CommentDate-index", attributeName = "CommentDate")
    public String getCommentDate() {
        return mCommentDate;
    }

    public void setCommentDate(String commentDate) {
        mCommentDate = commentDate;
    }

    @DynamoDBAttribute(attributeName = "UserId")
    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    @DynamoDBAttribute(attributeName = "Comment")
    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {
        mComment = comment;
    }

}
