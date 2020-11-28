package top.zcwfeng.arouter_compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import top.zcwfeng.arouter_annotation.ARouter;
import top.zcwfeng.arouter_annotation.bean.RouterBean;
import top.zcwfeng.arouter_compiler.utils.ProcessorConfig;
import top.zcwfeng.arouter_compiler.utils.ProcessorUtils;

/**
 * 编码此类，记住就是一个字（细心，细心，细心），出了问题debug真的不好调试
 */

// AutoService则是固定的写法，加个注解即可
// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor.class)
// 允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({ProcessorConfig.AROUTER_PACKAGE})
// 指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)
// 注解处理器接收的参数
@SupportedOptions({ProcessorConfig.OPTIONS, ProcessorConfig.APT_PACKAGE})
public class ArouterProcessor extends AbstractProcessor {
    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementTool;
    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;
    // Message用来打印 日志相关信息
    private Messager messager;
    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;
    // 各个模块传递过来的模块名 例如：app order personal ---- 这个配合Gradle配置
    private String options;
    // 各个模块传递过来的目录 用于统一存放 apt生成的文件
    private String aptPackage;

    // 仓库1，Path 缓存 -> Map<"personal",Listt<RouteBean>>
    private Map<String, List<RouterBean>> mAllPathMaps = new HashMap<>();

    // 仓库2，Graoup 缓存 -> Map<"personal","ARouter$$personal.class">
    private Map<String, String> mAllGroupMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        elementTool = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        typeTool = processingEnv.getTypeUtils();

        // 只有接受到 App壳 传递过来的数据，才能证明我们的 APT环境搭建完成
        options = processingEnv.getOptions().get(ProcessorConfig.OPTIONS);
        aptPackage = processingEnv.getOptions().get(ProcessorConfig.APT_PACKAGE);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>init options:" + options);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>init aptPackage:" + aptPackage);
        if (options != null && aptPackage != null) {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境搭建完成....");
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境有问题，请检查 options 与 aptPackage 为null...");
        }


    }

    /**
     * 相当于main函数，开始处理注解
     * 注解处理器的核心方法，处理具体的注解，生成Java文件
     *
     * @param annotations 使用了支持处理注解的节点集合
     * @param roundEnv    当前或是之前的运行环境,可以通过该对象查找的注解。
     * @return true 表示后续处理器不会再处理（已经处理完成）
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (annotations.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "未发现@Arouter注解的地方");
            return false;
        }

        // TODO: 2020/11/28 1.增加组件共享 call ,我们除了annotation还可以直接获取接口,可以抽取到外面配置
//        TypeElement callType = elementTool.getTypeElement("top.zcwfeng.arouter_api.Call");
        TypeElement callType = elementTool.getTypeElement(ProcessorConfig.CALL);
        // 拿到所有接口的信息
        TypeMirror callMirror = callType.asType();


        // 获取所有被@Arouter 注解的元素集合
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ARouter.class);
        // 通过Element工具类，获取Activity，Callback类型
        TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
        // 显示类信息（获取被注解的节点，类节点）这也叫自描述 Mirror
        TypeMirror activityMirror = activityType.asType();

        // 遍历所有的类节点 包含注解的
        for (Element element : elements) {
            // 获取类节点，获取包节点
//            String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();
            // 获取简单类名，例如：MainActivity
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>for 被@ARetuer注解的类有：" + className); // 打印出 就证明APT没有问题
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>for Options：" + options); // 打印出 就证明APT没有问题
            // 拿到注解
            ARouter aRouter = element.getAnnotation(ARouter.class);


            //  2020/11/18 测试1
            /*package top.zcwfeng.test;

            import java.lang.System;

            public final class DavidTest {
                public static void main(System[] args) {
                    System.out.println("Hello,JavaPoet");
                }
            }*/
           /*  // 1.方法
            MethodSpec mainMethod = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(System[].class,"args")
                    .addStatement("$T.out.println($S)",System.class,"Hello,JavaPoet")
                    .build();
            // 2.类
            // 注意+ options
            TypeSpec testClass = TypeSpec.classBuilder("DavidTest"+options)
                    .addMethod(mainMethod)
                    .addModifiers(Modifier.PUBLIC,Modifier.FINAL)
                    .build();
            //3.包
            JavaFile packgef = JavaFile.builder("top.zcwfeng.test", testClass).build();
            try {
                packgef.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE, "生成失败，请检查代码..." + e.getMessage());

            }*/
            // 2020/11/18 测试2
            /*package top.zcwfeng.customarouter;


            public class MainActivity$$$$$$$ARouter {
                public static Class findTargetClass(String path) {
                    return path.equals("app/MainAcctivity") ? MainActivity.class:null;
                }
            }*/

            /*
            String finalCalssName = classname + "$$$$$$$ARouter";
            // 1.方法
            MethodSpec mainMethod = MethodSpec.methodBuilder("findTargetClass")
                    .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
                    .returns(Class.class)
                    .addParameter(String.class,"path")
                    // JavaPoet 包装转型
                    .addStatement("return path.equals($S) ? $T.class:null",aRouter.path(),
                            ClassName.get((TypeElement)element))
                    .build();
            // 2.类
            TypeSpec testClass = TypeSpec.classBuilder(finalCalssName)
                    .addMethod(mainMethod)
                    .addModifiers(Modifier.PUBLIC)
                    .build();
            //3.包
            JavaFile packgef = JavaFile.builder(packageName, testClass).build();
            try {
                packgef.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
*/


            // TODO: 1.准备RouterBean 检查参数和路径
            RouterBean routerBean = new RouterBean.Builder()
                    .addGroup(aRouter.group())
                    .addPath(aRouter.path())
                    .addElement(element)
                    .build();


            // 我们自己定义：ARouter注解的类 必须继承 Activity
            // Main2Activity的具体详情 例如：继承了 Activity
            TypeMirror elementMirror = element.asType();
            // activityMirror  android.app.Activity描述信息
            if (typeTool.isSubtype(elementMirror, activityMirror)) {
                routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);
            } else if(typeTool.isSubtype(elementMirror, callMirror)){// TODO: 组件共享 2 判断我们的routerBean类型
                routerBean.setTypeEnum(RouterBean.TypeEnum.CALL);
            }else {
                // 我们测试，随便写一个类DavidTest.java 加上@Arouter注解，但是不满足我们规则typeEnum
                // 主动抛出异常
                throw new RuntimeException("@Arouter 注解目前仅限用于Activity类上");
            }

            if (checkRouterPath(routerBean)) {
                messager.printMessage(Diagnostic.Kind.NOTE, "RouterBean Check Success:" + routerBean.toString());
                // 赋值到 mAllPathMap 集合
                List<RouterBean> routerBeans = mAllPathMaps.get(routerBean.getGroup());
                // 如果从Map中找不到key为：bean.getGroup()的数据，就新建List集合再添加进Map
                if (ProcessorUtils.isEmpty(routerBeans)) {
                    routerBeans = new ArrayList<>();
                    routerBeans.add(routerBean);
                    mAllPathMaps.put(routerBean.getGroup(), routerBeans);
                } else {
                    routerBeans.add(routerBean);
                }
            } else {
                // ERROR 编译期发生异常
                messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            }

        }


        // 定义（生成类文件实现的接口） 有 Path Group
        TypeElement pathType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_PATH);
        TypeElement groupType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_GROUP);
        // TODO: 2. 第一步：生成系列PATH
        try {
            createPathFile(pathType); // 生成 Path类
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "在生成PATH模板时，异常了 e:" + e.getMessage());
        }
        // TODO: 3. 第二步：生成组Group
        try {
            createGroupFile(groupType, pathType);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "在生成GROUP模板时，异常了 e:" + e.getMessage());
        }

        elements.clear();
        return true;
    }

//    public class ARouter$$Group$$persional implements ARouterGroup {
//        @Override
//        public Map<String, Class<? extends ARouterPath>> getGroupMap() {
//            Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
//            groupMap.put("app", ARouter$$Path$$app.class);
//            return groupMap;
//        }
//    }


    /**
     * 生成路由组Group文件，如：ARouter$$Group$$app
     *
     * @param groupType ARouterLoadGroup接口信息
     * @param pathType  ARouterLoadPath接口信息
     */
    private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException {
        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：2222222");

        if (ProcessorUtils.isEmpty(mAllGroupMap) || ProcessorUtils.isEmpty(mAllPathMaps)) {
            return;
        }


        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),//Map
                ClassName.get(String.class),//Map<String,
                // Class<? extends ARouterPath>> 难度
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType)))//? extends ARouterPath
                // WildcardTypeName.supertypeOf() 做实验 ? super

        );

        // 1.方法 public Map<String, Class<? extends ARouterPath>> getGroupMap() {
        MethodSpec.Builder methodBuidler = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME) // 方法名
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(methodReturn); // 方法返回值
        //Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
        methodBuidler.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))),
                ProcessorConfig.GROUP_VAR1,
                ClassName.get(HashMap.class)
        );
        //  groupMap.put("personal", ARouter$$Path$$personal.class);
        //	groupMap.put("order", ARouter$$Path$$order.class);
        for (Map.Entry<String, String> entry : mAllGroupMap.entrySet()) {
            methodBuidler.addStatement("$N.put($S, $T.class)",
                    ProcessorConfig.GROUP_VAR1, // groupMap.put
                    entry.getKey(), // order, personal ,app
                    ClassName.get(aptPackage, entry.getValue()));
        }

        // return groupMap;
        methodBuidler.addStatement("return $N", ProcessorConfig.GROUP_VAR1);

        // 最终生成的类文件名 ARouter$$Group$$ + personal
        String finalClassName = ProcessorConfig.GROUP_FILE_NAME + options;

        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由组Group类文件：" +
                aptPackage + "." + finalClassName);

        // 生成文件： ARouter$$Group$$persional
        JavaFile.builder(aptPackage,
                TypeSpec.classBuilder(finalClassName)
                        .addSuperinterface(ClassName.get(groupType))// 实现ARouterLoadGroup接口 implements ARouterGroup
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(methodBuidler.build())// 方法的构建（方法参数 + 方法体）
                        .build())// 类构建完成
                .build()// JavaFile构建完成
                .writeTo(filer);// 文件生成器开始生成类文件


    }


//    public class ARouter$$Path$$personal implements ARouterPath {
//        @Override
//        public Map<String, RouterBean> getPathMap() {
//            Map<String, RouterBean> pathMap = new HashMap<>();
//            pathMap.put("/app/Main2Activity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY, Main2Activity.class, "/app/Main2Activity", "app"));
//            pathMap.put("/app/MainActivity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY, MainActivity.class, "/app/MainActivity", "app"));
//            return pathMap;
//        }
//    }


    /**
     * 系列Path的类  生成工作
     *
     * @param pathType ARouterPath 高层的标准
     * @throws IOException
     */
    private void createPathFile(TypeElement pathType) throws IOException {
        if (ProcessorUtils.isEmpty(mAllPathMaps)) {
            return;
        }


        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),//Map
                ClassName.get(String.class),//Map<String,
                ClassName.get(RouterBean.class)//Map<String,RouterBean>
        );

        // 遍历仓库 app,order,personal
        for (Map.Entry<String, List<RouterBean>> entry : mAllPathMaps.entrySet()) {
            // 1. 方法
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
                    .addAnnotation(Override.class)// 给方法上添加注解  @Override
                    .addModifiers(Modifier.PUBLIC)// public修饰符
                    .returns(methodReturn);// 把Map<String, RouterBean> 加入方法返回

            // Map<String, RouterBean> pathMap = new HashMap<>(); 如果是$T class类型一般要包装一下
            methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                    ClassName.get(Map.class),// Map
                    ClassName.get(String.class),//Map<String,
                    ClassName.get(RouterBean.class),//Map<String, RouterBean>
                    ProcessorConfig.PATH_VAR1,//Map<String, RouterBean> pathMap
                    ClassName.get(HashMap.class)//Map<String, RouterBean> pathMap = new HashMap<>();
            );

            // 循环添加map赋值
            List<RouterBean> pathList = entry.getValue();
            /**
             $N == 变量 变量有引用 所以 N
             $L == TypeEnum.ACTIVITY
             */
            //  pathMap.put("/app/Main2Activity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY, Main2Activity.class, "/app/Main2Activity", "app"));
            for (RouterBean bean : pathList) {
                methodBuilder.addStatement("$N.put($S, $T.create($T.$L,$T.class,$S,$S))",
                        ProcessorConfig.PATH_VAR1,
                        bean.getPath(),
                        ClassName.get(RouterBean.class),
                        ClassName.get(RouterBean.TypeEnum.class),
                        bean.getTypeEnum(),
                        ClassName.get((TypeElement) bean.getElement()),
                        bean.getPath(),
                        bean.getGroup()
                );
            }// end for

            // return pathMap;
            methodBuilder.addStatement("return $N", ProcessorConfig.PATH_VAR1);

            // TODO 注意：不能像以前一样，1.方法，2.类  3.包， 因为这里面有implements ，所以 方法和类要合为一体生成才行，这是特殊情况
            String finalClassName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();

            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：11111111111111111");


            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：" +
                    aptPackage + "." + finalClassName);

            // 生成类文件：public class ARouter$$Path$$personal implements ARouterPath
            JavaFile.builder(aptPackage,
                    TypeSpec.classBuilder(finalClassName)
                            .addSuperinterface(ClassName.get(pathType))// 实现ARouterLoadPath接口  implements ARouterPath==pathType
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(methodBuilder.build())// 方法的构建（方法参数 + 方法体）
                            .build())// 类构建完成
                    .build()// JavaFile构建完成
                    .writeTo(filer);// 文件生成器开始生成类文件

            // 仓库二 缓存二  非常重要一步，注意：PATH 路径文件生成出来了，才能赋值路由组mAllGroupMap
            mAllGroupMap.put(entry.getKey(), finalClassName);
        }
    }


    /**
     * 校验@ARouter注解的值，如果group未填写就从必填项path中截取数据
     *
     * @param routerBean 路由详细信息，最终实体封装类
     */
    private boolean checkRouterPath(RouterBean routerBean) {
        // "app"   "order"   "personal"
        String group = routerBean.getGroup();
        // "/app/MainActivity"   "/order/Order_MainActivity"   "/personal/Personal_MainActivity"
        String path = routerBean.getPath();
        // 校验  @ARouter注解中的path值，必须要以 / 开头（模仿阿里Arouter规范）
        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {//TextUtils Java 工程无法引用，所以封装processutils
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的path值，必须要以 / 开头");
        }

        // 因为用户使用 @ARouter(path="/app/MainActivity" group="app") 我们默认省略group，替业务程序取出组名app
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        // 检查 @ARouter注解中group赋值情况
        if (!ProcessorUtils.isEmpty(group) && !group.equals(options)) {
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
            return false;
        } else {
            routerBean.setGroup(finalGroup);
        }

        return true;
    }
}
