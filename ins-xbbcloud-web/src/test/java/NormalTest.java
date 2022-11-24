import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.xunhou.cloud.framework.util.XbbProtoJsonUtil;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author sha.li
 * @since 2022/9/14
 */
@Slf4j
public class NormalTest {
    @Test
    public void testDateFormat() {
        String s = "2019年4月01日 、2019-04-1、2019-4-01、2019.04.1、2019/4/01";
        String a = "2019-04-01";
        for (String s1 : s.split("、")) {
            s1 = s1.trim();
            String s2 = new DateTime(s1).toString("yyyy-MM-dd");
            if (!a.equals(s2)) {
                System.err.println("s1 = " + s1);
                System.err.println("s2 = " + s2);
            }
        }
    }

    @Test
    public void proto() {
        SalaryServerProto.SalaryBatchRequest.Builder builder = SalaryServerProto.SalaryBatchRequest.newBuilder();
        builder.setProductName("param.getProductName()");
        builder.setMonth("param.getMonth()");
        builder.setSalaryFile("param.getSalaryFile()");
        SalaryServerProto.SalaryBatchRequest request = builder.build();
        String str = XbbProtoJsonUtil.toJsonString(request);
        log.info(" str = {}", str);
        SalaryServerProto.SalaryBatchRequest.Builder salaryBatchRequestBuild = SalaryServerProto.SalaryBatchRequest.newBuilder();
        salaryBatchRequestBuild.mergeFrom(XbbProtoJsonUtil.fromJsonString(salaryBatchRequestBuild, str));
        String str2 = XbbProtoJsonUtil.toJsonString(salaryBatchRequestBuild.build());
        log.info(" str2 = {}", str2);
    }

    @Test
    public void n() {
        Number n = Convert.toNumber("221,321");
        log.info("r = {}", n);
    }
}
