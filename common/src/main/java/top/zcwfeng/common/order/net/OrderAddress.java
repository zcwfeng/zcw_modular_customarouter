package top.zcwfeng.common.order.net;

import java.io.IOException;

import top.zcwfeng.arouter_api.Call;

public
interface OrderAddress extends Call {

    OrderBean getOrderBean(String key,String ip) throws IOException;
}
