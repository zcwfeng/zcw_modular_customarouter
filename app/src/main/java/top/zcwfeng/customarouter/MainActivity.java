package top.zcwfeng.customarouter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import top.zcwfeng.arouter_annotation.ARouter;
import top.zcwfeng.arouter_annotation.Parameter;
import top.zcwfeng.arouter_api.ParameterManager;
import top.zcwfeng.arouter_api.RouterManager;
import top.zcwfeng.common.bean.Student;
import top.zcwfeng.common.order.drawable.OrderDrawable;
import top.zcwfeng.common.order.user.IUser;
import top.zcwfeng.common.utils.Cons;

@ARouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Parameter(name = "/order/getDrawable")
    OrderDrawable orderDrawable;
    @Parameter(name = "/order/getUserInfo")
    IUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (BuildConfig.isRelease) {
            Log.e(Cons.TAG, "当前为：集成化模式，除app可运行，其他子模块都是Android Library");
        } else {
            Log.e(Cons.TAG, "当前为：组件化模式，app/order/personal子模块都可独立运行");
        }

        ParameterManager.getInstance().loadParameter(this);
        ((ImageView)findViewById(R.id.image)).setImageResource(orderDrawable.getDrawable());

        Log.e("zcw_arouter", "app 模块获取user--->" + user.toString());

    }

   public void jumpOrder(View view){
//       Intent intent = new Intent(this, Order_MainActivity.class);
//       intent.putExtra("name", "DAVID");
//       startActivity(intent);

       // 使用我们自己写的路由 跳转交互
       RouterManager.getInstance()
               .build("/order/Order_MainActivity")
               .withString("name", "杜子腾")
               .navigation(this); // 组件和组件通信
   }

    public void jumpPersonal(View view) {
        // 以前是这样跳转
        /*Intent intent = new Intent(this, Personal_MainActivity.class);
        intent.putExtra("name", "David");
        startActivity(intent);*/

// 现在是这样跳转  目前还要写这么多代码，是不是非常累

        // TODO 最终的成效：用户 一行代码搞定，同时还可以传递参数，同时还可以懒加载

//        ARouter$$Group$$personal group$$personal = new ARouter$$Group$$personal();
//        Map<String, Class<? extends ARouterPath>> groupMap = group$$personal.getGroupMap();
//        Class<? extends ARouterPath> myClass = groupMap.get("personal");
//
//        try {
//            ARouter$$Path$$personal path = (ARouter$$Path$$personal) myClass.newInstance();
//            Map<String, RouterBean> pathMap = path.getPathMap();
//            RouterBean bean = pathMap.get("/personal/Personal_MainActivity");
//
//            if (bean != null) {
//                Intent intent = new Intent(this, bean.getMyclass());
//                startActivity(intent);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        Student student = new Student("结束了", "男", 99);

        RouterManager.getInstance().build("/personal/Personal_MainActivity")
                .withString("name", "使真想")
                .withString("sex", "man")
                .withInt("age", 18)
                .withSerializable("student",student)
                .navigation(this);
    }
}