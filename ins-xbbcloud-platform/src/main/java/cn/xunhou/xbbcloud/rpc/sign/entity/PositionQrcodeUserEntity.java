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
@XbbTable(table = "position_qrcode_user")
public class PositionQrcodeUserEntity extends XbbSnowTimeEntity {


    /**
     * 岗位二维码id
     */
    private Long positionQrcodeId;
    /**
     * 手机号
     */
    private String tel;


}
