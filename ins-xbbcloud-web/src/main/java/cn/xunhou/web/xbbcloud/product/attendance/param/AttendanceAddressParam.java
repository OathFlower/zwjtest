package cn.xunhou.web.xbbcloud.product.attendance.param;

import cn.xunhou.grpc.proto.xbbcloud.AttendanceServerProto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class AttendanceAddressParam {

    /**
     * 打卡地址id
     */
    private Long attendanceAddressId;
    /**
     * 地址名称
     */
    @NotNull(message = "地址名称不可为空")
    private String addressName;
    /**
     * 地址经度
     */
    @NotNull(message = "经度不可为空")
    private Double longitude;
    /**
     * 地址纬度
     */
    @NotNull(message = "纬度不可为空")
    private Double latitude;
    /**
     * 定位地址
     */
    @NotNull(message = "定位地址不可为空")
    private String locationAddress;
    /**
     * 部门id
     */
    private Long orgId;
    /**
     * 偏移距离
     */
    @Min(100)
    private Integer offsetDistance;

    public static AttendanceServerProto.AttendanceAddress convertParam2Request(AttendanceAddressParam param) {
        if (param == null) {
            return AttendanceServerProto.AttendanceAddress.newBuilder().build();
        }
        AttendanceServerProto.AttendanceAddress.Builder builder = AttendanceServerProto.AttendanceAddress.newBuilder();
        if (param.getAttendanceAddressId() != null) {
            builder.setAttendanceAddressId(param.getAttendanceAddressId());
        }
        builder.setLatitude(param.getLatitude())
                .setLongitude(param.getLongitude())
                .setLocationAddress(param.getLocationAddress())
                .setOffsetDistance(param.getOffsetDistance())
                .setAddressName(param.getAddressName());
        if (param.getOrgId() != null) {
            builder.setOrgId(param.getOrgId());
        }
        return builder.build();
    }
}
