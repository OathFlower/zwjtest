package cn.xunhou.web.xbbcloud.common.helper;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.framework.util.SystemUtil;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.grpc.proto.universal.UniversalServiceGrpc;
import cn.xunhou.grpc.proto.universal.UniversalServiceProto;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


/**
 * @author litb
 * @date 2022/8/2 13:52
 */
@Slf4j
@Component
public class NotifyHelper {
    @GrpcClient("ins-xhwallet-platform")
    private UniversalServiceGrpc.UniversalServiceBlockingStub universalServiceBlockingStub;

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 基于模板编码+tel发送短信,异步队列发送
     *
     * @param _params      参数
     * @param templateCode 模板code
     * @param tel          手机号
     */
    public void rtSendSmsByTel(String templateCode, String tel, Map<String, Object> _params) {
        UniversalServiceGrpc.UniversalServiceBlockingStub universalServiceBlockingStub =
                UniversalServiceGrpc.newBlockingStub(SpringContextUtil.getBean("universalChannel", Channel.class));
        try {
            //noinspection ResultOfMethodCallIgnored
            universalServiceBlockingStub.sendSmsMessage(UniversalServiceProto.SendMessageBeRequest.newBuilder()
                    .setTemplateCode(templateCode)
                    .setTel(tel)
                    .setContent(XbbJsonUtil.toJsonString(_params))
                    .build());
        } catch (Exception e) {
            log.error("发送短信失败,errorMsg = {}", e.getMessage());
        }

    }

    /**
     * 基于模板编码+tel 钉钉群消息
     *
     * @param text
     */
    public void sendDdMessage(String text) {
        sendDdMessage(text, null);
    }

    public void sendDdMessage(String text, String mobiles) {
        try {
            if (!SystemUtil.isFormalArea()) {
                text = "【" + SystemUtil.getLogicAreaStr() + "】-【" + applicationName + "】 " + text;
            }
            //https://oapi.dingtalk.com/robot/send?access_token=de90d03669a8d3cef3663189b1601c72c6e27b3f0c0c911fddea778c03815d37
            log.info("发送钉钉消息 :{}", text);
            UniversalServiceProto.DingtalkMsgInfoBeRequest.Builder builder = UniversalServiceProto.DingtalkMsgInfoBeRequest.newBuilder();
            if (CharSequenceUtil.isNotBlank(mobiles)) {
                List<String> mobilesList = StrUtil.split(mobiles, ",");
                builder.addAllAtMobiles(mobilesList);
            }
            universalServiceBlockingStub.sendDingtalkMsg(UniversalServiceProto.
                    DingtalkMsgBeRequest.newBuilder()
                    .setSecret("SECd24011a4aded70b94a1374ce36133225c1814802549858521b89599d271a2a84")
                    .setAccessToken("cbf66284930322dd79cb00e8147bc44227ec0a7014c82ddeb7f14221cc7e5f43")
                    .setMsgInfo(builder
                            .setAtAll(true)
                            .setType(UniversalServiceProto.DingtalkMsgTypeEnum.TEXT)
                            .setContent(text)
                            .build())
                    .build());
        } catch (Exception e) {
            e.getMessage();
            log.error("发送钉钉消息异常 =" + text, e);
        }

    }


}
