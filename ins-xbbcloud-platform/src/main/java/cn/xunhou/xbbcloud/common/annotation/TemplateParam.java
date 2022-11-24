package cn.xunhou.xbbcloud.common.annotation;

import cn.xunhou.grpc.proto.xbbcloud.SignServerProto;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TemplateParam {

    /**
     * 签署人
     *
     * @return
     */
    SignServerProto.EnumSigner xbbSigner() default SignServerProto.EnumSigner.UNKNOWN;

    /**
     * 属性类型
     *
     * @return
     */
    SignServerProto.EnumPropertyType propertyType() default SignServerProto.EnumPropertyType.INPUT;

    /**
     * 页面是否展示 默认展示
     */
    boolean showFlag() default true;

    /**
     * 下拉属性值
     *
     * @return
     */
    Class clazz() default Object.class;

    /**
     * 展示文本
     *
     * @return
     */
    String showText();

}
