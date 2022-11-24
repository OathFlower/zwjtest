package cn.xunhou.web.xbbcloud.product.hrm.result;

import cn.hutool.core.util.StrUtil;
import cn.xunhou.cloud.core.check.IEmpty;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * excel行校验失败消息
 *
 * @author sha.li
 * @since 2022/9/15
 */
public class ExcelRowErrorInfo implements IEmpty {
    private final Map<Integer, Collection<String>> msgMap;
    private final Supplier<Collection<String>> msgValueConstruct;

    public ExcelRowErrorInfo() {
        this(new LinkedHashMap<>(16), ArrayList::new);
    }

    public ExcelRowErrorInfo(@NonNull Map<Integer, Collection<String>> msgMap, @NonNull Supplier<Collection<String>> msgValueConstruct) {
        this.msgMap = msgMap;
        this.msgValueConstruct = msgValueConstruct;
    }

    public String formatMsg(Collection<String> messages) {
        return String.join("；", messages);
    }

    public ExcelRowErrorInfo add(Integer rowIndex, String msgTemplate, Object... templateParams) {
        return add(rowIndex, StrUtil.format(msgTemplate, templateParams));
    }

    public ExcelRowErrorInfo add(Integer rowIndex, String message) {
        msgMap.compute(rowIndex, (k, v) -> {
            if (v == null) {
                v = msgValueConstruct.get();
            }
            v.add(message);
            return v;
        });
        return this;
    }

    @Override
    public boolean isEmpty() {
        return msgMap.isEmpty();
    }

    @Override
    public String toString() {
        StringJoiner result = new StringJoiner("\n", "{", "}");
        msgMap.forEach((k, v) -> result.add("rowIndex=" + k + ", msg=" + formatMsg(v)));
        return result.toString();
    }

    public void forEach(BiConsumer<Integer, Collection<String>> action) {
        msgMap.forEach(action);
    }
}
