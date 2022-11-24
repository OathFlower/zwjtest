package cn.xunhou.web.xbbcloud.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.xunhou.common.tools.util.SpringContextUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

/**
 * 编号生成工具
 *
 * @author wangkm
 */
public class RedisUtil {
    public static String generateContractNo() {
        String start = "SAAS_CONTRACT_NO";
        RedisTemplate<String, String> redisTemplate = SpringContextUtil.getBean("redisTemplate");
        String date = DateUtil.format(DateUtil.date(), DatePattern.PURE_DATE_PATTERN);
        ValueOperations operations = redisTemplate.opsForValue();
        String key = start + ":" + date;
        long count = operations.increment(key);
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
        long max = 100000;
        Long v = max + count;
        String no = (v + "").substring(1, (max + "").length());
        return "SAAS" + date + no;
    }
}