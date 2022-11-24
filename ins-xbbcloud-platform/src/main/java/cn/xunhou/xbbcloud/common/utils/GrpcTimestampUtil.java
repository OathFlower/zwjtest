package cn.xunhou.xbbcloud.common.utils;

import cn.hutool.core.date.DateUtil;
import com.google.protobuf.Timestamp;
import org.springframework.lang.NonNull;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * @author litb
 * @date 2022/9/13 16:18
 * <p>
 * Grpc Timestamp {@link google/protobuf/timestamp.proto} 工具类
 */
public class GrpcTimestampUtil {

    private static final long PER_DAY_SECONDS = 24 * 3600;

    private static final long PER_DAY_MILLISECONDS = PER_DAY_SECONDS * 1000;

    /**
     * 将grpc时间戳转换成java.sql.Date
     *
     * @param timestamp 参数
     * @return 结果
     */
    public static Date toSqlDate(@NonNull Timestamp timestamp) {
        return new Date(timestamp.getSeconds() * 1000);
    }

    /**
     * 将grpc时间戳转换成此日期将来的Date
     *
     * @param timestamp 时间戳
     * @param plusDays  天数
     * @return 结果
     */
    public static Date plusToSqlDate(@NonNull Timestamp timestamp, int plusDays) {
        return new Date(timestamp.getSeconds() * 1000 + PER_DAY_MILLISECONDS * plusDays);
    }

    /**
     * 将grpc时间戳转换成此日期过去的Date
     *
     * @param timestamp    时间戳
     * @param subtractDays 减去的天数
     * @return 结果
     */
    public static Date subtractToSqlDate(@NonNull Timestamp timestamp, int subtractDays) {
        return new Date(timestamp.getSeconds() * 1000 - PER_DAY_MILLISECONDS * subtractDays);
    }

    public static <T extends java.util.Date> Timestamp fromJavaUtilDate(T t) {
        return Timestamp.newBuilder().setSeconds(t.getTime() / 1000).build();
    }

    public static java.sql.Timestamp toSqlTimestamp(Timestamp timestamp) {
        return new java.sql.Timestamp(timestamp.getSeconds() * 1000);
    }

    /**
     * 获取当前时间戳所在日期的零点零分零秒的时间戳
     *
     * @param timestamp 时间戳
     * @return 起始时间
     */
    public static Timestamp atStartOfDay(java.sql.Timestamp timestamp) {
        return Timestamp.newBuilder()
                .setSeconds(LocalDate.from(DateUtil.toLocalDateTime(new java.util.Date(timestamp.getTime())))
                        .atStartOfDay()
                        .toInstant(ZoneOffset.ofHours(8))
                        .toEpochMilli() / 1000).build();
    }
}
