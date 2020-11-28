package top.zcwfeng.order.model;

import top.zcwfeng.common.order.user.BaseUser;

// 相当于是图片资源 丢了一个Bean进来
public class UserInfo extends BaseUser {

    private String token;
    private int vipLevel;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }
}
