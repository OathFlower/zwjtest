CREATE TABLE `xbbcloud`.`salary_batch`
(
    `id`           bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '批次id',
    `tenant_id`    bigint(20) UNSIGNED NOT NULL DEFAULT '0' COMMENT '租户ID',
    `product_id`   bigint(20) UNSIGNED NOT NULL DEFAULT '0' COMMENT '项目ID',
    `month`        varchar(64)         NOT NULL DEFAULT '' COMMENT '计薪年月 yyyyMM',
    `status`       tinyint(8)          NOT NULL DEFAULT '0' COMMENT '状态  0进行中 1全部成功 2部分成功 3全部失败',
    `operator_id`  bigint(20) UNSIGNED NOT NULL DEFAULT '0' COMMENT '操作人id',
    `remarks`      varchar(500)                 DEFAULT NULL COMMENT '备注',
    `salary_file`  varchar(1000)       NOT NULL DEFAULT '' COMMENT '发薪文件',
    `created_at`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`),
    KEY `IDX_salary_batch_tenant` USING BTREE (`tenant_id`, `product_id`, `status`, `created_at`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARSET = utf8mb4 COMMENT '发薪批次表';

CREATE TABLE `xbbcloud`.`salary_detail`
(
    `id`             bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '详情ID',
    `batch_id`       bigint(20)          NOT NULL DEFAULT '0' COMMENT '批次id',
    `status`         tinyint(8)          NOT NULL DEFAULT '0' COMMENT '状态 0支付处理中（未认证） 1支付处理中（已下单） 1已发薪 2支付失败',
    `name`           varchar(255)        NOT NULL DEFAULT '' COMMENT '姓名',
    `id_card_no`     varchar(255)        NOT NULL DEFAULT '' COMMENT '身份证号 加解密',
    `open_id`        varchar(100)                 DEFAULT '' COMMENT 'openid',
    `phone`          varchar(200)        NOT NULL DEFAULT '' COMMENT '手机号',
    `payable_amount` int(11)             NOT NULL DEFAULT '0' COMMENT '应发金额（分）',
    `paid_in_amount` int(11)             NOT NULL DEFAULT '0' COMMENT '实发金额（分）',
    `tax_amount`     int(11)             NOT NULL DEFAULT '0' COMMENT '个税（分）',
    `other_deduct`   int(11)             NOT NULL DEFAULT '0' COMMENT '其他扣除（分）',
    `failure_reason` varchar(500)        NOT NULL DEFAULT '' COMMENT '失败原因',
    `remark`         varchar(255)        NOT NULL DEFAULT '' COMMENT '备注',
    `operator_id`    bigint(20) UNSIGNED NOT NULL DEFAULT '0' COMMENT '操作人id',
    `created_at`     timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`   tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`),
    KEY `IDX_query_batch` USING BTREE (`batch_id`, `status`, `name`, `id_card_no`, `phone`, `created_at`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARSET = utf8mb4 COMMENT '发薪明细表';

CREATE TABLE `xbbcloud`.`salary_merchant_flow`
(
    `id`               bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`        bigint(20)   NOT NULL COMMENT '租户id',
    `flow_no`          varchar(200) NOT NULL COMMENT '流水编号 支出类 批次的编号  收入类是 微信充值流水',
    `operation_type`   tinyint(4)   NOT NULL DEFAULT '1' COMMENT '操作类型 1收入 2支出',
    `operator_id`      bigint(20)   NOT NULL DEFAULT '0' COMMENT '操作人',
    `operation_amount` int(11)      NOT NULL DEFAULT '0' COMMENT '操作金额（分）',
    `remarks`          varchar(500)          DEFAULT NULL COMMENT '备注',
    `salary_batch_id`  bigint(20)            DEFAULT NULL COMMENT '交易批次id',
    `created_at`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`     tinyint(4)   NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`),
    UNIQUE `uni_flow_no` (`tenant_id`, `flow_no`, `deleted_flag`),
    KEY `idx_merchant_id_operation_type` USING BTREE (`tenant_id`, `operation_type`, `deleted_flag`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARSET = utf8mb4 COMMENT '薪酬云-商户号流水表';

CREATE TABLE `xbbcloud`.`salary_merchant_info`
(
    `id`                  bigint(20)     NOT NULL AUTO_INCREMENT COMMENT '租户id',
    `is_use`              tinyint(4)     NOT NULL DEFAULT '1' COMMENT '是否启用 1启用 2不启用',
    `use_to_date`         timestamp      NOT NULL COMMENT '到期时间',
    `tenant_type`         tinyint(4)     NOT NULL DEFAULT '1' COMMENT '租户类型',
    `payee_merchant_no`   varchar(100)   NOT NULL COMMENT '商户号',
    `payee_merchant_name` varchar(200)   NOT NULL COMMENT '商户号收款主体',
    `contract_file_id`    varchar(100)            DEFAULT NULL COMMENT '商户合同文件id',
    `service_rate`        float(10, 2)            DEFAULT '0.00' COMMENT '服务费率',
    `is_approval`         tinyint(4)     NOT NULL DEFAULT '1' COMMENT '审批能力 1启用 2不启用',
    `individual_tax`      tinyint(4)     NOT NULL DEFAULT '2' COMMENT '个税能力 1启用 2不启用',
    `payroll_method`      set ('1', '2') NOT NULL COMMENT '发薪方式 1无卡发薪、2有卡发薪',
    `certification_type`  tinyint(4)     NOT NULL DEFAULT '1' COMMENT '认证类型 1二要素认证、2信息+人脸认证',
    `service_merchant_no` varchar(100)   NOT NULL DEFAULT '' COMMENT '服务商信息',
    `special_merchant_id` varchar(100)   NOT NULL COMMENT '特约商户id',
    `remarks`             varchar(500)            DEFAULT NULL COMMENT '备注',
    `operator_id`         bigint(20)     NOT NULL DEFAULT '0' COMMENT '操作人',
    `created_at`          timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`        tinyint(4)     NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARSET = utf8mb4 COMMENT '薪酬云-商户信息表';

CREATE TABLE `xbbcloud`.`salary_openid`
(
    `id`           bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `user_xh_c_id` bigint(20) UNSIGNED NOT NULL DEFAULT '0' COMMENT '用户Cid',
    `id_card_no`   varchar(255)        NOT NULL DEFAULT '' COMMENT '身份证号 加解密',
    `open_id`      varchar(100)                 DEFAULT '' COMMENT 'openid',
    `created_at`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`),
    KEY `IDX_id_card` USING BTREE (`id_card_no`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARSET = utf8mb4 COMMENT '发薪openId表';

CREATE TABLE `xbbcloud`.`salary_product`
(
    `id`           bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`         varchar(64) NOT NULL DEFAULT '' COMMENT '项目名称',
    `tenant_id`    bigint(20) UNSIGNED NOT NULL DEFAULT '0' COMMENT '租户ID',
    `operator_id`  bigint(20) UNSIGNED NOT NULL DEFAULT '0' COMMENT '操作人id',
    `created_at`   timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`),
    KEY            `IDX_tenant_id` USING BTREE (`tenant_id`, `name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARSET = utf8mb4 COMMENT '发薪项目表';



ALTER TABLE xbbcloud.salary_merchant_info
    ADD ext_info json DEFAULT {} NULL COMMENT '扩展信息';
ALTER TABLE xbbcloud.salary_merchant_info CHANGE ext_info ext_info json DEFAULT {} NULL COMMENT '扩展信息' AFTER operator_id;



CREATE TABLE xbbcloud.fund_dispatching
(
    id                 bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    transaction_main   varchar(50)  NOT NULL COMMENT '交易主键-串起交易',
    transaction_type   TINYINT(4) NOT NULL COMMENT '交易类型-税费10；服务费20；实发金额30；其它40',
    failure_reason     varchar(1000) NULL COMMENT '失败原因',
    transaction_status TINYINT(4) DEFAULT 10 NOT NULL COMMENT '交易状态-未发起10；交易中20；交易成功30；交易失败40',
    parent_id          bigint(20) NULL COMMENT '上级交易节点id',
    operator_id        varchar(100) NOT NULL COMMENT '操作人',
    remark1            varchar(100) NOT NULL COMMENT '交易备注-双方可见',
    remark2            varchar(100) NOT NULL COMMENT '交易备注2 支付方可见',
    amount             INTEGER      NOT NULL COMMENT '交易金额',
    payer_id           varchar(100) NOT NULL COMMENT '付方',
    payer_config       json NULL COMMENT '付方信息',
    payee_id           varchar(100) NOT NULL COMMENT '收方',
    payee_config       json NULL COMMENT '收方信息',
    source_type        TINYINT(4) NOT NULL COMMENT '数据来源-薪酬云(代发客户)10',
    retry_count        INTEGER               DEFAULT 0 NOT NULL COMMENT '重试次数',
    `created_at`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`     tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_general_ci
COMMENT='资金调度表';


