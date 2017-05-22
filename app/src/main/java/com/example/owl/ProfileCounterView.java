package com.example.owl;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Zach on 5/22/17.
 */

public class ProfileCounterView extends View {

    private int mHootCount;
    private int mFollowerCount;
    private int mFollowingCount;

    private Paint mTextPaint;

    /**
     * Interface definition for a callback to be invoked when the current
     * item changes.
     */
    public interface OnCurrentItemChangedListener {
        void OnCurrentItemChanged(ProfileCounterView source, int currentItem);
    }

    /**
     * Class constructor taking only a context. Use this constructor to create
     * {@link ProfileCounterView} objects from your own code.
     *
     * @param context
     */
    public ProfileCounterView(Context context) {
        super(context);
        init();
    }

    /**
     * Class constructor taking a context and an attribute set. This constructor
     * is used by the layout engine to construct a {@link ProfileCounterView} from a set of
     * XML attributes.
     *
     * @param context
     * @param attrs   An attribute set which can contain attributes from
     *                {@link com.example.owl.ProfileCounterView} as well as attributes inherited
     *                from {@link android.view.View}.
     */
    public ProfileCounterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ProfileCounterView,
                0, 0
        );

        try {
            // Retrieve the values from the TypedArray and store into
            // fields of this class.
            //
            // The R.styleable.BowlingFrame_* constants represent the index for
            // each custom attribute in the R.styleable.BowlingFrame array.
            /*
            mRoll1Score = a.getInt(R.styleable.BowlingFrame_roll1Score, -1);
            mRoll2Score = a.getInt(R.styleable.BowlingFrame_roll2Score, -1);
            mRoll3Score = a.getInt(R.styleable.BowlingFrame_roll3Score, -1);
            mRunningScore = a.getInt(R.styleable.BowlingFrame_runningScore, -1);
            mScoreTextWidth = a.getDimension(R.styleable.BowlingFrame_scoreTextWidth, 0.0f);
            mScoreTextHeight = a.getDimension(R.styleable.BowlingFrame_scoreTextHeight, 75);
            mScoreTextColor = a.getColor(R.styleable.BowlingFrame_scoreTextColor, Color.BLACK);
            mFrameNumber = a.getInt(R.styleable.BowlingFrame_frameNumber, 0);
            mFrameNumberTextWidth = a.getDimension(R.styleable.BowlingFrame_frameNumberTextWidth, 0.0f);
            mFrameNumberTextHeight = a.getDimension(R.styleable.BowlingFrame_frameNumberTextHeight, 200);
            mFrameNumberTextColor = a.getColor(R.styleable.BowlingFrame_frameNumberTextColor,  Color.parseColor("#66D3D3D3"));
            mBackgroundColor = a.getColor(R.styleable.BowlingFrame_backgroundColor, Color.TRANSPARENT);
            mFrameColor = a.getColor(R.styleable.BowlingFrame_frameColor, Color.BLACK);
            */
        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }

        init();
    }


    private Integer getHootCount() {
        return mHootCount;
    }

    public void setHootCount(Integer hootCount) {
        mHootCount = hootCount;
        invalidate();
    }

    private Integer getFollowerCount() {
        return mFollowerCount;
    }

    public void setFollowerCount(Integer followerCount) {
        mFollowerCount = followerCount;
        invalidate();
    }

    private Integer getFollowingCount() {
        return mFollowingCount;
    }

    public void setFollowingCount(Integer followingCount) {
        mFollowingCount = followingCount;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText(
                Integer.toString(getHootCount()),
                50,
                50,
                mTextPaint
        );

    }

    private void init() {

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(50);

    }

}
