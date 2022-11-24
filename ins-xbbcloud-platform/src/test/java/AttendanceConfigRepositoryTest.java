import cn.xunhou.cloud.framework.XbbApplication;
import cn.xunhou.cloud.framework.util.SystemUtil;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.xbbcloud.rpc.attendance.dao.AttendanceConfigAddressRepository;
import cn.xunhou.xbbcloud.rpc.attendance.dao.AttendanceRecordRepository;
import cn.xunhou.xbbcloud.rpc.attendance.service.AttendanceService;
import cn.xunhou.xbbcloud.sched.AttendanceSched;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest(classes = XbbApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ContextConfiguration(loader = SwiftApplicationUnit.class)
@RunWith(PowerMockRunner.class) // 告诉JUnit使用PowerMockRunner进行测试
@PrepareForTest({SystemUtil.class})
// 所有需要测试的类列在此处，适用于模拟final类或有final, private, static, native方法的类
@PowerMockRunnerDelegate(SpringRunner.class) //RunWith依然是PowerMock，那这里Delegate委托给spring
@PowerMockIgnore({"javax.*.*", "com.sun.*", "org.xml.*", "org.apache.*"})
public class AttendanceConfigRepositoryTest {


    @GrpcClient("ins-xhportal-platform")
    private static HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;

    @Autowired
    private AttendanceConfigAddressRepository attendanceConfigAddressRepository;
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private AttendanceSched attendanceSched;

    static {
        System.setProperty("SystemRuntimeEnvironment", "qa");
        System.setProperty("area", "QA67");
    }

    //1555131602676404332
    @Test
    public void test() {
        HrmServiceProto.SnowEmployeeRequest snowEmployeeRequest = HrmServiceProto.SnowEmployeeRequest.newBuilder()
                .addId(1555131602676404332L)
                .build();
        HrmServiceProto.EmployeePageResponses employeeDetail = hrmServiceBlockingStub.findEmployeeDetail(snowEmployeeRequest);

    }


    @Test
    public void test2() {
        attendanceSched.sendAttendanceBill(null);
    }

}