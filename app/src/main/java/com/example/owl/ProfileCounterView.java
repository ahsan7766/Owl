package com.example.owl;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Zach on 5/22/17.
 */

public class ProfileCounterView extends View {

    private int mHootCount;
    private int mFollowerCount;
    private int mFollowingCount;

    private Paint mCountTextPaint;
    private Paint mTitleTextPaint;
    private Paint mLinePaint;

    private Rect mHootCountTextBounds;
    private Rect mFollowerCountTextBounds;
    private Rect mFollowingCountTextBounds;
    private Rect mHootTitleTextBounds;
    private Rect mFollowerTitleTextBounds;
    private Rect mFollowingTitleTextBounds;

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
            // The R.styleable.ProfileCounterView_* constants represent the index for
            // each custom attribute in the R.styleable.ProfileCounterView array.
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
            mHootCount = a.getInt(R.styleable.ProfileCounterView_hootCount, 0);
            mFollowingCount = a.getInt(R.styleable.ProfileCounterView_followingCount, 0);
            mFollowerCount = a.getInt(R.styleable.ProfileCounterView_followerCount, 0);
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

        // Outer border for visualization while testing
        /*
        canvas.drawRect(
                0,
                0,
                getWidth(),
                getHeight(),
                mLinePaint
        );
        */


        // First divider line
        canvas.drawLine(
                getWidth() / 3,
                0,
                getWidth() / 3,
                getHeight(),
                mLinePaint
        );

        // Second divider line
        canvas.drawLine(
                (getWidth() / 3) * 2,
                0,
                (getWidth() / 3) * 2,
                getHeight(),
                mLinePaint
        );

        // Hoot count text
        String hootCountString = getHootCount().toString();
        mCountTextPaint.getTextBounds(hootCountString, 0, hootCountString.length(), mHootCountTextBounds);
        canvas.drawText(
                hootCountString,
                (getWidth() / 6), // - mHootCountTextBounds.exactCenterX(),
                (getHeight() / 3), // - mHootCountTextBounds.exactCenterY(),
                mCountTextPaint
        );

        // Follower count text
        String followerCountString = getFollowerCount().toString();
        mCountTextPaint.getTextBounds(followerCountString, 0, followerCountString.length(), mFollowerCountTextBounds);
        canvas.drawText(
                followerCountString,
                ((getWidth() / 6) * 3), // - mFollowerCountTextBounds.exactCenterX(),
                (getHeight() / 3), // - mFollowerCountTextBounds.exactCenterY(),
                mCountTextPaint
        );

        // Following count text
        String followingCountString = getFollowingCount().toString();
        mCountTextPaint.getTextBounds(followingCountString, 0, followingCountString.length(), mFollowingCountTextBounds);
        canvas.drawText(
                followingCountString,
                ((getWidth() / 6) * 5), //- mFollowingCountTextBounds.exactCenterX(),
                (getHeight() / 3), // - mFollowingCountTextBounds.exactCenterY(),
                mCountTextPaint
        );

        // Hoots title text
        String hootTitleString = getResources().getString(R.string.Hoots);
        mTitleTextPaint.getTextBounds(hootTitleString, 0, hootTitleString.length(), mHootTitleTextBounds);
        canvas.drawText(
                hootTitleString,
                (getWidth() / 6), // - mHootTitleTextBounds.exactCenterX(),
                ((getHeight() / 8) * 7), // - mHootTitleTextBounds.exactCenterY(),
                mTitleTextPaint
        );

        // Followers title text
        String followerTitleString = getResources().getString(R.string.Followers);
        mTitleTextPaint.getTextBounds(followerTitleString, 0, followerTitleString.length(), mFollowerTitleTextBounds);
        canvas.drawText(
                followerTitleString,
                ((getWidth() / 6) * 3), // - mFollowerTitleTextBounds.exactCenterX(),
                ((getHeight() / 8) * 7), // - mFollowerTitleTextBounds.exactCenterY(),
                mTitleTextPaint
        );

        // Following title text
        String followingTitleString = getResources().getString(R.string.Following);
        mTitleTextPaint.getTextBounds(followingTitleString, 0, followingTitleString.length(), mFollowingTitleTextBounds);
        canvas.drawText(
                followingTitleString,
                ((getWidth() / 6) * 5), // - mFollowingTitleTextBounds.exactCenterX(),
                ((getHeight() / 8) * 7), // - mFollowingTitleTextBounds.exactCenterY(),
                mTitleTextPaint
        );
    }

    private void init() {

        mHootCountTextBounds = new Rect();
        mFollowerCountTextBounds = new Rect();
        mFollowingCountTextBounds = new Rect();
        mHootTitleTextBounds = new Rect();
        mFollowerTitleTextBounds = new Rect();
        mFollowingTitleTextBounds = new Rect();

        mCountTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCountTextPaint.setColor(Color.BLACK);
        mCountTextPaint.setTextAlign(Paint.Align.CENTER);
        mCountTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mCountTextPaint.setTextSize(50);

        mTitleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTitleTextPaint.setColor(Color.BLACK);
        mTitleTextPaint.setTextAlign(Paint.Align.CENTER);
        mTitleTextPaint.setTextSize(50);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(10);

    }

}
