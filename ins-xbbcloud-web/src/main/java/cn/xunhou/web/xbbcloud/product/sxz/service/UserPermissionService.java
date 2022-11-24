package cn.xunhou.web.xbbcloud.product.sxz.service;

import cn.xunhou.cloud.core.context.PermissionResult;
import cn.xunhou.cloud.web.mvc.auth.IUserPermission;
import cn.xunhou.grpc.proto.portal.PortalServiceGrpc;
import cn.xunhou.grpc.proto.portal.PortalServiceProto;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

/**
 * @author fico
 */
@Service
public class UserPermissionService implements IUserPermission {

    @GrpcClient("ins-xhportal-platform")
    private PortalServiceGrpc.PortalServiceBlockingStub portalServiceBlockingStub;

    private static final String ALLOW = "allow";

    @Override
    public PermissionResult hasPermission(Integer integer, Long aLong, String s) {
        PortalServiceProto.SecurityLevelBeResponse response = portalServiceBlockingStub.getLevelByObjectAndPermission(PortalServiceProto.ObjectWithPermissionBeRequest
                .newBuilder()
                .setAction(ALLOW)
                .setPermissionCode(s)
                .setObjectId(aLong)
                .build());
        return new PermissionResult().setPermissionId(response.getPermissionId()).setSecurityLevel(response.getLevelValue());
    }
}
