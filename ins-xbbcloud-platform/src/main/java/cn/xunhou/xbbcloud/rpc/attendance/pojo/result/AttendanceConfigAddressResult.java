package cn.xunhou.xbbcloud.rpc.attendance.pojo.result;

import cn.xunhou.grpc.proto.xbbcloud.AttendanceServerProto;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceConfigAddressEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class AttendanceConfigAddressResult {

    private Long attendanceAddressId;
    //定位地址
    private String locationAddress;
    //地址名称
    private String addressName;
    //经度
    private double longitude;
    //维度
    private double latitude;
    //偏移距离/米
    private Integer offsetDistance;
    //部门id
    private Long orgId;
    //租户id
    private Integer tenantId;

    public static List<AttendanceConfigAddressResult> convertEntity2Result(List<AttendanceConfigAddressEntity> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }
        List<AttendanceConfigAddressResult> resultList = new ArrayList<>();
        for (AttendanceConfigAddressEntity entity : entities) {
            resultList.add(new AttendanceConfigAddressResult()
                    .setAttendanceAddressId(entity.getId())
                    .setLocationAddress(entity.getLocationAddress())
                    .setAddressName(entity.getAddressName())
                    .setLatitude(entity.getLatitude())
                    .setLongitude(entity.getLongitude())
                    .setOffsetDistance(entity.getOffsetDistance())
                    .setOrgId(entity.getOrgId())
                    .setTenantId(entity.getTenantId()));
        }
        return resultList;
    }

    public static List<AttendanceServerProto.AttendanceAddress> convertResult2Response(List<AttendanceConfigAddressResult> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return Collections.emptyList();
        }
        List<AttendanceServerProto.AttendanceAddress> responses = new ArrayList<>();
        resultList.forEach(attendanceConfigAddressResult -> {
            responses.add(AttendanceServerProto.AttendanceAddress.newBuilder()
                    .setAttendanceAddressId(attendanceConfigAddressResult.getAttendanceAddressId())
                    .setLocationAddress(attendanceConfigAddressResult.getLocationAddress())
                    .setAddressName(attendanceConfigAddressResult.getAddressName())
                    .setLongitude(attendanceConfigAddressResult.getLongitude())
                    .setLatitude(attendanceConfigAddressResult.getLatitude())
                    .setOffsetDistance(attendanceConfigAddressResult.getOffsetDistance())
                    .setOrgId(attendanceConfigAddressResult.getOrgId())
                    .build());

        });
        return responses;
    }
}
