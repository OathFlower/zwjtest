package cn.xunhou.web.xbbcloud.product.attendance.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author litb
 * @date 2022/7/5 13:50
 * <p>
 * https://lbs.amap.com/api/webservice/guide/api/newpoisearch
 */
@Getter
@Setter
@ToString(callSuper = true)
public class GeoKeywordsResult extends AmapRsp {

    /**
     * 返回的poi完整集合
     */
    List<GeoKeywordsQueryResult> pois;
}
