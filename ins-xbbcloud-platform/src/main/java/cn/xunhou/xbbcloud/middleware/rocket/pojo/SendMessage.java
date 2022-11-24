package cn.xunhou.xbbcloud.middleware.rocket.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: chenning
 * @Date: 2022/10/08/10:59
 * @Description:
 */
@Data
@ToString
@NoArgsConstructor
public class SendMessage implements Serializable {

    private Long flowTemplateId;

    private Long insId;
    /**
     * 通知人
     */
    private List<Long> noticeTo;

    /**
     * 通知电话
     */
    private List<String> noticeTels;

    private String msg;

    /**
     * 通知方式 0钉钉 1企微 2短信
     */
    private List<Integer> noticeFun;

    private String smsTemplateCode;

    private String smsMsg;

    private String weChatContext;

}
