package sollian.com.soroundimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author lishouxian on 2016/12/14.
 */

public class SoRoundImageView extends ImageView {
    private SoRoundImageHelper helper;

    public SoRoundImageView(Context context) {
        super(context);
        init();
    }

    public SoRoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SoRoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

        if (isInEditMode()) {
            return;
        }

        TypedArray typedArray = context
                .obtainStyledAttributes(attrs, R.styleable.SoRoundImageView, defStyleAttr, 0);
        helper.setShape(typedArray
                .getInt(R.styleable.SoRoundImageView_riv_shape,
                        SoRoundImageHelper.SHAPE_RECTANGLE));
        helper.setCornerRadius(typedArray
                .getDimensionPixelSize(R.styleable.SoRoundImageView_riv_cornerRadius,
                        SoRoundImageHelper.DEFAULT_CORNER_R5));
        int boundWidth = typedArray
                .getDimensionPixelSize(R.styleable.SoRoundImageView_riv_strokeWidth, 0);
        helper.setBoundColor(typedArray
                .getColor(R.styleable.SoRoundImageView_riv_strokeColor,
                        SoRoundImageHelper.DEFAULT_BOUND_COLOR));

        if (boundWidth <= 0) {
            boundWidth = 0;
        }
        helper.setBoundWidth(boundWidth);
        typedArray.recycle();
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }
        helper = new SoRoundImageHelper(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            super.onDraw(canvas);
        } else {
            helper.doDrawContent(canvas);
        }
    }

    public int getShape() {
        return helper.getShape();
    }

    public void setShape(int shape) {
        helper.setShape(shape);
        invalidate();
    }

    public int getCornerRadius() {
        return helper.getCornerRadius();
    }

    public void setCornerRadius(int cornerRadius) {
        helper.setCornerRadius(cornerRadius);
        invalidate();
    }

    public int getBoundWidth() {
        return helper.getBoundWidth();
    }

    public void setBoundWidth(int boundWidth) {
        helper.setBoundWidth(boundWidth);
        invalidate();
    }

    public int getBoundColor() {
        return helper.getBoundColor();
    }

    public void setBoundColor(int boundColor) {
        helper.setBoundColor(boundColor);
        invalidate();
    }
}
