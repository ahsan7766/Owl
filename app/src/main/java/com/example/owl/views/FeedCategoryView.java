package com.example.owl.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.example.owl.R;

/**
 * Created by Zach on 5/23/17.
 */

public class FeedCategoryView extends View {

    private Drawable mBackgroundDrawable;

    private String mHeaderString;
    private int mPostCount;

    private Paint mOutlinePaint;
    private Paint mHeaderTextPaint;
    private Paint mSubheaderTextPaint;

    private Rect mOutlineRect;
    private Rect mHeaderTextBounds;
    private Rect mSubheaderTextBounds;

    /**
     * Interface definition for a callback to be invoked when the current
     * item changes.
     */
    public interface OnCurrentItemChangedListener {
        void OnCurrentItemChanged(FeedCategoryView source, int currentItem);
    }

    /**
     * Class constructor taking only a context. Use this constructor to create
     * {@link FeedCategoryView} objects from your own code.
     *
     * @param context
     */
    public FeedCategoryView(Context context) {
        super(context);
        init();
    }

    /**
     * Class constructor taking a context and an attribute set. This constructor
     * is used by the layout engine to construct a {@link FeedCategoryView} from a set of
     * XML attributes.
     *
     * @param context
     * @param attrs   An attribute set which can contain attributes from
     *                {@link FeedCategoryView} as well as attributes inherited
     *                from {@link android.view.View}.
     */
    public FeedCategoryView(Context context, AttributeSet attrs) {
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
            // The R.styleable.FeedCategoryView_* constants represent the index for
            // each custom attribute in the R.styleable.FeedCategoryView array.
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
            mHeaderString = a.getString(R.styleable.FeedCategoryView_Header);
            mPostCount = a.getInt(R.styleable.FeedCategoryView_postCount, 0);
        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }

        init();
    }

    private String getHeader() {
        return mHeaderString;
    }

    public void setHeader(String headerString) {
        mHeaderString = headerString;
        invalidate();
    }

    private Integer getPostCount() {
        return mPostCount;
    }

    public void setPostCount(Integer postCount) {
        mPostCount = postCount;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int length = Math.min(getWidth(), getHeight());

        mOutlineRect.set((getWidth() - length) / 2,
                (getHeight() - length) / 2,
                ((getWidth() - length) / 2) + length,
                ((getHeight() - length) / 2) + length);
        // Temp outline of border
        canvas.drawRect(
                (getWidth() - length) / 2,
                (getHeight() - length) / 2,
                ((getWidth() - length) / 2) + length,
                ((getHeight() - length) / 2) + length,
                mOutlinePaint
        );


        // Header text
        mHeaderTextPaint.getTextBounds(mHeaderString, 0, mHeaderString.length(), mHeaderTextBounds);
        canvas.drawText(
                getHeader(),
                (getWidth() / 2), //- mHeaderTextBounds.exactCenterX(),
                ((getHeight() / 8) * 3), // - mHeaderTextBounds.exactCenterY(),
                mHeaderTextPaint
        );

        // Sub-header text
        String postCountString = getPostCount().toString() + " " + getResources().getString(R.string.posts);
        mSubheaderTextPaint.getTextBounds(postCountString, 0, postCountString.length(), mSubheaderTextBounds);
        canvas.drawText(
                postCountString,
                (getWidth() / 2), // - mHeaderTextBounds.exactCenterX(),
                ((getHeight() / 8) * 5), // - mHeaderTextBounds.exactCenterY(),
                mSubheaderTextPaint
        );


    }


    private void init() {

        mOutlineRect = new Rect();
        mHeaderTextBounds = new Rect();
        mSubheaderTextBounds = new Rect();

        mOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutlinePaint.setColor(Color.BLACK);
        mOutlinePaint.setStyle(Paint.Style.STROKE);

        mHeaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHeaderTextPaint.setColor(Color.BLACK);
        mHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        mHeaderTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mHeaderTextPaint.setTextSize(50);

        mSubheaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSubheaderTextPaint.setColor(Color.BLACK);
        mSubheaderTextPaint.setTextAlign(Paint.Align.CENTER);
        mSubheaderTextPaint.setTextSize(25);

    }
}
