package com.example.owl.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.example.owl.R;

/**
 * Created by Zach on 5/23/17.
 */

public class ProfilePictureView extends View {


    private int mBackgroundPicture;

    private Bitmap mBitmap;

    private Paint mOuterCirclePaint;
    private Paint mInnerCirclePaint;


    /**
     * Interface definition for a callback to be invoked when the current
     * item changes.
     */
    public interface OnCurrentItemChangedListener {
        void OnCurrentItemChanged(FeedItemView source, int currentItem);
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
            mBackgroundPicture = a.getInt(R.styleable.ProfilePictureView_backgroundPicture, -1);
        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }

        init();
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        invalidate();
    }

    public int getBackgroundPicture() {
        return mBackgroundPicture;
    }

    public void setBackgroundPicture(int backgroundPicture) {
        mBackgroundPicture = backgroundPicture;
        mBitmap = BitmapFactory.decodeResource(getContext().getResources(), getBackgroundPicture());
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw square of perimeter for testing
        /*
        canvas.drawRect(
                0,
                0,
                getWidth(),
                getHeight(),
                mInnerCirclePaint
        );
        */

        int circleRadius = (Math.min(getWidth(), getHeight()) / 2);
        circleRadius -= circleRadius / 10;
        circleRadius -= 22;


        // Draw profile picture
        // If getBackgroundPicture is -1, then it wasn't set, so don't draw picture
        if(getBackgroundPicture() != -1) {
            //int halfRect =  ((16 * circleRadius) / 9) - 22; // The size of the bitmap profile picture
            int halfRect = circleRadius * 2;

            //mBitmap = BitmapFactory.decodeResource(getContext().getResources(), getBackgroundPicture());

            // Crop bitmap to be size of circle
            mBitmap = getResizedBitmap(mBitmap, halfRect, halfRect);

            canvas.drawBitmap(getCroppedBitmap(mBitmap),
                    (getWidth() / 2) - (mBitmap.getWidth() / 2),
                    getHeight() / 2 - (mBitmap.getHeight() / 2),
                    mInnerCirclePaint
            );
        }


        /*
        mBackgroundPicture = ContextCompat.getDrawable(getContext(), R.drawable.trees);
        mBackgroundPicture.setBounds(
                (getWidth() / 2) - halfRect,
                (getHeight() / 2) - halfRect,
                (getWidth() / 2) + halfRect,
                (getHeight() / 2) + halfRect
        );
        mBackgroundPicture.draw(canvas);
        */

        mOuterCirclePaint.setStrokeWidth(circleRadius / 3);
        mInnerCirclePaint.setStrokeWidth(circleRadius / 8);

        // Outer circle
        canvas.drawCircle(
                getWidth() / 2,
                getHeight() / 2,
                circleRadius,
                mOuterCirclePaint
        );

        // Inner circle

        canvas.drawCircle(
                getWidth() / 2,
                getHeight() / 2,
                circleRadius,
                mInnerCirclePaint
        );

    }


    private void init() {


        mBitmap = BitmapFactory.decodeResource(getContext().getResources(), getBackgroundPicture());


        mOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOuterCirclePaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        //mOuterCirclePaint.setColor(Color.BLUE);
        mOuterCirclePaint.setStyle(Paint.Style.STROKE);
        mOuterCirclePaint.setStrokeWidth(22);
        mOuterCirclePaint.setAlpha(140);

        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerCirclePaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        //mInnerCirclePaint.setColor(Color.RED);
        mInnerCirclePaint.setStyle(Paint.Style.STROKE);
        mInnerCirclePaint.setStrokeWidth(10);
        //mInnerCirclePaint.setAlpha(100);

    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public static Bitmap getClip(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }


    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }
}
