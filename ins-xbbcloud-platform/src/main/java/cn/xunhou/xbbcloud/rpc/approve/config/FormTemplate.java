package cn.xunhou.xbbcloud.rpc.approve.config;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author: chenning
 * @Date: 2022/10/11/17:58
 * @Description:
 */
@Setter
@Getter
@Accessors(chain = true)
public class FormTemplate {

    private Long templateId = 0L;

    private List<FormDetail> query;

    private List<FormDetail> form;

    private List<FormDetail> table;

}
