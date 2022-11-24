package cn.xunhou.xbbcloud.rpc.sign.pojo.result;


import lombok.Data;

@Data
public class ContractTemplateDto {

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
     * 参数字典值
     */
    private String paramsValue;

    /**
     * 主体标识（可多选）
     */
    private String subjectType;
    /**
     * 模板预览地址
     */
    private String previewUrl;

    /**
     * 预览pdf
     */
    private String previewPdf;

    /**
     * 模板pdf oss id
     */
    private String previewOss;

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


}
