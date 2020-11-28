package top.zcwfeng.order.impl;

import top.zcwfeng.arouter_annotation.ARouter;
import top.zcwfeng.common.order.user.BaseUser;
import top.zcwfeng.common.order.user.IUser;
import top.zcwfeng.order.model.UserInfo;
@ARouter(path = "/order/getUserInfo")
public class OrderUserImpl implements IUser {
    @Override
    public BaseUser getUserInfo() {

        UserInfo userInfo = new UserInfo();
        userInfo.setName("Zcw");
        userInfo.setAccount("154325354");
        userInfo.setPassword("1234567890");
        userInfo.setVipLevel(999);
        return userInfo;
    }
}
