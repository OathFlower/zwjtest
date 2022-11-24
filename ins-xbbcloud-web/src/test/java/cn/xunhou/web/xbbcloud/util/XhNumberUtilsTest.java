package cn.xunhou.web.xbbcloud.util;

import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class XhNumberUtilsTest {

    @Test
    public void millimeterFormat() {
        String r = XhNumberUtils.millimeterFormat(0, 2);
        log.info("ret = {}", r);
        r = XhNumberUtils.millimeterFormat(0.016, 2);
        log.info("ret = {}", r);
        r = XhNumberUtils.millimeterFormat(0.001, 2);
        log.info("ret = {}", r);
        r = XhNumberUtils.millimeterFormat(-11.00213, 2);
        log.info("ret = {}", r);
        r = XhNumberUtils.millimeterFormat(-11514512341234.00213, 2);
        log.info("ret = {}", r);
        r = XhNumberUtils.millimeterFormat(.016, 2);
        log.info("ret = {}", r);
        String s = NumberUtil.div("1", "100", 2).toPlainString();
        log.info("ret = {}", s);
    }
}