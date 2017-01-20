package sollian.com.soroundimageview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

/**
 * @author lishouxian on 2016/12/19.
 */

public class SoRoundImageHelper {
    public static final int SHAPE_RECTANGLE = 0;
    public static final int SHAPE_CIRCLE    = 1;

    public static final int DEFAULT_BOUND_COLOR = Color.WHITE;
    public static final int DEFAULT_CORNER_R5   = Util.dip2px(MyApplication.getInstance(), 5);
    public static final int DEFAULT_CORNER_R2   = Util.dip2px(MyApplication.getInstance(), 2);

    private int shape        = SHAPE_RECTANGLE;
    private int cornerRadius = DEFAULT_CORNER_R5;
    private int boundWidth;
    private int boundColor = DEFAULT_BOUND_COLOR;

    private final ImageView vImage;
    private final Paint     paint;
    private       Bitmap    bitmap;
    private       Drawable  drawable;

    public SoRoundImageHelper(ImageView imageView) {
        vImage = imageView;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(boundColor);
        paint.setStrokeWidth(boundWidth);
    }

    public void doDrawContent(Canvas canvas) {
        Drawable drawable = vImage.getDrawable();
        if (drawable == null) {
            return;
        }

        if (drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
            return;
        }

        if (!(drawable instanceof BitmapDrawable) && !(drawable instanceof ColorDrawable)) {
            Matrix matrix = getImageMatrix();
            if (matrix == null) {
                drawable.draw(canvas);
            } else {
                canvas.save();
                canvas.concat(matrix);
                drawable.draw(canvas);
                canvas.restore();
            }
            return;
        }

        /**
         * 不要使用canvas.getWidth方法，canvas的尺寸和控件尺寸未必相等！比如手动inflate的时候
         */
        Bitmap bmp;
        if (this.drawable == drawable && bitmap != null && !bitmap.isRecycled()) {
            bmp = bitmap;
        } else {
            int vW = vImage.getWidth();
            int vH = vImage.getHeight();
            Rect canvasBounds = configureCanvasBounds(vW, vH);

            this.drawable = drawable;

            bmp = getProcessedBmp(drawable, vW, vH, canvasBounds);
            if (bmp == null) {
                return;
            }
        }

        canvas.drawBitmap(bmp, 0, 0, null);
    }

    /**
     * 根据ScaleType确定canvas的最终绘制区域
     */
    private Rect configureCanvasBounds(int vW, int vH) {
        Rect rect = new Rect(0, 0, vW, vH);
        Drawable drawable = vImage.getDrawable();
        if (drawable instanceof ColorDrawable) {
            return rect;
        }
        ImageView.ScaleType scaleType = vImage.getScaleType();
        switch (scaleType) {
            case FIT_XY:
            case CENTER_CROP:
            case MATRIX:
                return rect;
            default:
                break;
        }

        Point size = getTrimedDrawableSize(vW, vH, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());

        int offsetX = 0;
        int offsetY = 0;
        switch (scaleType) {
            case FIT_CENTER:
            case CENTER_INSIDE:
                offsetX = (vW - size.x) / 2;
                offsetY = (vH - size.y) / 2;
                break;
            case FIT_END:
                offsetX = vW - size.x;
                offsetY = vH - size.y;
                break;
            default:
                break;
        }
        rect.left = offsetX;
        rect.top = offsetY;
        rect.right = offsetX + size.x;
        rect.bottom = offsetY + size.y;
        return rect;
    }

    /**
     * 根据ScaleType确定Drawable的最终尺寸
     */
    private Point getTrimedDrawableSize(int vW, int vH, int dW, int dH) {
        Point size = new Point(vW, vH);

        switch (vImage.getScaleType()) {
            case CENTER_INSIDE:
                if (dW > vW || dH > vH) {
                    float scaleW = (float) dW / vW;
                    float scaleH = (float) dH / vH;
                    float scale = Math.max(scaleW, scaleH);
                    size.set((int) (dW / scale), (int) (dH / scale));
                } else {
                    size.set(dW, dH);
                }
                break;
            case CENTER:
                size.set(Math.min(vW, dW), Math.min(vH, dH));
                break;
            case FIT_CENTER:
            case FIT_START:
            case FIT_END:
                float scaleW = (float) dW / vW;
                float scaleH = (float) dH / vH;
                float scale = Math.max(scaleW, scaleH);
                size.set((int) (dW / scale), (int) (dH / scale));
                break;
            default:
                break;
        }

        return size;
    }

    /**
     * 画图
     */
    private Bitmap getProcessedBmp(Drawable drawable, int width, int height, Rect canvasBounds) {
        if (width <= 0 || height <= 0) {
            return null;
        }

        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        } else {
            bitmap.eraseColor(Color.TRANSPARENT);
        }
        Canvas canvas = new Canvas(bitmap);

        //画遮罩层
        paint.setXfermode(null);
        paint.setColor(DEFAULT_BOUND_COLOR);
        drawBound(canvas, canvasBounds, true);

        //画图像层
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        if (drawable instanceof BitmapDrawable) {
            Bitmap b = ((BitmapDrawable) drawable).getBitmap();
            if (b == null) {
                return null;
            }
            if (vImage.getScaleType() != ImageView.ScaleType.FIT_XY) {
                Matrix matrix = getImageMatrix();
                if (matrix == null) {
                    canvas.drawBitmap(b, new Rect(0, 0, b.getWidth(), b.getHeight()),
                            new Rect(0, 0, width, height), paint);
                } else {
                    canvas.drawBitmap(b, matrix, paint);
                }
            } else {
                canvas.drawBitmap(b, new Rect(0, 0, b.getWidth(), b.getHeight()),
                        new Rect(0, 0, width, height), paint);
            }
        } else if (drawable instanceof ColorDrawable) {
            int color = ((ColorDrawable) drawable).getColor();
            canvas.drawColor(color, PorterDuff.Mode.SRC_IN);
        }

        //画描边层
        paint.setXfermode(null);
        paint.setColor(boundColor);
        drawBound(canvas, canvasBounds, false);

        return bitmap;
    }

    private void drawBound(Canvas canvas, Rect canvasBounds, boolean fill) {
        if (!fill && (boundWidth == 0 || boundColor == 0)) {
            return;
        }
        int outW = canvasBounds.width() - boundWidth;
        int outH = canvasBounds.height() - boundWidth;
        if (!fill) {
            paint.setStyle(Paint.Style.STROKE);
        }
        switch (shape) {
            case SHAPE_RECTANGLE:
                int halfStrokeW = boundWidth >> 1;
                RectF rect = new RectF(0, 0, outW, outH);
                rect.offset(canvasBounds.left + halfStrokeW, canvasBounds.top + halfStrokeW);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
                break;
            case SHAPE_CIRCLE:
                int outR = Math.min(outW, outH) / 2;
                canvas.drawCircle(canvasBounds.centerX(), canvasBounds.centerY(), outR,
                        paint);
                break;
            default:
                break;
        }

        paint.setStyle(Paint.Style.FILL);
    }

    /**
     * api<18 和 api>=18 getImageMatrix()的返回值不同，我们需要的是mDrawMatrix这个成员变量，所以分别实现
     */
    private Matrix getImageMatrix() {
        if (Build.VERSION.SDK_INT >= 18) {
            return vImage.getImageMatrix();
        } else {
            try {
                return (Matrix) Util.getFieldSafe(ImageView.class, "mDrawMatrix", vImage);
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    public int getShape() {
        return shape;
    }

    public void setShape(int shape) {
        this.shape = shape;
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    public int getBoundWidth() {
        return boundWidth;
    }

    public void setBoundWidth(int boundWidth) {
        this.boundWidth = boundWidth;
        paint.setStrokeWidth(boundWidth);
    }

    public int getBoundColor() {
        return boundColor;
    }

    public void setBoundColor(int boundColor) {
        this.boundColor = boundColor;
        paint.setColor(boundColor);
    }
}
