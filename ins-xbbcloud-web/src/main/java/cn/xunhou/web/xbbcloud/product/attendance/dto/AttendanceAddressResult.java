package cn.xunhou.web.xbbcloud.product.attendance.dto;

import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.AttendanceServerProto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class AttendanceAddressResult {

    /**
     * 打卡地址id
     */
    private Long attendanceAddressId;
    /**
     * 地址名称
     */
    private String addressName;
    /**
     * 地址经度
     */
    private Double longitude;
    /**
     * 地址纬度
     */
    private Double latitude;
    /**
     * 定位地址
     */
    private String locationAddress;
    /**
     * 部门id
     */
    private Long orgId;
    /**
     * 部门名称
     */
    private String orgName;
    /**
     * 偏移距离
     */
    private Integer offsetDistance;

    public static List<AttendanceAddressResult> convertResponse2Result(List<AttendanceServerProto.AttendanceAddress> attendanceAddresses, Map<Long, HrmServiceProto.OrgListResponse> orgListResponseMap) {
        if (CollectionUtils.isEmpty(attendanceAddresses)) {
            return Collections.emptyList();
        }
        List<AttendanceAddressResult> addressResults = new ArrayList<>();
        attendanceAddresses.forEach(attendanceAddress -> {
            AttendanceAddressResult result = new AttendanceAddressResult();
            result.setAttendanceAddressId(attendanceAddress.getAttendanceAddressId());
            result.setAddressName(attendanceAddress.getAddressName());
            result.setLongitude(attendanceAddress.getLongitude());
            result.setLatitude(attendanceAddress.getLatitude());
            result.setLocationAddress(attendanceAddress.getLocationAddress());
            result.setOffsetDistance(attendanceAddress.getOffsetDistance());
            result.setOrgId(attendanceAddress.getOrgId());
            HrmServiceProto.OrgListResponse orgListResponse = orgListResponseMap.get(attendanceAddress.getOrgId());
            if (orgListResponse != null) {
                result.setOrgName(orgListResponse.getName());
            }
            addressResults.add(result);
        });
        return addressResults;
    }
}
