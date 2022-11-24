package cn.xunhou.xbbcloud.rpc.other.pojo.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class CommonSettingResult {

    //通用设置id
    private Long commonSettingId;
    //创建人
    private Long createBy;


}
