package top.zcwfeng.arouter_api;

import android.app.Activity;
import android.util.Log;
import android.util.LruCache;

public class ParameterManager {

    public static ParameterManager instance;

    public static ParameterManager getInstance(){
        if(instance == null) {
            synchronized (ParameterManager.class) {
                if (instance == null) {
                    instance = new ParameterManager();
                }
            }

        }
        return instance;
    }

    // key=类名 value=参数加载的接口
    private LruCache<String,ParameterGet> cache;
    // 为什么还要拼接，此次拼接 是为了寻找他
    static final String FILE_SUFFIX_NAME = "$$Parameter"; // 为了这个效果：Order_MainActivity + $$Parameter

    public ParameterManager() {
        cache = new LruCache<>(100);
    }

    /**
     * 使用者使用这个方法用来参数接收
     * @param activity
     */
    public void loadParameter(Activity activity){
        String classame = activity.getClass().getName();
        ParameterGet parameterLoad = cache.get(classame);
        if(null == parameterLoad) {
            try {
                Class<?> clazz = Class.forName(classame+FILE_SUFFIX_NAME);
                parameterLoad = (ParameterGet) clazz.newInstance();
                cache.put(classame, parameterLoad);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.e("zcw_arouter", "loadParameter 参数接收");
        parameterLoad.getParameter(activity);
    }


}
