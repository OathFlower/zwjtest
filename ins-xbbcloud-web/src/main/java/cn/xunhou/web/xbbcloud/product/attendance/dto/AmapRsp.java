package cn.xunhou.web.xbbcloud.product.attendance.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 高德地图统一的响应对象
 *
 * @author sha.li
 * @since 2022-5-9
 */
@Getter
@Setter
@ToString
public class AmapRsp {
    /**
     * 1 表示请求成功
     */
    public static final String OK_STATUS = "1";
    /**
     * 0 表示请求失败
     */
    public static final String ERR_STATUS = "0";
    /**
     * 未找到数据时的infoCode
     */
    public static final String DATA_NO_FOUND_INFO_CODE = "30001";


    /**
     * 返回结果状态值
     * <p>
     * 返回值为 0 或 1，0 表示请求失败；1 表示请求成功。
     */
    protected String status;
    /**
     * 返回结果数目
     * <p>
     * 返回结果的个数。
     */
    protected Long count;
    /**
     * 错误编码
     */
    protected String infocode;
    /**
     * 返回状态说明
     * <p>
     * 当 status 为 0 时，info 会返回具体错误原因，否则返回“OK”。详情可以参阅info状态表
     */
    protected String info;

    /**
     * 请求是否成功
     */
    public boolean successful() {
        return OK_STATUS.equals(getStatus());
    }

    /**
     * 是否未找到数据
     */
    public boolean dataNotFound() {
        return DATA_NO_FOUND_INFO_CODE.equals(getInfocode());
    }
}
