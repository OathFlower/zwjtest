package cn.xunhou.xbbcloud.rpc.approve.config;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * @Author: chenning
 * @Date: 2022/09/26/17:00
 * @Description:
 */
@Data
public class WorkflowTemplate {

    private Long templateId = 0L;

    private Integer templateType = 0;

    private String templateName = "";

    private String description = "";

    private String postHandle = "";

    private List<Node> node = Collections.emptyList();

    @Data
    public static class Node {
        private Integer level = 0;

        private Integer subLevel = 0;

        private String nodeName = "";

        private Integer nodeType = 0;

        private String event = "";

        private String condition = "";

        private Integer preNode = 0;

        private Integer nextNode = 0;

        private Integer assigneeType = 2;

        private String listener = "";

        private Integer approveType = 0;

        private Integer signType = 2;

        private Long assignee = 0L;

        private String description = "";

        private AuditType auditType = new AuditType();

        private Integer nextTrueSubNode = 0;

        private Integer nextFalseSubNode = 0;
    }
    @Data
    public static class AuditType {
        private Integer type = 0;
        private String code = "";
    }

}
