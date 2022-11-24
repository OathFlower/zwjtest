package cn.xunhou.xbbcloud.rpc.salary.entity;


import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "salary_openid")
public class SalaryOpenIdEntity extends XbbSnowTimeEntity {


    private Long userXhCId;
    private String idCardNo;
    private String openId;


}