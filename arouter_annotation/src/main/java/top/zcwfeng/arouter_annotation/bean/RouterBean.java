package top.zcwfeng.arouter_annotation.bean;

import javax.lang.model.element.Element;

/**
 * 最终路由 要 传递 对象
 * <p>
 * 路由路径Path的最终实体封装类
 * 例如：app分组中的MainActivity对象，这个对象有更多的属性
 */
public class RouterBean {
    // 为了以后的扩展
    public enum TypeEnum {
        ACTIVITY,
        CALL
    }

    private TypeEnum typeEnum;// 枚举类型：Activity
    private Element element;// 类节点 JavaPoet学习的时候，可以拿到很多的信息
    private Class<?> myclass;// 被注解的 Class对象 例如： MainActivity.class  Main2Activity.class Main3Activity.class
    private String path;// 路由地址  例如：/app/MainActivity

    public void setTypeEnum(TypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public void setMyclass(Class<?> myclass) {
        this.myclass = myclass;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    private String group; // 路由组  例如：app  order  personal

    public RouterBean(TypeEnum typeEnum, /*Element element,*/ Class<?> myclass, String path, String group) {
        this.typeEnum = typeEnum;
//        this.element = element;
        this.myclass = myclass;
        this.path = path;
        this.group = group;
    }

    // 对外暴露
    // 对外提供简易版构造方法，主要是为了方便APT生成代码
    public static RouterBean create(TypeEnum type, Class<?> clazz, String path, String group) {
        return new RouterBean(type, clazz, path, group);
    }

    public RouterBean(Builder builder) {
        this.typeEnum = builder.type;
        this.element = builder.element;
        this.myclass = builder.clazz;
        this.path = builder.path;
        this.group = builder.group;
    }

    public TypeEnum getTypeEnum() {
        return typeEnum;
    }

    public Element getElement() {
        return element;
    }

    public Class<?> getMyclass() {
        return myclass;
    }

    public String getPath() {
        return path;
    }

    public String getGroup() {
        return group;
    }

    public static class Builder {
        // 枚举类型：Activity
        private TypeEnum type;
        // 类节点
        private Element element;
        // 注解使用的类对象
        private Class<?> clazz;
        // 路由地址
        private String path;
        // 路由组
        private String group;

        public Builder addType(TypeEnum type) {
            this.type = type;
            return this;

        }

        public Builder addElement(Element element) {
            this.element = element;
            return this;

        }

        public Builder addClazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;

        }

        public Builder addPath(String path) {
            this.path = path;
            return this;

        }

        public Builder addGroup(String group) {
            this.group = group;
            return this;
        }

        public RouterBean build() {

            if(path == null || path.length() == 0){
                throw new IllegalArgumentException("path必填项为空，应该为如：/app/MainActivity");
            }
            return new RouterBean(this);
        }
    }

    @Override
    public String toString() {
        return "RouterBean{" +
                "typeEnum=" + typeEnum +
                ", element=" + element +
                ", myclass=" + myclass +
                ", path='" + path + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
