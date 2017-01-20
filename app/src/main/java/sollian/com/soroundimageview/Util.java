package sollian.com.soroundimageview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.lang.reflect.Field;

/**
 * @author sollian on 2017/1/20.
 */

public class Util {
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Nullable
    public static Object getFieldSafe(Class<?> clazz, String fieldName, Object target) {
        try {
            return getField(clazz, target, fieldName);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object getField(Class<?> clazz, Object target, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        if (TextUtils.isEmpty(fieldName)) {
            return null;
        }
        Field field = clazz.getDeclaredField(fieldName);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        return field.get(target);
    }
}
