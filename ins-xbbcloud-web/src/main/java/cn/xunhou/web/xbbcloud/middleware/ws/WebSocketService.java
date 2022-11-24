package cn.xunhou.web.xbbcloud.middleware.ws;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint(value = "/websocket")
public class WebSocketService {
    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    public static ConcurrentHashMap<String, WebSocketService> webSocketMap = new ConcurrentHashMap<>();
    /**
     * 与客户端连接的session
     */
    private Session session;

    /**
     * 客户端id
     */
    private String id = "";

    /**
     * 发送自定义消息
     */
    public static void sendInfo(String message, String id) {
        log.info("发送给客户端:" + id + ",报文:" + message);
        if (CharSequenceUtil.isNotBlank(id) && webSocketMap.containsKey(id)) {
            webSocketMap.get(id).sendMessage(message);
        } else {
            log.error("客户端:" + id + ",不在线");
        }
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("pointSN") String id) {
        this.id = id;
        this.session = session;

        if (webSocketMap.containsKey(id)) {
            webSocketMap.remove(id);

            webSocketMap.put(id, this);
        } else {

            webSocketMap.put(id, this);
        }

        log.info("客户端:" + id + ",连接成功");
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if (webSocketMap.containsKey(id)) {
            webSocketMap.remove(id);

        }
        log.info("客户端:" + id + ",断开连接");
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("客户端:" + id + ",报文:" + message);

        sendMessage("收到");
    }

    /**
     * @param session
     * @param e
     */
    @OnError
    public void onError(Session session, Throwable e) {

        log.error("连接错误:" + this.id, e);
    }

    public void sendMessage(String message) {
//        try {
        this.session.getAsyncRemote().sendText(message);
//        } catch (IOException e) {
//            log.error("发送客户端:" + id + ",异常", e);
//        }
    }

}
