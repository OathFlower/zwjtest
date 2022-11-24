package cn.xunhou.web.xbbcloud.util;

import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.xunhou.web.xbbcloud.product.hrm.constant.ConstantData;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonMap;

/**
 * @author sha.li
 * @since 2022/9/16
 */
@Slf4j
public class MvcUtil {
    public static void setExcelResponseInfo(HttpServletResponse response, String fileName) {
        if (response == null) {
            log.info("response is null");
            return;
        }
        response.setContentType(ConstantData.XLSX_CONTENT_TYPE);
        response.setCharacterEncoding(UTF_8.name());
        String encodeFileName = URLEncodeUtil.encode(fileName, UTF_8);
        log.info("encodeFileName={}", encodeFileName);
        String contentDisposition = StrUtil.format("attachment; filename=\"{fileName}\"; filename*=utf-8''{fileName}", singletonMap("fileName", encodeFileName));
        response.setHeader("Content-Disposition", contentDisposition);
    }
}
