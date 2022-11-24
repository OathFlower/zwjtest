package cn.xunhou.web.xbbcloud.product.schedule.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

/**
 * @author litb
 * @date 2022/9/19 19:19
 * <p>
 * 排班周期返回结果
 */
@Getter
@Setter
@ToString
public class ScheduleDateResult {

    /**
     * 周期开始时间 yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleStartAt;

    /**
     * 周期结束时间 yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleEndAt;

    /**
     * 该周期类的时间列表 MM.dd 格式
     */
    private List<String> scheduleDateList;

}
