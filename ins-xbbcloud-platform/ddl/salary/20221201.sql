ALTER TABLE `xbbcloud`.`fund_dispatching`
    ADD COLUMN `ext_info` JSON NULL COMMENT '扩展信息';
ALTER TABLE `xbbcloud`.`fund_dispatching`
    ADD INDEX `index_main_type` (`transaction_main`, `capital_type`, `dispatch_status`, `deleted_flag`) USING BTREE;
ALTER TABLE `xbbcloud`.`fund_dispatching`
    MODIFY COLUMN `capital_type` tinyint(4) NOT NULL DEFAULT 10 COMMENT '交易类型-税费10；服务费20；实发金额30；退回税费11；退回服务费21；退回实发金额31； 其它99;' AFTER `transaction_main`,
    MODIFY COLUMN `dispatch_status` tinyint(4) NOT NULL DEFAULT 10 COMMENT '状态-未发起10；挂起11；交易中20；成功30；失败40' AFTER `failure_reason`;

ALTER TABLE `xbbcloud`.`salary_batch`
    ADD COLUMN `subject_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '发薪主体id' AFTER `product_id`;

ALTER TABLE `xbbcloud`.`salary_batch`
    ADD COLUMN `expand_json` text NULL COMMENT '扩展数据json' AFTER `product_id`;

ALTER TABLE `xbbcloud`.`salary_detail`
    ADD COLUMN `retry_count` int(11) NOT NULL DEFAULT '0' COMMENT '重试次数' AFTER `remark`;

ALTER TABLE `xbbcloud`.`salary_batch`
    MODIFY COLUMN `status` tinyint(8) NOT NULL DEFAULT '0' COMMENT '状态  0支付处理中 1已发薪 2部分失败 3全部失败';

ALTER TABLE `xbbcloud`.`salary_batch`
    ADD COLUMN `pay_method` tinyint(8) NOT NULL DEFAULT 1 COMMENT '发薪方式 1小程序提现  2微信转账' AFTER `status`;

ALTER TABLE `xbbcloud`.`salary_detail`
    MODIFY COLUMN `status` tinyint(8) NOT NULL DEFAULT '0' COMMENT '状态 0支付处理中(未认证)  1支付处理中(已下单)  2已发薪   3支付失败   4待提现   5提现中  6撤回中  7已撤回  8撤回失败  9提现失败 10提现成功';
ALTER TABLE `xbbcloud`.`salary_detail`
    ADD COLUMN `expand_json` text NULL COMMENT '扩展数据json' AFTER `remark`;