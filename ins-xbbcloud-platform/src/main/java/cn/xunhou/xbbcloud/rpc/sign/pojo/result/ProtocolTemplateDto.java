package cn.xunhou.xbbcloud.rpc.sign.pojo.result;


import lombok.Data;

import java.util.List;

@Data
public class ProtocolTemplateDto {

    private Long id;

    /**
     * 合同名称
     */
    private String name;
    /**
     * 模板编号
     */
    private String templateNo;
    /**
     * 协议分组名称
     */
    private String protocolTypeName;
    /**
     * 签署参数（逗号分隔）
     */
    private String params = "";

    /**
     * 签字字段参数（逗号分隔）
     */
    private String signParams = "";

    /**
     * 盖章字段参数（逗号分隔）
     */
    private String stampParams = "";

    /**
     * 模板预览地址
     */
    private String previewUrl;
    /**
     * 创建人Id
     */
    private Long creatorId;
    /**
     * 创建时间
     */
    private String createtime;
    /**
     * 更新日期
     */
    private String modifytime;
    /**
     * 删除标记
     */
    private Integer deleteflag;


    /**
     * 业务类型
     */
    private List<Integer> contractTemplateTypes;

    /**
     * 业务类型名
     */
    private List<String> contractTemplateTypeNames;

    /**
     * 操作人Id
     */
    private Long operatorId;

    /**
     * 入职流程（是否为入职流程 0 否 1 是）
     */
    private Integer entryProcess;

}
