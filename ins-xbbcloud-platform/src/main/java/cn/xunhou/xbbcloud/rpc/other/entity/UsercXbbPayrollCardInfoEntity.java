package cn.xunhou.xbbcloud.rpc.other.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "userc_xbb_payroll_card_info")
public class UsercXbbPayrollCardInfoEntity {


    private Long id;
    /**
     * 厚道C端用户id
     */
    private Long userXhCId;

    /**
     * C端openID;
     */
    private String openId;

    /**
     * 务工卡签约主体id
     */
    private Long payrollCardSubjectId;
    /**
     * 真实姓名
     */

    private String userName;
    /**
     * 授权状态
     */
    private Integer authorizeStatus;
    /**
     * 身份证
     */

    private String idCardNo;
    /**
     * 授权时间
     */
    private String authorizeTime;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 创建时间
     */

    private String createtime;
    /**
     * 更新日期
     */

    private String modifytime;


}
