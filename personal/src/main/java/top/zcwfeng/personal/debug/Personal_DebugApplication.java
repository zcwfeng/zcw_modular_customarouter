package top.zcwfeng.personal.debug;

import android.app.Application;
import android.util.Log;

import top.zcwfeng.common.utils.Cons;

// TODO 注意：这是 测试环境下的代码 Debug
public class Personal_DebugApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(Cons.TAG, "personal/debug/Personal_DebugApplication");
    }
}
