package cn.xunhou.xbbcloud.common.enums;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * @author litb
 * @date 2021/7/30 11:05
 * <p>
 * 枚举类父级接口,用于统一处理序列化、反序列化以及数据库映射
 */
public interface IEnum<C extends Serializable> {

    /**
     * 根据code从指定枚举类中获取枚举值
     *
     * @param clazz 枚举类
     * @param code  code
     * @param <E>   枚举的类型
     * @return 枚举值
     */
    @Nullable
    static <E extends Enum<?> & IEnum<? extends Serializable>, C> E formCode(@NonNull Class<E> clazz, @NonNull C code) {
        E[] elements = clazz.getEnumConstants();
        for (E element : elements) {
            if (element.getCode().equals(code)) {
                return element;
            }
        }
        return null;
    }

    /**
     * 获取枚举的code
     *
     * @return code
     */
    C getCode();

    /**
     * 获取枚举的value
     *
     * @return value
     */
    String getMessage();

}
