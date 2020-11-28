package top.zcwfeng.order.impl;

import top.zcwfeng.arouter_annotation.ARouter;
import top.zcwfeng.common.order.drawable.OrderDrawable;
import top.zcwfeng.order.R;

// TODO: 组件共享 3 暴露接口
@ARouter(path = "/order/getDrawable")
public class OrderDrawableImpl implements OrderDrawable {
    @Override
    public int getDrawable() {
        return R.drawable.ic_ac_unit_black_24dp;
    }
}
