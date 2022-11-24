package cn.xunhou.web.xbbcloud.product.attendance.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author litb
 * @date 2022/7/5 13:48
 * 高德地图 搜索POI 2.0-关键字搜索结果
 * https://lbs.amap.com/api/webservice/guide/api/newpoisearch
 * <p>
 * 2.0.2版本仅返回了基础字段
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class GeoKeywordsQueryResult {

    private static final long serialVersionUID = -154817827189663840L;

    /**
     * poi名称
     */
    private String name;

    /**
     * poi唯一标识
     */
    private String id;

    /**
     * poi所属类型
     */
    private String type;

    /**
     * poi分类编码
     */
    private String typecode;

    /**
     * poi所属省份
     */
    private String pname;

    /**
     * poi所属城市
     */
    private String cityname;

    /**
     * poi所属区县
     */
    private String adname;

    /**
     * poi详细地址
     */
    private String address;

    /**
     * poi所属省份编码
     */
    private String pcode;

    /**
     * poi所属区域编码
     */
    private String adcode;

    /**
     * poi所属城市编码
     */
    private String citycode;

    /**
     * poi经纬度
     */
    private String location;
}
