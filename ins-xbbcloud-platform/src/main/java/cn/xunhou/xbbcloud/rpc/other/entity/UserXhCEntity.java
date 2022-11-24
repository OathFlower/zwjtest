package cn.xunhou.xbbcloud.rpc.other.entity;


import cn.xunhou.cloud.dao.annotation.XbbPrimaryKey;
import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.annotation.generate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "user_xh_c")
public class UserXhCEntity {

    /**
     * 主键
     */
    @XbbPrimaryKey(generate = generate.AutoIncrement)
    private Integer id;

    /**
     * 员工姓名
     */
    private String name;

    /**
     * 电话
     */
    private String tel;

    /**
     * 和微信公众号关联的id
     */
    private String wechatOpenId;

    /**
     * 微信unionid
     */
    private String wechatUnionId;

    /**
     * 微信头像
     */
    private String wechatHeadImg;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 出生日期（yyyymmdd）
     */
    private String birthDay;

    /**
     * 学历
     */
    private String eduLevel;

    /**
     * 学校编码
     */
    private String school;

    /**
     * 专业
     */
    private String special;

    /**
     * 政治面貌
     */
    private Integer politicalAffiliation;

    /**
     * 婚姻状况编码
     */
    private Integer marriageStatus;

    /**
     * 籍贯
     */
    private String nativePlace;

    /**
     * 户籍类型编码
     */
    private Integer registrationType;

    /**
     * 民族
     */
    private Integer nationCode;

    /**
     * 户籍地址
     */
    private String registrationLocation;

    /**
     * 所在地
     */
    private String dq;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 紧急联系人
     */
    private String emergencyContactPerson;

    /**
     * 紧急联系人联系方式
     */
    private String emergencyContactPersonTel;

    /**
     * 紧急联系人住址
     */
    private String emergencyContactPersonAddress;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 证件号
     */
    private String idCardNo;

    /**
     * 证件类型
     */
    private Integer idCardType;

    /**
     * 实名认证状态
     */
    private Integer realNameCertStatus;

    /**
     * 实名认证类型
     */
    private Integer realNameCertType;

    /**
     * 实名认证完成时间
     */
    private String realNameCertTime;

    /**
     * 身份证正面文件系统id
     */
    private String idCardFrontPicLpfsId;

    /**
     * 身份证反面文件系统id
     */
    private String idCardBackPicLpfsId;

    /**
     * 账号来源 0: 厚道 1:上海勋厚
     */
    private Integer source;

    /**
     * 账户状态
     */
    private Integer status;

    /**
     * 测试数据标识
     */
    private Integer testFlag;

    /**
     * 创建时间
     */
    private Timestamp createtime;

    /**
     * 更新时间
     */
    private Timestamp modifytime;


}