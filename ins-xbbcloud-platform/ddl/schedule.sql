drop table if exists xbbcloud.schedule;
CREATE TABLE xbbcloud.schedule
(
    `id`            BIGINT(20) UNSIGNED not null auto_increment PRIMARY KEY COMMENT '主键id',
    `tenant_id`     INT(11) UNSIGNED    NOT NULL DEFAULT '0' COMMENT '租户id',
    `org_id`        BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '部门id',
    `start_date`    date                NOT NULL COMMENT '开始日期 yyyyMMdd',
    `end_date`      date                NOT NULL COMMENT '结束日期 yyyyMMdd ',
    `publish_state` TINYINT(3)          NOT NULL DEFAULT '0' COMMENT '发布状态 0未发布 1发布',
    `lock_state`    TINYINT(2)          NOT NULL DEFAULT '0' COMMENT '锁定状态 0未锁定 1锁定',
    `create_by`     bigint(20)          NOT NULL DEFAULT '0' COMMENT '创建人',
    `modify_by`     bigint(20)          NOT NULL DEFAULT '0' COMMENT '更新人',
    `created_at`    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`  TINYINT(3)          NOT NULL DEFAULT '0' COMMENT '删除标识：0未删除 1已删除',
    key idx_org (org_id)
) COMMENT '排班-总表';
drop table if exists xbbcloud.schedule_detail;
CREATE TABLE xbbcloud.schedule_detail
(
    `id`               BIGINT(20) UNSIGNED not null auto_increment PRIMARY KEY COMMENT '主键id',
    `tenant_id`        INT(11) UNSIGNED    NOT NULL DEFAULT '0' COMMENT '租户id',
    `org_id`           BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '部门id',
    `work_schedule_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '排班班次id',
    `employee_id`      BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '员工id',
    `start_datetime`   TIMESTAMP           NOT NULL COMMENT '开始时间',
    `end_datetime`     TIMESTAMP           NOT NULL COMMENT '结束时间',
    `day_of_week`      TINYINT(2)          NOT NULL COMMENT '周一-周日 从1开始',
    `create_by`        bigint(20)          NOT NULL DEFAULT '0' COMMENT '创建人',
    `modify_by`        bigint(20)          NOT NULL DEFAULT '0' COMMENT '更新人',
    `created_at`       TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`     TINYINT(3)          NOT NULL DEFAULT '0' COMMENT '删除标识：0未删除 1已删除',
    key idx_schedule_id (work_schedule_id),
    key idx_org_employee (org_id, employee_id)
) COMMENT '排班-详情表';
drop table if exists xbbcloud.schedule_read_record;
CREATE TABLE xbbcloud.schedule_read_record
(
    `id`               BIGINT(20) UNSIGNED not null auto_increment PRIMARY KEY COMMENT '主键id',
    `tenant_id`        INT(11) UNSIGNED    NOT NULL DEFAULT '0' COMMENT '租户id',
    `work_schedule_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '排班班次id',
    `employee_id`      BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '员工id',
    `create_by`        bigint(20)          NOT NULL DEFAULT '0' COMMENT '创建人',
    `modify_by`        bigint(20)          NOT NULL DEFAULT '0' COMMENT '更新人',
    `created_at`       TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`     TINYINT(3)          NOT NULL DEFAULT '0' COMMENT '删除标识：0未删除 1已删除',
    key idx_schedule_id (work_schedule_id)
) COMMENT '排班-C端用户已读记录表';
drop table if exists xbbcloud.common_setting;
CREATE TABLE xbbcloud.common_setting
(
    `id`           BIGINT(20) UNSIGNED not null auto_increment PRIMARY KEY COMMENT '主键id',
    `tenant_id`    INT(11) UNSIGNED    NOT NULL DEFAULT '0' COMMENT '租户id',
    `type`         TINYINT(4) UNSIGNED NOT NULL DEFAULT '0' COMMENT '配置类型 0默认 1排班预警',
    `config_info`  json                         DEFAULT NULL COMMENT '配置信息json字符串',
    `create_by`    bigint(20) UNSIGNED NOT NULL DEFAULT '0' COMMENT '创建人',
    `modify_by`    bigint(20)          NOT NULL DEFAULT '0' COMMENT '更新人',
    `created_at`   TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(3)          NOT NULL DEFAULT '0' COMMENT '删除标识：0未删除 1已删除',
    key idx_tenant (tenant_id)
) COMMENT '通用设置表';
CREATE TABLE xbbcloud.`dictionary`
(
    `id`           bigint(20) unsigned NOT NULL COMMENT 'id',
    `tenant_id`    int(10) unsigned    NOT NULL DEFAULT '0' COMMENT '租户id，租户id为0表示是系统级的配置',
    `type`         int(10) unsigned    NOT NULL DEFAULT '0' COMMENT '类型 1用工类型 2招聘渠道',
    `parent_code`  int(10) unsigned    NOT NULL DEFAULT '0' COMMENT '父级编码',
    `code`         int(10) unsigned    NOT NULL DEFAULT '0' COMMENT '编码',
    `name`         varchar(256)        NOT NULL DEFAULT '' COMMENT '名称',
    `description`  varchar(1000)       NOT NULL DEFAULT '' COMMENT '描述',
    `sort`         int(10)             NOT NULL DEFAULT '0' COMMENT '排序值',
    `created_at`   bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
    `updated_at`   bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
    `create_by`    bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建人',
    `modify_by`    bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '更新人',
    `deleted_flag` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
    key idx_tenant (tenant_id),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='字典表';
-- 默认字典
INSERT INTO `xbbcloud`.`dictionary`(`id`, `tenant_id`, `type`, `parent_code`, `code`, `name`, `description`, `sort`,
                                    `created_at`, `updated_at`, `create_by`, `modify_by`, `deleted_flag`)
VALUES (1, 0, 1, 0, 1, '全职', '用工类型-全职', 1, 0, 0, 0, 0, 0);

INSERT INTO `xbbcloud`.`dictionary`(`id`, `tenant_id`, `type`, `parent_code`, `code`, `name`, `description`, `sort`,
                                    `created_at`, `updated_at`, `create_by`, `modify_by`, `deleted_flag`)
VALUES (2, 0, 1, 0, 2, '兼职', '用工类型-兼职', 2, 0, 0, 0, 0, 0);

INSERT INTO `xbbcloud`.`dictionary`(`id`, `tenant_id`, `type`, `parent_code`, `code`, `name`, `description`, `sort`,
                                    `created_at`, `updated_at`, `create_by`, `modify_by`, `deleted_flag`)
VALUES (3, 0, 2, 0, 1, '勋厚人力', '招聘渠道-勋厚人力', 1, 0, 0, 0, 0, 0);

ALTER TABLE `xbbcloud`.`schedule_read_record`
    ADD UNIQUE INDEX `uk_schedule` (`work_schedule_id`, `employee_id`) USING BTREE;
