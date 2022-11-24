package cn.xunhou.web.xbbcloud.product.attendance.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class AttendanceRecordParam {
    @NotNull(message = "当前页不可为空")
    private Integer cur_page;
    @NotNull(message = "分页大小不可为空")
    private Integer page_size;
    @NotNull(message = "状态不可为空")
    private Integer status;
    /**
     * 组织id
     */
    private Long org_id;
    /**
     * 员工姓名/工号
     */
    private String keyword;
    /**
     * 开始时间
     */
    private String date_start;
    /**
     * 结束时间
     */
    private String date_end;
    /**
     * 是否分页
     */
    private boolean isPaged;
    /**
     * 租户Id
     */
    private Integer tenantId;
}
