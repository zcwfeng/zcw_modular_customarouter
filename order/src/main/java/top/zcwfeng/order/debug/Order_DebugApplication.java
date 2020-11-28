package top.zcwfeng.order.debug;

import android.app.Application;
import android.util.Log;

import top.zcwfeng.common.utils.Cons;

// TODO 同学们注意：这是 测试环境下的代码 Debug
public class Order_DebugApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(Cons.TAG, "order/debug/Order_DebugApplication");
    }
}
