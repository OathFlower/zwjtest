package cn.xunhou.web.xbbcloud.product.hrm.service;

import cn.hutool.core.collection.CollUtil;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.portal.PortalServiceGrpc;
import cn.xunhou.grpc.proto.portal.PortalServiceProto;
import cn.xunhou.grpc.proto.universal.UniversalServiceGrpc;
import cn.xunhou.grpc.proto.xbbcloud.ScheduleServerGrpc;
import cn.xunhou.web.xbbcloud.product.hrm.param.RoleIdListParam;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sha.li
 * @since 2022/9/14
 */
@Slf4j
@Service
public class HrmAccountService {

    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();

    @GrpcClient("ins-xhportal-platform")
    private HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;

    @GrpcClient("ins-xhwallet-platform")
    private UniversalServiceGrpc.UniversalServiceBlockingStub universalServiceBlockingStub;

    @GrpcClient("ins-xbbcloud-platform")
    private ScheduleServerGrpc.ScheduleServerBlockingStub scheduleServerBlockingStub;

    @GrpcClient("ins-xhportal-platform")
    private static PortalServiceGrpc.PortalServiceBlockingStub portalServiceBlockingStub;

    private static final String ACTION = "allow";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 通过账户id查询已分配的角色id
     * @param accountId
     * @return
     */
    public JsonResponse<List<String>> findAccountRole(@NonNull Long accountId) {
        PortalServiceProto.ObjectBeRequest build = PortalServiceProto.ObjectBeRequest.newBuilder()
                .setObjectId(String.valueOf(accountId))
                .setTemplate(PortalServiceProto.SecurityRbacTemplate.ACCOUNT_RBAC)
                .setAction(ACTION).build();
        PortalServiceProto.RoleBeResponses roleBeResponses = portalServiceBlockingStub.listRbacRolesForObject(build);
        if (CollUtil.isEmpty(roleBeResponses.getRolesList())) {
            return JsonResponse.success(new ArrayList<>());
        }
        List<String> roleIds = roleBeResponses.getRolesList().stream().map(PortalServiceProto.RoleBeResponse::getRoleId).distinct().collect(Collectors.toList());
        return JsonResponse.success(roleIds);
    }


    /**
     * 删除->角色和账户关联关系
     * @param accountId
     * @param roleId
     * @return
     */
    public JsonResponse<?> deleteRolesByAccountId(@NonNull Long accountId, @NonNull Long roleId) {
        PortalServiceProto.RoleObjectBeRequest build = PortalServiceProto.RoleObjectBeRequest.newBuilder()
                .setAction(ACTION)
                .setObjectId(String.valueOf(accountId))
                .setRoleId(String.valueOf(roleId))
                .setTemplate(PortalServiceProto.SecurityRbacTemplate.ACCOUNT_RBAC).build();
        try {
            portalServiceBlockingStub.deleteRbacRoleWithObject(build);
        } catch (StatusRuntimeException e) {
            log.info("员工管理->分配角色 查看已分配的角色 删除 失败", e);
            log.info("e.getStatus():" + e.getStatus());
            if (e.getStatus().getCode().value() == Status.ALREADY_EXISTS.getCode().value()) {
                log.info("当前员工的角色已删除");
            } else {
                throw Status.INTERNAL.withDescription("账户删除角色异常").asRuntimeException();
            }
        }
        return JsonResponse.success();
    }


    /**
     * 新增->角色和账户关联关系
     * @param accountId
     * @param param
     * @return
     */
    public JsonResponse<?> saveRolesByAccountId(Long accountId, @NonNull RoleIdListParam param) {
        if (CollUtil.isEmpty(param.getRoleIdList())) {
            return JsonResponse.success();
        }
        for (Long roleId : param.getRoleIdList()) {
            PortalServiceProto.RoleObjectBeRequest build = PortalServiceProto.RoleObjectBeRequest.newBuilder()
                    .setObjectId(String.valueOf(accountId))
                    .setRoleId(String.valueOf(roleId))
                    .setTemplate(PortalServiceProto.SecurityRbacTemplate.ACCOUNT_RBAC)
                    .setAction(ACTION).build();
            try {
                portalServiceBlockingStub.saveRbacRoleWithObject(build);
            } catch (StatusRuntimeException e) {
                log.info("员工管理->分配角色 查看已分配的角色 新增 失败", e);
                log.info("e.getStatus():" + e.getStatus());
                if (e.getStatus().getCode().value() == Status.ALREADY_EXISTS.getCode().value()) {
                    log.info("当前角色已绑定过权限 无需再次绑定");
                } else if (e.getStatus().getCode().value() == Status.UNAUTHENTICATED.getCode().value()) {
                    log.info("当前角色只允许超级管理员添加用户");
                    throw Status.UNAUTHENTICATED.withDescription("当前角色只允许超级管理员添加用户").asRuntimeException();
                } else {
                    throw Status.INTERNAL.withDescription("员工分配角色异常").asRuntimeException();
                }
            }
        }
        return JsonResponse.success();
    }
}
