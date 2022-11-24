package cn.xunhou.xbbcloud.rpc.sign.entity;

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
@XbbTable(table = "position_contract_template")
public class PositionContractTemplateEntity extends XbbSnowTimeEntity {


    /**
     * 岗位二维码ID
     */
    private Long positionQrcodeId;
    /**
     * 合同模板(合同/协议)ID
     */
    private Long contractTemplateId;
    /**
     * 类型 1合同 2协议
     */
    private Integer type;


}
