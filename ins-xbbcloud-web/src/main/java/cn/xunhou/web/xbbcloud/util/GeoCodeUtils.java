package cn.xunhou.web.xbbcloud.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.core.exception.SystemRuntimeException;
import cn.xunhou.web.xbbcloud.product.attendance.dto.GeoKeywordsQueryResult;
import cn.xunhou.web.xbbcloud.product.attendance.dto.GeoKeywordsResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class GeoCodeUtils {

    private static final String KEY = "eeefea8b7e340b56157af13fb6dc4a82";

    private static final String KEYWORDS_SEARCH_REQUEST_URL = "https://restapi.amap.com/v5/place/text?parameters";

    public static List<GeoKeywordsQueryResult> queryByKeyWord(String keyWord, Integer curPage, Integer pageSize) {
        if (StringUtils.isBlank(keyWord)) {
            throw new SystemRuntimeException("缺少关键字参数");
        }
        Map<String, Object> paramMap = new HashMap<>(8);
        paramMap.put("key", KEY);
        if (StringUtils.isNotBlank(keyWord)) {
            paramMap.put("keywords", keyWord);
        }
        paramMap.put("page_size", pageSize);
        paramMap.put("page_num", curPage);

        log.info("paramMap=" + paramMap);
        String responseStr = HttpUtil.get(KEYWORDS_SEARCH_REQUEST_URL, paramMap);
        if (StrUtil.isBlank(responseStr)) {
            log.info("返回结果为空");
            throw new SystemRuntimeException("返回结果为空");
        }
        log.info("responseStr=" + responseStr);
        GeoKeywordsResult result = JSONUtil.toBean(responseStr, GeoKeywordsResult.class);
        log.info("GeoKeywordsResult=" + result);
        if (result.dataNotFound()) {
            log.info("dataNotFound");
            return Collections.emptyList();
        }
        if (!result.successful()) {
            throw new SystemRuntimeException("请求接口失败");
        }
        return result.getPois();
    }
}
