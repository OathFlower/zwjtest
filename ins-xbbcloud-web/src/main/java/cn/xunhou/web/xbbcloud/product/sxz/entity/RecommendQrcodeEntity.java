package cn.xunhou.web.xbbcloud.product.sxz.entity;


import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbEntity;
import lombok.Data;

/**
 * 推荐二维码表
 */
@Data
@XbbTable(table = "recommend_qrcode")
public class RecommendQrcodeEntity extends XbbEntity {
    /**
     * 二维码url
     */
    private String qrcodeUrl;
    /**
     * 邀请码
     */
    private String interviewCode;
    /**
     * 操作人员id
     */
    private Long operatorId;

}
