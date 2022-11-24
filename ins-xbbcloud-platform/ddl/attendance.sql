-- 考勤打卡记录日志
CREATE TABLE `attendance_record_log`
(
    `id`                   bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `attendance_record_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '任务服务记录id',
    `business_time`        timestamp           NOT NULL DEFAULT '' COMMENT '业务时间',
    `type`                 tinyint(3)          NOT NULL DEFAULT '0' COMMENT '日志类型',
    `content`              varchar(1000)       NOT NULL DEFAULT '' COMMENT '日志内容',
    `create_by`            bigint(20)          NOT NULL DEFAULT '0' COMMENT '创建人',
    `created_at`           timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`           timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`         tinyint(3)          NOT NULL DEFAULT '0' COMMENT '删除标识：0未删除 1已删除',
    key idx_attendance_record_id (attendance_record_id),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='考勤打卡记录日志表';

-- 考勤打卡记录表
CREATE TABLE `xbbcloud`.`attendance_record`
(
    `id`                                     bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '主键',
    `tenant_id`                              int(20) unsigned    NOT NULL DEFAULT '0' COMMENT '租户id',
    `emp_id`                                 bigint(20) unsigned          DEFAULT '0' COMMENT '企业员工id',
    `org_id`                                 bigint(20)          NOT NULL DEFAULT '0' COMMENT '部门id',
    `work_schedule_detail_id`                bigint(20)          NOT NULL DEFAULT '0' COMMENT '排班详情id',
    `clock_in`                               timestamp           NOT NULL COMMENT '上班打卡时间',
    `clock_out`                              timestamp           NOT NULL COMMENT '下班打卡时间',
    `punch_in_attendance_config_address_id`  varchar(200)        NOT NULL DEFAULT '' COMMENT '上班考勤打卡地址配置id',
    `punch_out_attendance_config_address_id` varchar(200)        NOT NULL DEFAULT '' COMMENT '下班考勤打卡地址配置id',
    `punch_in_address`                       varchar(200)        NOT NULL DEFAULT '' COMMENT '上班打卡地址',
    `punch_out_address`                      varchar(200)        NOT NULL DEFAULT '' COMMENT '下班打卡地址',
    `work_hour`                              double(11, 2)       NOT NULL DEFAULT '0.0' COMMENT '打卡工时',
    `actual_hour`                            double(11, 2)       NOT NULL DEFAULT '0.0' COMMENT '实际工时',
    `calculate_unit`                         tinyint(4)          NOT NULL DEFAULT '0' COMMENT '工时计算单位（分钟、半小时、小时）',
    `adjust_work_hour_remark`                varchar(100)        NOT NULL DEFAULT '' COMMENT '调整工时备注',
    `status`                                 tinyint(4)          NOT NULL DEFAULT '0' COMMENT '状态（0上班打卡，1下班打卡，2已审核，3待发薪，4已发薪）',
    `attendance_finish_flag`                 tinyint(4)          NOT NULL DEFAULT '0' COMMENT '考勤是否结束（0可用，1不可用）',
    `create_by`                              bigint(20)          NOT NULL DEFAULT '0' COMMENT '创建人',
    `modified_by`                            bigint(20)          NOT NULL DEFAULT '0' COMMENT '更新人',
    `created_at`                             timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`                             timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`                           tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
    key idx_tenant_id (tenant_id),
    key idx_emp_id (emp_id),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='考勤打卡记录表';


-- 考勤打卡配置表-考勤地点表
CREATE TABLE `attendance_config_address`
(
    `id`               bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `common_config_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '考勤配置id',
    `tenant_id`        bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '租户id',
    `org_id`           bigint(20)          NOT NULL DEFAULT '0' COMMENT '部门id',
    -- 最小值上传控制
    `offset_distance`  int(11)             NOT NULL DEFAULT '0' COMMENT '偏移距离',
    `longitude`        double(11, 6)       NOT NULL DEFAULT '0.000000' COMMENT '地址经度',
    `latitude`         double(11, 6)       NOT NULL DEFAULT '0.000000' COMMENT '地址纬度',
    `location_address` varchar(200)        NOT NULL DEFAULT '' COMMENT '定位地址',
    `address_name`     varchar(100)        NOT NULL DEFAULT '' COMMENT '地址名称',
    `create_by`        bigint(20)          NOT NULL DEFAULT '0' COMMENT '创建人',
    `modified_by`      bigint(20)          NOT NULL DEFAULT '0' COMMENT '更新人',
    `created_at`       timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`     tinyint(3)          NOT NULL DEFAULT '0' COMMENT '删除标识：0未删除 1已删除',
    key idx_config_id (common_config_id),
    key idx_tenant_id (tenant_id),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='考勤打卡配置-考勤地点表';


CREATE TABLE xbbcloud.`attendance_salary_bill_batch`
(
    `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `tenant_id`    int(10) unsigned    NOT NULL DEFAULT '0' COMMENT '租户id',
    `status`       tinyint(4)          NOT NULL DEFAULT '0' COMMENT '状态（0待发送，1发薪待审核，2发薪已审核）',
    `created_at`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` tinyint(3)          NOT NULL DEFAULT '0' COMMENT '删除标识：0未删除 1已删除',

    key idx_tenant_id (tenant_id),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='考勤打卡-薪资账单批次表';



CREATE TABLE xbbcloud.`attendance_salary_bill_batch_detail`
(
    `id`                              bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `tenant_id`                       int(10) unsigned    NOT NULL DEFAULT '0' COMMENT '租户id',
    `salary_attendance_bill_batch_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '薪资账单批次id',
    `attendance_record_id`            bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '考勤打卡记录id',
    `money`                           decimal(11, 4)      NOT NULL DEFAULT '0.0000' COMMENT '金额',
    `pay_type`                        tinyint(4)          NOT NULL DEFAULT '0' COMMENT '发薪方式',
    `bank_card_no`                    varchar(255)        NOT NULL DEFAULT '' COMMENT '银行卡号',
    `bank_name`                       varchar(255)        NOT NULL DEFAULT '' COMMENT '银行卡名称',
    `status`                          tinyint(4)          NOT NULL DEFAULT '0' COMMENT '状态（0待发送，1发薪待审核，2发薪已审核）',
    `sub_status`                      tinyint(4)          NOT NULL DEFAULT '0' COMMENT '子状态（0失败，1成功）',
    `fail_reason`                     varchar(1024)       NOT NULL DEFAULT '' COMMENT '失败原因',
    `created_at`                      timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`                      timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`                    tinyint(3)          NOT NULL DEFAULT '0' COMMENT '删除标识：0未删除 1已删除',
    key idx_tenant_id (tenant_id),
    UNIQUE KEY `idx_attendance_record_id` (`attendance_record_id`) USING BTREE,
    key idx_salary_attendance_bill_batch_id (salary_attendance_bill_batch_id),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='考勤打卡-薪资账单详情表';

