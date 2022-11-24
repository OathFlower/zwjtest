package cn.xunhou.web.xbbcloud.product.hrm.constant.enums;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.IdcardUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author sha.li
 * @since 2022/9/20
 */
@AllArgsConstructor
@Getter
public enum IdCardTypeEnum {
    /**
     * 居民身份证
     */
    T1(1, "居民身份证") {
        @Override
        public boolean validIdCard(String idCard) {
            return IdcardUtil.isValidCard18(idCard);
        }
    },
    /**
     * 港澳居民来往内地通行证
     */
    T2(2, "港澳居民来往内地通行证") {
        @Override
        public boolean validIdCard(String idCard) {
            return HONGKONG_MACAO_MAINLAND_TRAVEL_PASS.matcher(idCard).matches();
        }
    },
    /**
     * 台湾居民来往大陆通行证
     */
    T3(3, "台湾居民来往大陆通行证") {
        @Override
        public boolean validIdCard(String idCard) {
            return TAIWAN_TRAVEL_PASS.matcher(idCard).matches();
        }
    },
    ;

    /**
     * 港澳居民来往内地通行证
     */
    public static final Pattern HONGKONG_MACAO_MAINLAND_TRAVEL_PASS = Pattern.compile("^[HMhm]([0-9]{10}|[0-9]{8})$");
    /**
     * 台湾居民来往大陆通行证
     */
    public static final Pattern TAIWAN_TRAVEL_PASS = Pattern.compile("^([0-9]{8}|[0-9]{10})$");

    private final Integer code;
    private final String name;

    public static Opt<IdCardTypeEnum> get(String name) {
        for (IdCardTypeEnum e : IdCardTypeEnum.values()) {
            if (e.getName().equals(name)) {
                return Opt.ofNullable(e);
            }
        }
        return Opt.empty();
    }

    public static List<String> getNames() {
        return Arrays.stream(IdCardTypeEnum.values()).map(IdCardTypeEnum::getName).collect(Collectors.toList());
    }

    /**
     * 校验证件是否正确
     */
    public abstract boolean validIdCard(String idCard);
}
