package com.ourwayoflife.owl.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.models.CanvasTile;

/**
 * Created by Zach on 7/10/17.
 */

public class CanvasTileView extends View {

    private static final String TAG = CanvasTile.class.getName();

    private String mName;
    private Bitmap mPhoto;

    private Paint mOutlinePaint;
    private Paint mNameTextFillPaint;
    private Paint mNameTextStrokePaint;
    //private Paint mSubheaderTextPaint;


    private TextPaint mTextPaintNameFill;
    private TextPaint mTextPaintNameStroke;

    private StaticLayout mStaticFill;
    private StaticLayout mStaticStroke;

    private Rect mOutlineRect;
    private Rect mNameTextBounds;
    //private Rect mSubheaderTextBounds;

    /**
     * Interface definition for a callback to be invoked when the current
     * item changes.
     */
    public interface OnCurrentItemChangedListener {
        void OnCurrentItemChanged(CanvasTileView source, int currentItem);
    }

    /**
     * Class constructor taking only a context. Use this constructor to create
     * {@link CanvasTileView} objects from your own code.
     *
     * @param context
     */
    public CanvasTileView(Context context) {
        super(context);
        init();
    }

    /**
     * Class constructor taking a context and an attribute set. This constructor
     * is used by the layout engine to construct a {@link CanvasTileView} from a set of
     * XML attributes.
     *
     * @param context
     * @param attrs   An attribute set which can contain attributes from
     *                {@link CanvasTileView} as well as attributes inherited
     *                from {@link android.view.View}.
     */
    public CanvasTileView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CanvasTileView,
                0, 0
        );

        try {
            // Retrieve the values from the TypedArray and store into
            // fields of this class.
            //
            // The R.styleable.CanvasTileView_* constants represent the index for
            // each custom attribute in the R.styleable.CanvasTileView array.
            String photoString = a.getString(R.styleable.CanvasTileView_photo);
            if(photoString != null) {
                try {
                    byte[] encodeByte = Base64.decode(photoString, Base64.DEFAULT);
                    mPhoto = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                } catch (Exception e) {
                    Log.e(TAG, "Conversion from String to Bitmap: " + e.getMessage());
                }
            } else {
                // TODO set the photo to the default photo for "Not found"
            }

            mName = a.getString(R.styleable.CanvasTileView_name);
        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }

        init();
    }

    public Bitmap getPhoto() {
        return mPhoto;
    }

    public void setPhoto(Bitmap photo) {
        mPhoto = photo;
    }

    private String getName() {
        return mName == null ?  "" : mName;
    }

    public void setName(String headerString) {
        mName = headerString;
        invalidate();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);



        // Amount of space given for profile picture, name, ect...
        final int bottomSpace = 0;

        int length = Math.min(getWidth(), getHeight() - bottomSpace);

        /*
        mOutlineRect.set((getWidth() - length) / 2,
                (getHeight() - length) / 2,
                ((getWidth() - length) / 2) + length,
                ((getHeight() - length) / 2) + length);
        */

        mOutlineRect.set((getWidth() - length) / 2,
                (getHeight() - bottomSpace - length) / 2,
                ((getWidth() - length) / 2) + length,
                ((getHeight() - bottomSpace - length) / 2) + length);


        // Draw border around entire canvas for testing purposes
        //canvas.drawRect(0,0,getWidth(),getHeight(),mOutlinePaint);

        if(getPhoto() != null) {
            canvas.drawBitmap(getPhoto(), null, mOutlineRect, mOutlinePaint);
        }



        // Temp outline of border
        canvas.drawRect(
                mOutlineRect,
                mOutlinePaint
        );


        /*
        // Name text
        mNameTextFillPaint.getTextBounds(getName(), 0, getName().length(), mNameTextBounds);
        canvas.drawText(
                getName(),
                (getWidth() / 2), //- mNameTextBounds.exactCenterX(),
                ((getHeight() / 8) * 3), // - mNameTextBounds.exactCenterY(),
                mNameTextFillPaint
        );

        mNameTextStrokePaint.getTextBounds(getName(), 0, getName().length(), mNameTextBounds);
        canvas.drawText(
                getName(),
                (getWidth() / 2), //- mNameTextBounds.exactCenterX(),
                ((getHeight() / 8) * 3), // - mNameTextBounds.exactCenterY(),
                mNameTextStrokePaint
        );
        */


        canvas.save();
        canvas.translate(getWidth() / 2, (getHeight() / 8) * 3);

        //StaticLayout staticLayoutFill = new StaticLayout(getName(), mTextPaintNameFill, length, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        mStaticFill = new StaticLayout(getName(), mTextPaintNameFill, length, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        mStaticFill.draw(canvas);

        mStaticStroke = new StaticLayout(getName(), mTextPaintNameStroke, length, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        mStaticStroke.draw(canvas);

        canvas.restore();

        /*
        // Sub-header text
        String postCountString = getPhotoCount().toString() + " " + getResources().getString(R.string.photos);
        mSubheaderTextPaint.getTextBounds(postCountString, 0, postCountString.length(), mSubheaderTextBounds);
        canvas.drawText(
                postCountString,
                (getWidth() / 2), // - mNameTextBounds.exactCenterX(),
                ((getHeight() / 8) * 5), // - mNameTextBounds.exactCenterY(),
                mSubheaderTextPaint
        );
        */


    }


    private void init() {

        mOutlineRect = new Rect();
        mNameTextBounds = new Rect();
        //mSubheaderTextBounds = new Rect();

        mOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutlinePaint.setColor(Color.BLACK);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setAlpha(110);


        mNameTextFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNameTextFillPaint.setStyle(Paint.Style.FILL);
        mNameTextFillPaint.setColor(Color.WHITE);
        mNameTextFillPaint.setTextAlign(Paint.Align.CENTER);
        mNameTextFillPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mNameTextFillPaint.setTextSize(50);

        mNameTextStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNameTextStrokePaint.setStyle(Paint.Style.STROKE);
        mNameTextStrokePaint.setStrokeWidth(0.6f);
        mNameTextStrokePaint.setColor(Color.BLACK);
        mNameTextStrokePaint.setTextAlign(Paint.Align.CENTER);
        mNameTextStrokePaint.setTypeface(Typeface.DEFAULT_BOLD);
        mNameTextStrokePaint.setTextSize(50);


        mTextPaintNameFill = new TextPaint(mNameTextFillPaint);

        mTextPaintNameStroke = new TextPaint(mNameTextStrokePaint);


        int length = Math.min(getWidth(), getHeight());
        mStaticFill = new StaticLayout(getName(), mTextPaintNameFill, length, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        mStaticStroke = new StaticLayout(getName(), mTextPaintNameStroke, length, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        /*
        mSubheaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSubheaderTextPaint.setColor(Color.WHITE);
        mSubheaderTextPaint.setTextAlign(Paint.Align.CENTER);
        mSubheaderTextPaint.setTextSize(30);
        */
    }
}
