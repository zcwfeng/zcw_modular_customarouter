package top.zcwfeng.arouter_compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import top.zcwfeng.arouter_annotation.Parameter;
import top.zcwfeng.arouter_compiler.utils.ProcessorConfig;
import top.zcwfeng.arouter_compiler.utils.ProcessorUtils;

/**
 * 生成方法
 */
public class ParameterFactory {

    /*
    生成如下方法模板

     @Override
        public void getParameter(Object targetParameter) {
            Personal_MainActivity t = (Personal_MainActivity) targetParameter;
            t.name = t.getIntent().getStringExtra("name");
            t.sex = t.getIntent().getStringExtra("sex");
        }
     */
    // 方法构建
    private MethodSpec.Builder method;
    // 类名：MainActivity / Personal_MainActivity 等
    private ClassName className;
    // 用来提示错误等信息
    private Messager messager;
    // type(类信息)工具类，包含用于操作TypeMirror的工具方法
    private Types typeUtils;
    // 获取元素接口信息（生成类文件需要的接口实现类）
    private TypeMirror callMirror;

    public ParameterFactory(Builder builder) {
        this.messager = builder.messager;
        this.className = builder.className;
        typeUtils = builder.typeUtils;
        // 生成此方法
        // 通过方法参数体构建方法体：public void getParameter(Object target) {
        method = MethodSpec.methodBuilder(ProcessorConfig.PARAMETER_METHOD_NAME)
                .addAnnotation(Override.class)//@Override
                .addModifiers(Modifier.PUBLIC)
                .addParameter(builder.parameterSpec);

        // TODO: 组件共享 接口自描述
        // call自描述 Call接口的
        callMirror = builder.elementUtils.getTypeElement(ProcessorConfig.CALL).asType();
    }


    /**
     * Personal_MainActivity t = (Personal_MainActivity) targetParameter;
     */
    public void addFirstStatement() {
        method.addStatement("$T t = ($T)" + ProcessorConfig.PARAMETER_NAME, className, className);
    }


    /**
     * 多行，循环，复杂
     * 构建方体内容，如：t.s = t.getIntent.getStringExtra("s");
     * <p>
     * t.name = t.getIntent().getStringExtra("name");
     * t.sex = t.getIntent().getStringExtra("sex");
     *
     * @param element 被注解的属性元素
     * @Parameter int age = 10;
     */
    public void buildStatement(Element element) {
        // 遍历注解的属性节点 生成函数体
        TypeMirror typeMirror = element.asType();

        // 获取TypeKind 枚举类型的序列号
        int type = typeMirror.getKind().ordinal();

        // 获取属性名 name age sex
        String fieldName = element.getSimpleName().toString();

        // 获取注解的值
        String annotationValue = element.getAnnotation(Parameter.class).name();

        // 配合： t.age = t.getIntent().getBooleanExtra("age", t.age ==  9);
        // 判断注解的值为空的情况下的处理（注解中有name值就用注解值）
        annotationValue = ProcessorUtils.isEmpty(annotationValue) ? fieldName : annotationValue;

        // 最终拼接额前缀
        String finalValue = "t." + fieldName;

        // t.name = t.getIntent().getStringExtra("name");
        String methodContent = finalValue + "= t.getIntent().";

        // TypeKind 枚举类型不包含String
        if (type == TypeKind.INT.ordinal()) {
            // t.s = t.getIntent().getIntExtra("age", t.age);
            methodContent += "getIntExtra($S," + finalValue + ")";
        } else if (type == TypeKind.BOOLEAN.ordinal()) {
            methodContent += "getBooleanExtra($S," + finalValue + ")";
        } else {
            // 没有序列号提供，需要我们自己完成
            // t.s = t.getIntent.getStringExtra("s");
            // typeMirror.toString() java.lang.String
            if (typeMirror.toString().equalsIgnoreCase(ProcessorConfig.STRING)) {
                methodContent += "getStringExtra($S)";
            } else if (typeUtils.isSubtype(typeMirror, callMirror)) {
                // TODO 共享组件 目标生成模板代码
                // t.orderDrawable = (OrderDrawable) RouterManager.getInstance().build("/order/getDrawable").navigation(t);
                methodContent = "t."
                        + fieldName
                        + " = ($T) $T.getInstance().build($S).navigation(t)";
                method.addStatement(methodContent,
                        TypeName.get(typeMirror),
                        ClassName.get(ProcessorConfig.AROUTER_API_PACKAGE,ProcessorConfig.ROUTER_MANAGER),
                        annotationValue
                        );
                //  因为下面还有 addStatemlent 所以这里return
                return;
            } else {
                // TODO: 共享组件 对象的传输
                methodContent = "t.getIntent().getSerializableExtra($S)";
            }


        }
        // 健壮代码
        if (methodContent.contains("Serializable")) {
            // t.student=(Student) t.getIntent().getSerializableExtra("student"); 注意：为了强转
            method.addStatement(finalValue + "=($T)" + methodContent, ClassName.get(element.asType()) ,annotationValue);
        }//加强判断
        else if (methodContent.endsWith(")")) {
            method.addStatement(methodContent, annotationValue);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "目前暂支持String,int,boolean传参");
        }

    }


    public MethodSpec build() {
        return method.build();
    }


    public static class Builder {
        private ClassName className;
        // 用来提示错误等信息
        private Messager messager;


        // 操作Element工具类 (类、函数、属性都是Element)
        private Elements elementUtils;

        // type(类信息)工具类，包含用于操作TypeMirror的工具方法
        private Types typeUtils;


        // 方法参数
        private ParameterSpec parameterSpec;

        public Builder(ParameterSpec parameterSpec) {
            this.parameterSpec = parameterSpec;
        }

        public Builder setElementUtils(Elements elementUtils) {
            this.elementUtils = elementUtils;
            return this;
        }

        public Builder setTypeUtils(Types typeUtils) {
            this.typeUtils = typeUtils;
            return this;

        }

        public Builder setClassName(ClassName className) {
            this.className = className;
            return this;
        }

        public Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

//        public Builder setParameterSpec(ParameterSpec parameterSpec) {
//            this.parameterSpec = parameterSpec;
//            return this;
//        }

        public ParameterFactory build() {
            if (parameterSpec == null) {
                throw new IllegalArgumentException("parameterSpec方法参数体为空");
            }

            if (className == null) {
                throw new IllegalArgumentException("方法内容中的className为空");
            }

            if (messager == null) {
                throw new IllegalArgumentException("messager为空，Messager用来报告错误、警告和其他提示信息");
            }
            return new ParameterFactory(this);
        }
    }


}
