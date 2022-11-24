package cn.xunhou.web.xbbcloud.product.salary.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SubjectAndSubAccountResult extends SubjectInfoResult {
    /**
     * 子账户信息
     */
    private List<SubAccountInfoResult> subAccountInfoList;
}
