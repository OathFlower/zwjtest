/**
 * Copyright 2022 ab173.com
 */
package cn.xunhou.web.xbbcloud.product.sxz.dto;

import lombok.Data;

import java.util.Date;

@Data
public class WeChatNotify {

    private String transaction_id;
    private Amount amount;
    private String mchid;
    private String trade_state;


    private Date success_time;
    private String out_trade_no;
    private String appid;
    private String trade_state_desc;
    private String trade_type;
    private String attach;


    public static class Amount {
        private Integer payer_total;

        public Integer getPayer_total() {
            return payer_total;
        }

        public void setPayer_total(Integer payer_total) {
            this.payer_total = payer_total;
        }
    }
}