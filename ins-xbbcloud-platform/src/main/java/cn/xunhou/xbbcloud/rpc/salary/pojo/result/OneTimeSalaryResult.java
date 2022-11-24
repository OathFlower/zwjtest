package cn.xunhou.xbbcloud.rpc.salary.pojo.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;


@Getter
@Setter
@ToString
@Accessors(chain = true)
public class OneTimeSalaryResult implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 身份证号 加解密
     */
    private String idCardNo;

    /**
     * 第一次发薪时间
     */
    private Timestamp startTime;


}
