package com.example.owl.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.example.owl.R;

/**
 * Created by Zach on 5/23/17.
 */

public class ProfilePictureView extends View {

    private Drawable mBackgroundDrawable;

    private Paint mOuterCirclePaint;
    private Paint mInnerCirclePaint;


    /**
     * Interface definition for a callback to be invoked when the current
     * item changes.
     */
    public interface OnCurrentItemChangedListener {
        void OnCurrentItemChanged(FeedCategoryView source, int currentItem);
    }

    /**
     * Class constructor taking only a context. Use this constructor to create
     * {@link ProfilePictureView} objects from your own code.
     *
     * @param context
     */
    public ProfilePictureView(Context context) {
        super(context);
        init();
    }

    /**
     * Class constructor taking a context and an attribute set. This constructor
     * is used by the layout engine to construct a {@link ProfilePictureView} from a set of
     * XML attributes.
     *
     * @param context
     * @param attrs   An attribute set which can contain attributes from
     *                {@link ProfilePictureView} as well as attributes inherited
     *                from {@link android.view.View}.
     */
    public ProfilePictureView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ProfilePictureView,
                0, 0
        );

        try {
            // Retrieve the values from the TypedArray and store into
            // fields of this class.
            //
            // The R.styleable.ProfilePictureView_* constants represent the index for
            // each custom attribute in the R.styleable.FeedCategoryView array.
            //mHeaderString = a.getString(R.styleable.ProfilePictureView_Header);
        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }

        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw profile picture
        int halfRect = getWidth() / 5;
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.dickbutt);
        drawable.setBounds(
                (getWidth() / 2) - halfRect,
                (getHeight() / 2) - halfRect,
                (getWidth() / 2) + halfRect,
                (getHeight() / 2) + halfRect
        );
        drawable.draw(canvas);


        // Outer circle
        canvas.drawCircle(
                getWidth() / 2,
                getHeight() / 2,
                getWidth() / 4,
                mOuterCirclePaint
        );

        // Inner circle

        canvas.drawCircle(
                getWidth() / 2,
                getHeight() / 2,
                getWidth() / 4,
                mInnerCirclePaint
        );

    }


    private void init() {

        mOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOuterCirclePaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        //mOuterCirclePaint.setColor(Color.BLUE);
        mOuterCirclePaint.setStyle(Paint.Style.STROKE);
        mOuterCirclePaint.setStrokeWidth(30);
        mOuterCirclePaint.setAlpha(200);

        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerCirclePaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        //mInnerCirclePaint.setColor(Color.RED);
        mInnerCirclePaint.setStyle(Paint.Style.STROKE);
        mInnerCirclePaint.setStrokeWidth(10);
        //mInnerCirclePaint.setAlpha(100);


    }
}
