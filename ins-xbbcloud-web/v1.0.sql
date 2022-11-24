CREATE TABLE xbbcloud.`recommend_qrcode`
(
    `id`             bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `qrcode_url`     varchar(526)        NOT NULL DEFAULT '' COMMENT '二维码url',
    `interview_code` varchar(20)         NOT NULL DEFAULT '' COMMENT '邀请码',-- 唯一建
    `operator_id`    bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '操作人员id',
    `created_at`     bigint(20)          NOT NULL DEFAULT '0' COMMENT '创建时间',
    `updated_at`     bigint(20)          NOT NULL DEFAULT '0' COMMENT '更新时间',
    `deleted_flag`   tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`),
    unique index interview_code_index (interview_code)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='推荐二维码表';


CREATE TABLE xbbcloud.`user`
(
    `id`             bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `customer_id`    bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '客户表id',
    `interview_code` varchar(20)         NOT NULL DEFAULT '' COMMENT '邀请码',
    `name`           varchar(50)         NOT NULL DEFAULT '' COMMENT '用户名称',
    `tel`            varchar(100)        NOT NULL DEFAULT '' COMMENT '手机号',-- 唯一
    `admin_type`     tinyint(3)          NOT NULL DEFAULT '0' COMMENT '类型 0普通用户 1企业管理员',
    `operator_id`    bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '操作人id',
    `coin`           int(11)             NOT NULL DEFAULT '0' COMMENT '余额',
    `created_at`     bigint(20)          NOT NULL DEFAULT '0' COMMENT '创建时间',
    `updated_at`     bigint(20)          NOT NULL DEFAULT '0' COMMENT '更新时间',
    `deleted_flag`   tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`),
    unique index tel_index (tel)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

CREATE TABLE xbbcloud.`customer`
(
    `id`            bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `customer_name` varchar(50)                  DEFAULT NULL COMMENT '公司名称',-- 唯一
    `tax_no`        varchar(50)                  DEFAULT NULL COMMENT '公司税号',-- 唯一
    `address`       varchar(50)                  DEFAULT NULL COMMENT '地址',
    `operator_id`   bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '操作人id',
    `coin`          int(11)             NOT NULL DEFAULT '0' COMMENT '余额',
    `created_at`    bigint(20)          NOT NULL DEFAULT '0' COMMENT '创建时间',
    `updated_at`    bigint(20)          NOT NULL DEFAULT '0' COMMENT '更新时间',
    `deleted_flag`  tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`),
    unique index customer_name_index (customer_name),
    unique index tax_no_index (tax_no)
) ENGINE = InnoDB
  AUTO_INCREMENT = 20000
  DEFAULT CHARSET = utf8mb4 COMMENT ='客户表';


CREATE TABLE xbbcloud.`order`
(
    `id`           bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '订单id',
    `title`        varchar(256)                 DEFAULT NULL COMMENT '订单标题',
    `user_id`      bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '用户id',
    `product_id`   bigint(11)                   DEFAULT NULL COMMENT '支付产品id',
    `receipt_id`   bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '发票id',
    `payable_fee`  int(11)                      DEFAULT NULL COMMENT '应付金额(分)',
    `payment_fee`  int(11)                      DEFAULT NULL COMMENT '实付金额(分)',
    `prepay_id`    varchar(100)                 DEFAULT NULL COMMENT '预支付号',
    `wx_status`    varchar(10)                  DEFAULT NULL COMMENT '微信订单状态',
    `created_at`   bigint(20)          NOT NULL DEFAULT '0' COMMENT '创建时间',
    `updated_at`   bigint(20)          NOT NULL DEFAULT '0' COMMENT '更新时间',
    `deleted_flag` tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 100000
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单表';



CREATE TABLE xbbcloud.`wx_payment`
(
    `id`             bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '支付记录id',
    `order_id`       bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '商户支付订单编号',
    `transaction_id` varchar(50)                  DEFAULT NULL COMMENT '支付系统交易编号',
    `payment_type`   varchar(20)                  DEFAULT NULL COMMENT '支付类型',
    `trade_type`     varchar(20)                  DEFAULT NULL COMMENT '交易类型',
    `trade_state`    varchar(50)                  DEFAULT NULL COMMENT '交易状态',
    `payer_total`    int(11)                      DEFAULT NULL COMMENT '支付金额(分)',
    `content`        text COMMENT '通知参数',
    `created_at`     bigint(20)          NOT NULL DEFAULT '0' COMMENT '创建时间',
    `updated_at`     bigint(20)          NOT NULL DEFAULT '0' COMMENT '更新时间',
    `deleted_flag`   tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='微信支付表';

CREATE TABLE xbbcloud.`service_order`
(
    `id`            bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '订单id',
    `title`         varchar(256)                 DEFAULT NULL COMMENT '订单标题',
    `customer_id`   bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '客户表id',
    `user_id`       bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '用户id',
    `product_id`    int(11)                      DEFAULT NULL COMMENT '服务产品id',
    `coin`          int(11)                      DEFAULT NULL COMMENT '虚拟币',
    `status`        tinyint(4)          NOT NULL DEFAULT '0' COMMENT '订单状态  未使用0 核销1',
    `service_type`  tinyint(4)          NOT NULL DEFAULT '0' COMMENT '服务类型  购买服务0 赠送服务1',
    `operator_id`   bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '操作人id',
    `customer_name` varchar(50)                  DEFAULT NULL COMMENT '公司名称',
    `remark`        varchar(500)                 DEFAULT NULL COMMENT '备注',
    `created_at`    bigint(20)          NOT NULL DEFAULT '0' COMMENT '创建时间',
    `updated_at`    bigint(20)          NOT NULL DEFAULT '0' COMMENT '更新时间',
    `deleted_flag`  tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='服务订单表';

CREATE TABLE xbbcloud.`virtual_flow`
(
    `id`           bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `customer_id`  bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '客户表id',
    `user_id`      bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '用户id',
    `flow_type`    tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '状态 0充值 1消费',
    `object_id`    bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '业务id',
    `coin`         int(11)                      DEFAULT NULL COMMENT '虚拟币',
    `created_at`   bigint(20)          NOT NULL DEFAULT '0' COMMENT '创建时间',
    `updated_at`   bigint(20)          NOT NULL DEFAULT '0' COMMENT '更新时间',
    `deleted_flag` tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='虚拟币流水表';


CREATE TABLE xbbcloud.`receipt`
(
    `id`            bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `customer_id`   bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '客户表id',
    `user_id`       bigint(11) unsigned NOT NULL DEFAULT '0' COMMENT '用户id',
    `customer_name` varchar(50)                  DEFAULT NULL COMMENT '公司名称',
    `tax_no`        varchar(50)                  DEFAULT NULL COMMENT '公司税号',
    `address`       varchar(50)                  DEFAULT NULL COMMENT '邮寄地址',
    `total_fee`     int(11)                      DEFAULT NULL COMMENT '开票金额(分)',
    `created_at`    bigint(20)          NOT NULL DEFAULT '0' COMMENT '创建时间',
    `updated_at`    bigint(20)          NOT NULL DEFAULT '0' COMMENT '更新时间',
    `deleted_flag`  tinyint(4)          NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='发票表';
