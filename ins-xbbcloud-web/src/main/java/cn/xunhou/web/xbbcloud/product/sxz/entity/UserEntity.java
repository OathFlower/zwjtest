package cn.xunhou.web.xbbcloud.product.sxz.entity;


import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbEntity;
import lombok.Data;

/**
 * 用户表
 */
@Data
@XbbTable(table = "user")
public class UserEntity extends XbbEntity {
    /**
     * 客户表id
     */
    private Long customerId;
    /**
     * 推荐码
     */
    private String interviewCode;

    /**
     * 用户名称
     */
    private String name;

    /**
     * 手机号
     */
    private String tel;
    /**
     * 类型 0普通用户 1企业管理员
     */
    private Integer adminType;
    /**
     * 操作人员id
     */
    private Long operatorId;

    /**
     * 余额
     */
    private Integer coin;

}
