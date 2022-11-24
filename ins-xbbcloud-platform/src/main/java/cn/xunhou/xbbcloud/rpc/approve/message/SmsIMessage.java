package cn.xunhou.xbbcloud.rpc.approve.message;

import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.grpc.proto.universal.UniversalServiceGrpc;
import cn.xunhou.grpc.proto.universal.UniversalServiceProto;
import cn.xunhou.xbbcloud.common.constants.WorkflowConstant;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.SendMessage;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @Author: chenning
 * @Date: 2022/09/28/14:25
 * @Description:
 */
@Slf4j
@Component
public class SmsIMessage implements IMessage {


    @GrpcClient("ins-xhwallet-platform")
    private UniversalServiceGrpc.UniversalServiceBlockingStub universalServiceBlockingStub;
    @Override
    public void send(SendMessage context) {
        log.info("发送sms提醒给:"+ XbbJsonUtil.toJsonString(context.getNoticeTo()));

        UniversalServiceProto.SendMessageBeRequest.Builder builder = UniversalServiceProto.SendMessageBeRequest.newBuilder()
                .setTenantId(10000)
                .addAllTels(context.getNoticeTels())
                .setTemplateCode(context.getSmsTemplateCode());
        if (StringUtils.isNotBlank(context.getSmsMsg())){
            builder.setContent(context.getSmsMsg());
        }
        log.info("sms request:"+ builder.build());

        universalServiceBlockingStub.sendSmsMessage(builder.build());
    }

    @Override
    public Integer getType() {
        return WorkflowConstant.SMS;
    }
}
