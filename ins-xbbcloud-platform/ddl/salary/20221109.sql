CREATE TABLE `xbbcloud`.`fund_dispatching`
(
    `id`                   bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`            bigint(20) unsigned NOT NULL COMMENT '租户id',
    `transaction_main`     varchar(100) NOT NULL COMMENT '交易主键-串起交易',
    `capital_type`         tinyint(4) NOT NULL DEFAULT '10' COMMENT '交易类型-税费10；服务费20；实发金额30；其它99',
    `failure_reason`       varchar(500)          DEFAULT NULL COMMENT '失败原因',
    `dispatch_status`      tinyint(4) NOT NULL DEFAULT '10' COMMENT '状态-未发起10；交易中20；成功30；失败40',
    `parent_id`            bigint(20) DEFAULT NULL COMMENT '上级交易节点id',
    `remark1`              varchar(100)          DEFAULT NULL COMMENT '交易备注-双方可见',
    `remark2`              varchar(100)          DEFAULT NULL COMMENT '交易备注2 支付方可见',
    `operator_id`          bigint(20) NOT NULL DEFAULT '0' COMMENT '操作人',
    `amount`               int(11) NOT NULL DEFAULT '0' COMMENT '交易金额（分）',
    `payer_id`             bigint(20) NOT NULL DEFAULT '0' COMMENT '付方',
    `payer_config`         json                  DEFAULT NULL COMMENT '付方信息',
    `payee_id`             bigint(20) NOT NULL DEFAULT '0' COMMENT '收方 主账号-1',
    `payee_config`         json                  DEFAULT NULL COMMENT '收方信息',
    `source_type`          tinyint(4) NOT NULL DEFAULT '10' COMMENT '数据来源-薪酬云(代发客户)',
    `retry_count`          tinyint(4) NOT NULL DEFAULT '0' COMMENT '重试次数',
    `dispatch_direction`   tinyint(4) NOT NULL DEFAULT '0' COMMENT '调度方向 //公对公 B2B = 0; //公对微信 B2WX = 1; //公对用户 B2C = 2; //微信对用户转账 WX2C = 3;',
    `order_value`          int(10) DEFAULT NULL COMMENT '交易顺序',
    `asset_transaction_id` bigint(20) DEFAULT NULL COMMENT '资金交易id',
    `created_at`           timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`           timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`         tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金调度';

ALTER TABLE `xbbcloud`.`salary_batch`
    ADD `source` TINYINT(3) DEFAULT 0 NOT NULL COMMENT '来源 0SASS 1代发';

ALTER TABLE `xbbcloud`.`salary_batch` MODIFY COLUMN `status` TINYINT(3) COMMENT '状态  0支付处理中 1已发薪 2部分成功 3全部失败';


ALTER TABLE `xbbcloud`.`salary_detail`
    ADD `subject_id` BIGINT DEFAULT 0 NOT NULL COMMENT '发薪主体id';

ALTER TABLE `xbbcloud`.`salary_detail`
    ADD `service_amount` INT DEFAULT 0 NOT NULL COMMENT '服务费';

ALTER TABLE `xbbcloud`.`salary_detail`
    ADD `tenant_id` BIGINT DEFAULT 0 NOT NULL COMMENT '租户id';

ALTER TABLE `xbbcloud`.`salary_detail` MODIFY COLUMN `status` TINYINT(3) COMMENT '状态 0支付处理中(未认证)  1支付处理中(已下单)  2已发薪   3支付失败   4支付处理中(待提现)';


ALTER TABLE `xbbcloud`.`salary_merchant_flow`
    ADD `subject_name` VARCHAR(100) NULL COMMENT '主体名称';

ALTER TABLE `xbbcloud`.`salary_merchant_flow`
    ADD `payee_info_id` BIGINT NULL COMMENT '代发客户(子账户id) - saas(特约商户id)';

ALTER TABLE `xbbcloud`.`salary_merchant_info`
    ADD `payee_sub_account_id` BIGINT NULL COMMENT '收款子账户id';

ALTER TABLE `xbbcloud`.`salary_merchant_info`
    ADD `payer_subject_id` BIGINT DEFAULT 0 NULL COMMENT '发薪主体';

ALTER TABLE `xbbcloud`.`salary_merchant_info`
    ADD `payee_subject_id` BIGINT NULL COMMENT '收款主体id';

ALTER TABLE `xbbcloud`.`salary_merchant_info`
    ADD `ext_info` JSON NULL COMMENT '扩展信息';
