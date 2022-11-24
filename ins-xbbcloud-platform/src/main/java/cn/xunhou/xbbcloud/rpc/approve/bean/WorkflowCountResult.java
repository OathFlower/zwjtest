package cn.xunhou.xbbcloud.rpc.approve.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * @Author: chenning
 * @Date: 2022/09/29/17:13
 * @Description:
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class WorkflowCountResult {

    private Long id;

    private Integer status;
    private Integer count = 0;
}
