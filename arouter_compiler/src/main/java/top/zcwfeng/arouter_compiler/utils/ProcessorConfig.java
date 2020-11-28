package top.zcwfeng.arouter_compiler.utils;
// @ARouter注解 的 包名 + 类名
public interface ProcessorConfig {
    String AROUTER_PACKAGE = "top.zcwfeng.arouter_annotation.ARouter";
    String OPTIONS = "moduleName";//目的是接收 每个module名称
    String APT_PACKAGE= "packageNameForAPT";//目的是接收 包名（APT 存放的包名）

    String PARAMETER_PACKAGE= "top.zcwfeng.arouter_annotation.Parameter";
    // String全类名
    String STRING_PACKAGE = "java.lang.String";

    // Activity全类名
    String ACTIVITY_PACKAGE = "android.app.Activity";

    // ARouter api 包名
    String AROUTER_API_PACKAGE = "top.zcwfeng.arouter_api";

    // ARouter api 的 ParameterGet 高层标准
    String AROUTER_API_PARAMETER_GET = AROUTER_API_PACKAGE + ".ParameterGet";

    // ARouter api 的 ARouterGroup 高层标准
    String AROUTER_API_GROUP = AROUTER_API_PACKAGE + ".ARouterGroup";

    // ARouter api 的 ARouterPath 高层标准
    String AROUTER_API_PATH = AROUTER_API_PACKAGE + ".ARouterPath";

    // 路由组，中的 Path 里面的 方法名
    String PATH_METHOD_NAME = "getPathMap";

    // 路由组，中的 Group 里面的 方法名
    String GROUP_METHOD_NAME = "getGroupMap";

    // ARouter aip 的 ParmeterGet 的 生成文件名称 $$Parameter
    String PARAMETER_FILE_NAME = "$$Parameter";

    // ARouter api 的 ParameterGet 方法参数的名字
    String PARAMETER_NAME = "targetParameter";

    // ARouter api 的 ParmeterGet 方法的名字
    String PARAMETER_METHOD_NAME = "getParameter";

    // String全类名
    public static final String STRING = "java.lang.String";

    // 路由组，中的 Path 里面 的 变量名 1
    String PATH_VAR1 = "pathMap";

    // 路由组，中的 Group 里面 的 变量名 1
    String GROUP_VAR1 = "groupMap";

    // 路由组，PATH 最终要生成的 文件名
    String PATH_FILE_NAME = "ARouter$$Path$$";

    // 路由组，GROUP 最终要生成的 文件名
    String GROUP_FILE_NAME = "ARouter$$Group$$";

    // 组件共享，ARouter api 的 Call 高层标准
    String CALL = AROUTER_API_PACKAGE + ".Call";

    // RouterManager类名  生成 RouterManager.getInstance()
    String ROUTER_MANAGER = "RouterManager";
}
