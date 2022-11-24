package cn.xunhou.web.xbbcloud.product.sign.result;


import lombok.Data;


@Data
public class UserXhCResult {

    private static final long serialVersionUID = 1514747809259902482L;

    private Long id;
    /**
     * 姓名
     */
    private String name;
    /**
     * 手机号
     */
    private String tel;
    /**
     * 微信id
     */
    private String wechatOpenId;
    /**
     * 微信unionId
     */
    private String wechatUnionId;

    /**
     * 出生日期（YYYYMMDD）
     */
    private String birthDay;
    /**
     * 学校
     */
    private String school;
    /**
     * 专业
     */
    private String special;

    /**
     * 籍贯
     */
    private String nativePlace;

    /**
     * 所在地
     */
    private String dq;
    /**
     * 微信头像
     */
    private String wechat_head_img;


    /**
     * 户籍地址
     */
    private String registrationLocation;
    /**
     * 所在地名称
     */
    private String dqName;
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
     * 实名认证时间
     */
    private String realNameCertTime;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 证件号
     */
    private String idCardNo;
    /**
     * 证件类型（default 0,HONGKONG_MACAO_LIVING 4,TAIWAN_LIVING 5）
     */
    private Integer idCardType;
    /**
     * 身份证正面文件系统id
     */
    private String idCardFrontPicLpfsId;
    /**
     * 身份证反面文件系统id
     */
    private String idCardBackPicLpfsId;

    private String createtime;
    private String modifytime;

    /**
     * 用户类型  1灵活员工   2劳务员工
     */
    private String userType;


    /**
     * 用户类型  1灵活员工   2劳务员工  3委托代征  4外包
     */
    private String userTypeName;


    /**
     * 员工状态
     * 入离职状态：未入职0，已入职1，离职2
     */
    private transient Integer jobStatus;


    /**
     * 项目id
     */
    private Integer projectid;

    /**
     * 项目名称
     */
    private String projectname;


}