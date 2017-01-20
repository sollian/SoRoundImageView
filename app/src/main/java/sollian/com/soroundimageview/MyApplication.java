package sollian.com.soroundimageview;

import android.app.Application;

/**
 * @author sollian on 2017/1/20.
 */

public class MyApplication extends Application {
    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Application getInstance() {
        return instance;
    }
}
