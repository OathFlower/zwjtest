package cn.xunhou.web.xbbcloud.product.sign.enums;


/**
 * @author wangkm
 */
public class ReadingStatus {

    public enum ErrorEnum {
        /**
         * 查不数据
         */
        SOURCE_DATA_FOUND_NOT("0", "查不到相关数据"),
        /**
         * 数据类型不存在
         */
        SOURCE_TYPE_FOUND_NOT("1", "数据类型不存在");


        ErrorEnum(String code, String message) {
            this.code = code;
            this.message = message;
        }

        private final String code;
        private final String message;

        public String code() {
            return this.code;
        }

        /**
         * 详细信息
         *
         * @return
         */

        public String message() {
            return this.message;
        }
    }

    /**
     * 阅读状态
     */
    public enum ReadingState {
        /**
         * 未读
         */
        UNREAD(0, "未读"),
        /**
         * 已读
         */
        READ(1, "已读");
        private final Integer code;
        private final String text;

        ReadingState(Integer code, String text) {
            this.code = code;
            this.text = text;
        }

        /**
         * 根据code查枚举
         *
         * @param code code值
         * @return 枚举
         */
        public static ReadingState findEnumForCode(Integer code) {
            if (null == code) {
                return null;
            }
            ReadingState[] values = values();
            for (ReadingState value : values) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }

        public Integer getCode() {
            return code;
        }

        public String getText() {
            return text;
        }
    }


    /**
     * 数据类型
     */
    public enum SourceType {
        // https://dqj5a1drpr.feishu.cn/docs/doccnj9aNMaGbSqyTjjrhHPiFwI#8E5nWR
        /**
         * 面试签到：未签到数。进入签到详情页，对应数字减少（触发点：PM交付发邀请）
         * 超时淘汰则需要角标展示，点击查看后则对应数字减少
         */
        TYPE_0(0, "面试签到"),
        /**
         * 待确认数。进入邀请详情页后对应数字减少（触发点：PM人力发offer）
         */
        TYPE_1(1, "入职邀请"),

        /**
         * 入职办理：（触发点：PM人力操作入职办理）
         * - 角标展示：有入职代办时、入职办理后台审核成功及失败时
         * - 角标不展示：人力系统取消入职办理时、点击提交按钮时、查看入职办理成功后
         * - 其他：如果用户在入职办理过程中退出页面（未成功提交信息），首页依旧展示角标数字1。
         */
        TYPE_2(2, "入职办理"),

        /**
         * 合同协议：未签署和状态更新数。进入查看合同详情后，首页角标对应减少。（触发点：PM人力发合同、审核合同（通过/拒绝））
         */
        TYPE_3(3, "合同协议"),

        /**
         * 离职申请：离职状态更新。查看离职申请页面后角标消失。离职成功（触发点：PM人力操作离职）
         */
        TYPE_4(4, "离职申请"),
        /**
         * 更多岗位：创建岗位更新.查看更多岗位后角标消失
         */
        TYPE_5(5, "更多岗位"),
        /**
         * 邀请奖励：邀请人获取到奖励后更新.退出页面后角标消失
         */
        TYPE_6(6, "邀请奖励"),
        TYPE_7(7, "预留7"),
        ;
        private final Integer code;
        private final String text;

        SourceType(Integer code, String text) {
            this.code = code;
            this.text = text;
        }

        public Integer getCode() {
            return code;
        }

        public String getText() {
            return text;
        }

        /**
         * 根据code查枚举
         *
         * @param code code值
         * @return 枚举
         */
        public static SourceType findEnumForCode(Integer code) {
            if (null == code) {
                return null;
            }
            SourceType[] values = values();
            for (SourceType value : values) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

}
