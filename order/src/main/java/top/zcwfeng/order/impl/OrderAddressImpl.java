package top.zcwfeng.order.impl;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import top.zcwfeng.arouter_annotation.ARouter;
import top.zcwfeng.common.order.net.OrderAddress;
import top.zcwfeng.common.order.net.OrderBean;
import top.zcwfeng.order.services.OrderServices;
@ARouter(path = "/order/getOrderBean")
public class OrderAddressImpl implements OrderAddress {
    private final static String BASE_URL = "http://apis.juhe.cn/";

    @Override
    public OrderBean getOrderBean(String key, String ip) throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .build();
        OrderServices host = retrofit.create(OrderServices.class);
        Call<ResponseBody> call = host.get(ip, key);
        retrofit2.Response<ResponseBody> response = call.execute();
        if(response !=null && response.body() != null){
            JSONObject jsonObject = JSON.parseObject(response.body().toString());
            OrderBean orderBean = jsonObject.toJavaObject(OrderBean.class);
            Log.e("zcw_arouter","Order 订单独特网络请求功能解析，结果》》" + orderBean.toString());
            return orderBean;
        }
        return null;
    }
}
