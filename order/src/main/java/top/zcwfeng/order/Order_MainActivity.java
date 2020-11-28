package top.zcwfeng.order;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import top.zcwfeng.arouter_annotation.ARouter;
import top.zcwfeng.arouter_api.RouterManager;
import top.zcwfeng.common.utils.Cons;


@ARouter(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity_main);

        Log.e(Cons.TAG, "order/Order_MainActivity");
    }

    public void jumpApp(View view) {
//        Toast.makeText(this, "路由还没有写好呢，别猴急...", Toast.LENGTH_SHORT).show();

        RouterManager.getInstance()
                .build("/app/MainActivity")
                .withString("name", "李元霸")
                .withString("sex", "男")
                .withInt("age", 99)
                .navigation(this);
    }

    public void jumpPersonal(View view) {
//        Toast.makeText(this, "路由还没有写好呢，别猴急...", Toast.LENGTH_SHORT).show();

        RouterManager.getInstance()
                .build("/personal/Personal_MainActivity")
                .withString("name", "李元霸")
                .withString("sex", "男")
                .withInt("age", 99)
                .navigation(this);
    }
}
