package top.zcwfeng.customarouter.base;

import top.zcwfeng.common.RecordPathManager;
import top.zcwfeng.common.base.BaseApplication;
import top.zcwfeng.customarouter.MainActivity;
import top.zcwfeng.order.Order_MainActivity;
import top.zcwfeng.personal.Personal_MainActivity;

public class Application extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        RecordPathManager.joinGroup("app", "MainActivity", MainActivity.class);
        RecordPathManager.joinGroup("order", "Order_MainActivity", Order_MainActivity.class);

        RecordPathManager.joinGroup("personal", "Personal_MainActivity", Personal_MainActivity.class);

    }
}
