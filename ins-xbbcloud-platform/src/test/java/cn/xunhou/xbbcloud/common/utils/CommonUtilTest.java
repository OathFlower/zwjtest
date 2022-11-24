package cn.xunhou.xbbcloud.common.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class CommonUtilTest {

    @Test
    public void taxMonthCount() {

        DateTime dateTime = DateUtil.parse("2022-10-10");
        Integer i = CommonUtil.taxMonthCount(dateTime.toTimestamp());
        log.info("r = {}", i);

        dateTime = DateUtil.parse("2021-10-10");
        i = CommonUtil.taxMonthCount(dateTime.toTimestamp());
        log.info("r = {}", i);

        dateTime = DateUtil.parse("2022-06-10");
        i = CommonUtil.taxMonthCount(dateTime.toTimestamp());
        log.info("r = {}", i);

        dateTime = DateUtil.parse("2023-10-10");
        try {
            i = CommonUtil.taxMonthCount(dateTime.toTimestamp());
            log.info("r = {}", i);
        } catch (Exception e) {
            e.printStackTrace();
        }


        dateTime = DateUtil.parse("2023-11-10");
        try {
            i = CommonUtil.taxMonthCount(dateTime.toTimestamp());
            log.info("r = {}", i);
        } catch (Exception e) {
            e.printStackTrace();
        }

        dateTime = DateUtil.parse("2023-01-10");
        try {
            i = CommonUtil.taxMonthCount(dateTime.toTimestamp());
            log.info("r = {}", i);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}