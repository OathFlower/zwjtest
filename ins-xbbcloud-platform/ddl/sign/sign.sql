CREATE TABLE `xbbcloud`.`sign_info`
(
    `id`                   bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '租户ID',
    `use_to_date`          timestamp           NOT NULL COMMENT '到期时间',
    `customer_type`        tinyint(4)          NOT NULL DEFAULT '1' COMMENT '客户类型：1-内部',
    `remarks`              varchar(500)                 DEFAULT NULL COMMENT '备注',
    `operator_id`          bigint(20)          NOT NULL DEFAULT '0' COMMENT '操作人',
    `business_contract_id` bigint(20)                   DEFAULT NULL COMMENT '商务合同id',
    `ext_info`             json                         DEFAULT NULL COMMENT '扩展信息',
    `created_at`           timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`           timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`         tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='签约云-基本信息';


CREATE TABLE `xbbcloud`.`sign_relation_project`
(
    `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '批次id',
    `sign_info_id` bigint(20)          NOT NULL DEFAULT '0' COMMENT '签约云-基本信息ID',
    `project_id`   varchar(100)        NOT NULL DEFAULT '0' COMMENT '项目ID',
    `remarks`      varchar(500)                 DEFAULT NULL COMMENT '备注',
    `operator_id`  bigint(20)          NOT NULL DEFAULT '0' COMMENT '操作人',
    `created_at`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uni_tenant_project_id` (`sign_info_id`, `project_id`, `deleted_flag`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='签约云-租户项目关联关系';


CREATE TABLE `xbbcloud`.`contract`
(
    `id`                  bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `template_id`         int(11) unsigned    NOT NULL DEFAULT '0' COMMENT '模板id',
    `employee_id`         bigint(20)          NOT NULL DEFAULT '0' COMMENT '员工id',
    `id_card_no`          varchar(255)        NOT NULL DEFAULT '' COMMENT '身份证号 加解密',
    `subject_id`          int(11)             NOT NULL DEFAULT '0' COMMENT '签署主体id',
    `tenant_id`           bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '租户ID',
    `type`                tinyint(4)          NOT NULL DEFAULT '0' COMMENT '类型 1合同，2协议 后期废除',
    `contract_no`         varchar(128)        NOT NULL DEFAULT '' COMMENT '编号',
    `sign_date`           timestamp           NULL     DEFAULT NULL COMMENT '签约日期',
    `status`              tinyint(8)          NOT NULL DEFAULT '0' COMMENT '状态 待签署0 待确认1 待生效2 生效中3  已到期4 已作废5  提前终止6',
    `source`              tinyint(6)          NOT NULL DEFAULT '0' COMMENT '数据来源 0岗位二维码签约',
    `source_business_id`  bigint(20)          NOT NULL DEFAULT '0' COMMENT '根据数据来源关联业务id',
    `template_json`       text                NOT NULL COMMENT '用户及企业参数json',
    `start_time`          timestamp           NULL     DEFAULT NULL COMMENT '合同开始时间',
    `end_time`            timestamp           NULL     DEFAULT NULL COMMENT '合同截止时间',
    `service_business_id` varchar(255)        NOT NULL DEFAULT '' COMMENT '第三方合同平台对应业务id',
    `contract_oss_id`     varchar(256)        NOT NULL DEFAULT '' COMMENT '电子合同文件系统id',
    `created_at`          timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`        tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='合同协议表';


CREATE TABLE `xbbcloud`.`position_qrcode`
(
    `id`                     bigint(20) unsigned      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`              bigint(20) unsigned      NOT NULL DEFAULT '0' COMMENT '租户ID',
    `hro_position_id`        int(11) unsigned         NOT NULL DEFAULT '0' COMMENT '人力的岗位ID',
    `social_insurance`       tinyint(3) unsigned      NOT NULL DEFAULT '0' COMMENT '0不缴纳社保 1缴纳社保',
    `template_json`          text CHARACTER SET utf32 NOT NULL COMMENT '企业动态表单json',
    `expire_date`            timestamp                NOT NULL COMMENT '二维码过期时间',
    `subject_id`             int(11) unsigned         NOT NULL DEFAULT '0' COMMENT '企业合同主体id',
    `contract_template_type` tinyint(10)              NOT NULL DEFAULT '0' COMMENT '合同模板类型',
    `remark`                 varchar(255)             NOT NULL DEFAULT '' COMMENT '备注',
    `operator_id`            bigint(20) unsigned      NOT NULL DEFAULT '0' COMMENT '操作人员id',
    `created_at`             timestamp                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`             timestamp                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`           tinyint(4)               NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='岗位-二维码表';


CREATE TABLE `xbbcloud`.`position_contract_template`
(
    `id`                   bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `position_qrcode_id`   bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '岗位二维码ID',
    `contract_template_id` int(11) unsigned    NOT NULL DEFAULT '0' COMMENT '合同模板(合同/协议)ID',
    `type`                 tinyint(4)          NOT NULL DEFAULT '0' COMMENT '类型 1合同，2协议 后期废除',
    `created_at`           timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`           timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`         tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='岗位-合同模板-关系表';


CREATE TABLE `xbbcloud`.`position_qrcode_user`
(
    `id`                 bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `position_qrcode_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '二维码ID',
    `tel`                varchar(255)        NOT NULL DEFAULT '' COMMENT '手机号 加密',
    `created_at`         timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`         timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`       tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='岗位=二维码-签署白名单人员表';