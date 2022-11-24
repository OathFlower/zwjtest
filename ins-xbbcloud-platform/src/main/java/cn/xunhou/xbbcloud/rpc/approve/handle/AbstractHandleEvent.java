package cn.xunhou.xbbcloud.rpc.approve.handle;

import cn.xunhou.xbbcloud.middleware.rocket.pojo.SendMessage;
import cn.xunhou.xbbcloud.rpc.approve.bean.Context;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author: chenning
 * @Date: 2022/09/28/15:46
 * @Description:
 */
@Slf4j
public abstract class AbstractHandleEvent implements HandleEvent {

    protected boolean validCtx(Context ctx) {
        List<Integer> noticeFun = ctx.getNoticeFun();
//        List<Long> noticeTo = ctx.getNoticeTo();
        if (CollectionUtils.isEmpty(noticeFun)) {
            log.info("没有获取到通知方式或者通知人");
            return false;
        }
        return true;
    }

    @Override
    public abstract void addHandle(Context ctx);

    @Override
    public abstract void passHandle(Context ctx);

    @Override
    public abstract void rejectHandle(Context ctx);

    @Override
    public abstract void editHandle(Context ctx);

//    public void sendMessage(Context ctx){
//        if (!validCtx(ctx)) {
//            return;
//        }
//        sendMessage();
//       ctx.getNoticeFun().forEach(v->{
//           if (ctx.getMessageMap().get(v) != null){
//               ctx.getMessageMap().get(v).send(ctx);
//           }
//       });
//    }
    protected SendMessage buildSendMessage(Context ctx){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setMsg(ctx.getMsg());
        sendMessage.setNoticeFun(ctx.getNoticeFun());
        sendMessage.setNoticeTels(ctx.getNoticeTels());
        sendMessage.setNoticeTo(ctx.getNoticeTo());
        sendMessage.setSmsTemplateCode(ctx.getSmsTemplateCode());
        sendMessage.setSmsMsg(ctx.getSmsMsg());
        sendMessage.setWeChatContext(ctx.getNoticeContext());
        return sendMessage;
    }

}
