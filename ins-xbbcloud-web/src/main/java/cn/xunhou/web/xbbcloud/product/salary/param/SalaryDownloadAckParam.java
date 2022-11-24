package cn.xunhou.web.xbbcloud.product.salary.param;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 回执单下载入参
 *
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryDownloadAckParam {
    /**
     * 交易详情编号
     */
    @NotEmpty(message = "详情id不能为空")
    private List<String> withdrawalNos;
}
