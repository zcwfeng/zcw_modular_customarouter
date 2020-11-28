package top.zcwfeng.order.debug;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import top.zcwfeng.common.utils.Cons;

// TODO 同学们注意：这是 测试环境下的代码 Debug
public class Order_DebugBaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(Cons.TAG, "order/debug/Order_DebugBaseActivity");
    }
}
