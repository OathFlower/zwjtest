package cn.xunhou.web.xbbcloud.product.sign.result;

import cn.xunhou.web.xbbcloud.product.sign.enums.ReadingStatus;
import lombok.Data;

@Data
public class ReadingStatusResult {

    /**
     * 手机号
     * 数据类型为 "面试签到" 时，通过手机号反查 薪班班C端userId
     */
    private String tel;

    private Long id;

    /**
     * 数据id
     */
    private String sourceId;

    /**
     * 数据类型
     */
    private ReadingStatus.SourceType sourceType;

    /**
     * 阅读状态:0-未读；1-已读
     */
    private ReadingStatus.ReadingState readingState;

    /**
     * 归属人id: 薪班班C端userId
     */
    private Long ownerId;

    /**
     * 创建人id
     */
    private Long createId;


    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 更新日期
     */
    private String modifyTime;
}
