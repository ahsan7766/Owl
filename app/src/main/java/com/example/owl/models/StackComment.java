package com.example.owl.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Zach on 6/30/17.
 */

@DynamoDBTable(tableName = "StackComment")
public class StackComment {

    private String mCommentId;
    private String mStackId;
    private String mCommentDate;
    private String mUserId;
    private String mComment;


    public StackComment() {}


    @DynamoDBHashKey(attributeName = "CommentId")
    public String getCommentId() {
        return mCommentId;
    }

    public void setCommentId(String commentId) {
        mCommentId = commentId;
    }

    @DynamoDBAttribute(attributeName = "StackId")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "StackId-CommentDate-index", attributeName = "StackId")
    public String getStackId() {
        return mStackId;
    }

    public void setStackId(String stackId) {
        mStackId = stackId;
    }

    @DynamoDBAttribute(attributeName = "CommentDate")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "StackId-CommentDate-index", attributeName = "CommentDate")
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
