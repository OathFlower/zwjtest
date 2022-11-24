drop table if exists xbbcloud.workflow_instances;
CREATE TABLE xbbcloud.`workflow_instances`
(
    `id`               bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`        bigint(20)   NOT NULL DEFAULT '0' COMMENT '租户id',
    `flow_no`          varchar(100) NOT NULL DEFAULT '0' COMMENT '流编号',
    `flow_title`        varchar(100) NOT NULL DEFAULT '' COMMENT '审批实例名称',
    `flow_temp_id`        bigint(20)   NOT NULL DEFAULT '0' COMMENT '流程模板id',
    `applicant_id`      bigint(20)   NOT NULL DEFAULT '0' COMMENT '申请人ID',
    `assignee_id`      bigint(20)   COMMENT '操作人ID',
    `edit_target_id`      bigint(20)   DEFAULT NULL COMMENT '编辑目标ID',
    `status`           tinyint(4)   NOT NULL DEFAULT '0' COMMENT '审批状态 0未发起 10审批中 20审批通过 30审批驳回 40撤销',
    `run_status`       tinyint(4)   NOT NULL DEFAULT '0' COMMENT '实例状态 1运行中 2挂起 3已结束',
    `source`           tinyint(4)   NOT NULL DEFAULT '0' COMMENT '来源 0自有系统 1钉钉 2企微',
    `start_time`       timestamp   COMMENT '开始时间',
    `end_time`       timestamp    COMMENT '结束时间',
    `approve_time`       timestamp     COMMENT '审核时间',
    `apply_time`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '申请时间',
    `reason`          varchar(500)          DEFAULT NULL COMMENT '原因',
    `description`          varchar(500)          DEFAULT NULL COMMENT '描述',
    `created_at`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`     tinyint(4)   NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY idx_flow_temp_id (flow_temp_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_edit_target_id (edit_target_id),
    KEY idx_applicant_id (applicant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='工作流-实例表';
drop table if exists xbbcloud.workflow_form_template;
CREATE TABLE xbbcloud.`workflow_form_template`
(
    `id`               bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `ins_id`        bigint(20)    NOT NULL DEFAULT 0 COMMENT '审批实例表ID',
    `flow_temp_id`   bigint(20)    NOT NULL DEFAULT 0 COMMENT '流程模板id',
    `form_temp_id`   bigint(20)    NOT NULL DEFAULT 0 COMMENT '表单模板id',
    `v0`    varchar (500) DEFAULT NULL COMMENT '虚拟字段',
    `v1`    varchar (500) DEFAULT NULL COMMENT '虚拟字段',
    `v2`    varchar (500) DEFAULT NULL COMMENT '虚拟字段',
    `v3`    varchar (500) DEFAULT NULL COMMENT '虚拟字段',
    `v4`    varchar (500) DEFAULT NULL COMMENT '虚拟字段',
    `v5`    varchar (500) DEFAULT NULL COMMENT '虚拟字段',
    `v6`    varchar (500) DEFAULT NULL COMMENT '虚拟字段',
    `v7`    varchar (500) DEFAULT NULL COMMENT '虚拟字段',
    `v8`    varchar (500) DEFAULT NULL COMMENT '虚拟字段',
    `v9`    varchar (500) DEFAULT NULL COMMENT '虚拟字段',
    `ext`    varchar (500) DEFAULT NULL COMMENT '虚拟扩展字段',
    `form_json`    varchar (1000) DEFAULT NULL COMMENT 'Json字段',
    `created_at`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`     tinyint(4)   NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`),
    KEY `idx_ins_id` (`ins_id`),
    KEY idx_flow_temp_id (flow_temp_id),
    KEY idx_v0 (v0),
    KEY idx_v1 (v1),
    KEY idx_v2 (v2),
    KEY idx_v3 (v3),
    KEY idx_v4 (v4)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='工作流-表单表';

drop table if exists xbbcloud.workflow_node;
CREATE TABLE xbbcloud.`workflow_node`
(
    `id`               bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`        bigint(20)   NOT NULL DEFAULT 0 COMMENT '租户id',
    `ins_id`        bigint(20)   NOT NULL COMMENT '审批实例表ID',
    `node_title`        varchar(100) NOT NULL DEFAULT '' COMMENT '节点名称',
    `sign_type`   tinyint(4)   NOT NULL DEFAULT '1' COMMENT '签署类型 1会签,2. 或签',
    `approve_mode`   tinyint(4)   NOT NULL DEFAULT '1' COMMENT '审批模式 1人工，2自动通过，3自动拒绝',
    `node_type`   tinyint(4)   NOT NULL DEFAULT '1' COMMENT '节点类型 1. 审批人，2.抄送人，3. 办理人，4条件分支',
    `assignee_id`      bigint(20)   NOT NULL DEFAULT '0' COMMENT '审核人ID',
    `status`           tinyint(4)   NOT NULL DEFAULT '0' COMMENT '审批状态 10审批中 20审批通过 30审批驳回',
    `run_status`       tinyint(4)   NOT NULL DEFAULT '0' COMMENT '节点状态 0未发起 1运行中 2挂起 3已结束',
    `approve_time`       timestamp     COMMENT '审核时间',
    `reason`          varchar(500)          DEFAULT NULL COMMENT '原因',
    `description`          varchar(500)          DEFAULT NULL COMMENT '描述',
    `node_level`           tinyint(4)   NOT NULL DEFAULT '99' COMMENT '节点优先级 0为初始节点',
    `pre_node`           tinyint(4)   NOT NULL DEFAULT '0' COMMENT '上一个节点 初始节点为0',
    `next_node`           tinyint(4)   NOT NULL DEFAULT '0' COMMENT '下一个节点 末节点为0表示结束',
    `created_at`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`     tinyint(4)   NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`),
    KEY `idx_ins_id` (`ins_id`),
    KEY `idx_ins_id_status` (ins_id,status,node_level)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='工作流-节点表';

drop table if exists xbbcloud.workflow_process;
CREATE TABLE xbbcloud.`workflow_process`
(
    `id`               bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `ins_id`        bigint(20)   NOT NULL COMMENT '审批实例表ID',
    `node_id`        bigint(20)   NOT NULL COMMENT '当前运行到的节点ID',
    `approve_time`       timestamp     COMMENT '审核时间',
    `assignee_id`      bigint(20)   NOT NULL DEFAULT '0' COMMENT '审核人ID',
    `status`           tinyint(4)   NOT NULL DEFAULT '1' COMMENT '审批状态 10审批中 20审批通过 30审批驳回',
    `run_status`       tinyint(4)   NOT NULL DEFAULT '1' COMMENT '节点状态 1运行中 2挂起 3已结束',
    `created_at`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`     tinyint(4)   NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`),
    KEY `idx_ins_id` (`ins_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='工作流-进程表';