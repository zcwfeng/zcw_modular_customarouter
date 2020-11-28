package top.zcwfeng.arouter_api;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

public class BundleManager {
    // TODO: 组件共享 call
    private Call call;

    // Intent传输  携带的值，保存到这里
    private Bundle bundle = new Bundle();

    public Call getCall() {
        return call;
    }

    public void setCall(Call call) {
        this.call = call;
    }

    public Bundle getBundle() {
        return bundle;
    }

    // TODO: 2020/11/21 继续增加Bundle 其他扩展类型
    // 对外界提供，可以携带参数的方法
    public BundleManager withString(@NonNull String key, @Nullable String value) {
        bundle.putString(key, value);
        return this; // 链式调用效果 模仿开源框架
    }

    public BundleManager withBoolean(@NonNull String key, @Nullable boolean value) {
        bundle.putBoolean(key, value);
        return this;
    }

    public BundleManager withInt(@NonNull String key, @Nullable int value) {
        bundle.putInt(key, value);
        return this;
    }

    public BundleManager withDouble(@NonNull String key, @Nullable double value) {
        bundle.putDouble(key, value);
        return this;
    }

    public BundleManager withFloat(@NonNull String key, @Nullable float value) {
        bundle.putFloat(key, value);
        return this;
    }

    public BundleManager withBundle(Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

    public BundleManager withSerializable(@NonNull String key, @Nullable Serializable object) {
        bundle.putSerializable(key, object);
        return this;
    }

    // 直接完成跳转
    public Object navigation(Context context){
        Log.e("zcw_arouter", "navigation 路由跳转");
        return RouterManager.getInstance().navigation(context, this);
    }

}
