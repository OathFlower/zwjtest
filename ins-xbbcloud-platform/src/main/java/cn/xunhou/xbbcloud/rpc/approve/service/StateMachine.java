package cn.xunhou.xbbcloud.rpc.approve.service;

import cn.xunhou.xbbcloud.rpc.approve.bean.Context;
import cn.xunhou.xbbcloud.rpc.approve.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: chenning
 * @Date: 2022/09/26/11:29
 * @Description:
 */
@Component
public class StateMachine {

    private Map<Integer, Event> eventMap = new HashMap<>();


    @Autowired
    public StateMachine(List<Event> eventList) {
        for (Event event : eventList) {
            eventMap.put(event.getType(), event);
        }

    }

    public void trigger(Context ctx) {
        Event event = eventMap.get(ctx.getEventType());

        if (event != null) {
            event.init();
            event.beforeTransit(ctx);
            event.transit(ctx);
            event.postTransit(ctx);
        }
    }


}
