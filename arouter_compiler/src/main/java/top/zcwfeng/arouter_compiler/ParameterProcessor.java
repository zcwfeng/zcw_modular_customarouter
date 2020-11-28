package top.zcwfeng.arouter_compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import top.zcwfeng.arouter_annotation.Parameter;
import top.zcwfeng.arouter_compiler.utils.ProcessorConfig;
import top.zcwfeng.arouter_compiler.utils.ProcessorUtils;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(ProcessorConfig.PARAMETER_PACKAGE)
public class ParameterProcessor extends AbstractProcessor {


    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementTool;
    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;
    // Message用来打印 日志相关信息
    private Messager messager;
    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;
    // 存储所有带@Parameter的属性
    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        elementTool = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        typeTool = processingEnv.getTypeUtils();
        messager.printMessage(Diagnostic.Kind.NOTE, "APT--->ParameterProcessor init");
    }

    // TODO: 2020/11/21 生成模板
//    public class Personal_MainActivity$$Parameter implements ParameterGet {
//        @Override
//        public void getParameter(Object targetParameter) {
//            Personal_MainActivity t = (Personal_MainActivity) targetParameter;
//            t.name = t.getIntent().getStringExtra("name");
//            t.sex = t.getIntent().getStringExtra("sex");
//        }
//    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 由于返回了 false 就不需要一下代码了
//        if (annotations.isEmpty()) {
//            messager.printMessage(Diagnostic.Kind.NOTE, "未发现@Arouter注解的地方");
//            return false;
//        }

        if (!ProcessorUtils.isEmpty(annotations)) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Parameter.class);

            if (!ProcessorUtils.isEmpty(elements)) {
                //  需要一个缓存存储所有带有注释的属性
                for (Element element : elements) {


                    // 字段节点的上一个节点是类，@Parameter 属性的上一个节点。属性的父节点
                    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                    // enclosingElement == Personal_MainActivity == key
                    if (tempParameterMap.containsKey(enclosingElement)) {
                        tempParameterMap.get(enclosingElement).add(element);
                    } else {
                        List<Element> fields = new ArrayList<>();
                        fields.add(element);
                        tempParameterMap.put(enclosingElement, fields);
                    }
                }// end for tempParameterMap 缓存有值了
            }

            // TODO: 2020/11/21 生成类文间 判断是否有需要生成的类文件
            if (ProcessorUtils.isEmpty(tempParameterMap)) return true;


            TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
            TypeElement parameterType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_PARAMETER_GET);

            ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT,
                    ProcessorConfig.PARAMETER_NAME).build();

            //循环遍历缓存
            for (Map.Entry<TypeElement, List<Element>> entry : tempParameterMap.entrySet()) {
                //key: Personal_MainActivity
                //value: [name,sex,age]
                TypeElement typeElement = entry.getKey();
                // 判断是否是Activity类型的子类
                if (!typeTool.isSubtype(typeElement.asType(), activityType.asType())) {
                    throw new RuntimeException("@Parameter注解仅限与Activity");
                }

                // Personal_Activity 获取类名
                ClassName className = ClassName.get(typeElement);

                // 生成方法
                ParameterFactory factory = new ParameterFactory.Builder(parameterSpec)
                        .setClassName(className)
                        .setMessager(messager)
                        .setTypeUtils(typeTool)
                        .setElementUtils(elementTool)
                        .build();

                //Personal_MainActivity t = (Personal_MainActivity) targetParameter;
                factory.addFirstStatement();

                for (Element element : entry.getValue()) {
                    factory.buildStatement(element);
                }
                // 最终生成的类文件名（类名$$Parameter） 例如：Personal_MainActivity$$Parameter
                String finalClassName = typeElement.getSimpleName() + ProcessorConfig.PARAMETER_FILE_NAME;
                messager.printMessage(Diagnostic.Kind.NOTE, "APT生成获取参数类文件：" +
                        className.packageName() + "." + finalClassName);
                messager.printMessage(Diagnostic.Kind.NOTE, "APT--->ParameterProcessor process====1111" + parameterType);


                try {


                    JavaFile.builder(className.packageName(),
                            TypeSpec.classBuilder(finalClassName)
                                    .addSuperinterface(ClassName.get(parameterType))//implements ParameterGet
                                    .addModifiers(Modifier.PUBLIC)
                                    .addMethod(factory.build())
                                    .build()
                    ).build().writeTo(filer);

                } catch (IOException e) {
                    e.printStackTrace();

                }
            }

        }


        //  true  执行两次 为了防止第二有问题 加了if (set.isEmpty()) {  内部机制回来检测一遍 所以有了第二次
        //  false 执行一次
        return false;
    }
}
