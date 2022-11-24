package cn.xunhou.xbbcloud.common.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.xunhou.common.tools.util.SpringContextUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author wangkm
 */
public class RedisUtil {
    /**
     * 生成编号
     *
     * @param stepSpan 编号个数
     * @return 编号集合
     */
    public static List<String> generateNos(String key, Integer stepSpan) {
        if (stepSpan < 0) {
            throw ExceptionUtil.wrapRuntime("步长大于0");
        }
        if (CharSequenceUtil.isBlank(key)) {
            throw ExceptionUtil.wrapRuntime("主键不能为空");
        }

        RedisTemplate<String, String> redisTemplate = SpringContextUtil.getBean("redisTemplate");
        String date = DateUtil.format(DateUtil.date(), DatePattern.PURE_DATE_PATTERN);
        ValueOperations operations = redisTemplate.opsForValue();
        String start = key + ":" + date;
        List<String> stringList = new ArrayList<>(stepSpan);
        long max = 100000;
        for (int i = 0; i < stepSpan; i++) {
            long count = operations.increment(start);
            Long v = max + count;
            String no = (v + "").substring(1, (max + "").length());
            stringList.add(key + date + no);
        }
        redisTemplate.expire(start, 1, TimeUnit.DAYS);
        return stringList;
    }
}
