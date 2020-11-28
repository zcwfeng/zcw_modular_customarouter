package top.zcwfeng.personal;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import top.zcwfeng.arouter_annotation.ARouter;
import top.zcwfeng.arouter_annotation.Parameter;
import top.zcwfeng.arouter_api.ParameterManager;
import top.zcwfeng.arouter_api.RouterManager;
import top.zcwfeng.common.bean.Student;
import top.zcwfeng.common.order.drawable.OrderDrawable;
import top.zcwfeng.common.order.net.OrderAddress;
import top.zcwfeng.common.utils.Cons;

@ARouter(path = "/personal/Personal_MainActivity")
public class Personal_MainActivity extends AppCompatActivity {

    @Parameter
    String name;
    @Parameter
    String sex;
    @Parameter
    int age;

    @Parameter
    Student student;

    @Parameter(name = "/order/getDrawable")
    OrderDrawable orderDrawable;
    @Parameter(name = "/order/getOrderBean")
    OrderAddress orderAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_activity_main);

        // 模仿ButterKnife bind(this);
        ParameterManager.getInstance().loadParameter(this);
        Log.e("zcw_arouter", "Personal 模块获取组件共享");

        ((ImageView)findViewById(R.id.image)).setImageResource(orderDrawable.getDrawable());
        // 输出 Student
        Log.e(Cons.TAG, "我的Personal onCreate 对象的传递:" +  student.toString());

        new Thread(new Runnable(){

            @Override
            public void run() {
                try {
                    orderAddress.getOrderBean("aa205eeb45aa76c6afe3c52151b52160", "144.34.161.97");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void jumpApp(View view) {
//        Toast.makeText(this, "路由还没有写好呢，别猴急...", Toast.LENGTH_SHORT).show();

        // 使用我们自己写的路由 跳转交互
        RouterManager.getInstance()
                .build("/app/MainActivity")
                .withString("name", "杜子腾")
                .navigation(this); // 组件和组件通信

    }

    public void jumpOrder(View view) {
//        Toast.makeText(this, "路由还没有写好呢，别猴急...", Toast.LENGTH_SHORT).show();

        // 使用我们自己写的路由 跳转交互
        RouterManager.getInstance()
                .build("/order/Order_MainActivity")
                .withString("name", "杜子腾")
                .navigation(this); // 组件和组件通信

    }

}

