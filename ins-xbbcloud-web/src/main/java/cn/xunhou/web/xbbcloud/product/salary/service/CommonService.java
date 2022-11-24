package cn.xunhou.web.xbbcloud.product.salary.service;

import cn.hutool.core.collection.CollUtil;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.web.xbbcloud.config.xhrpc.XhRpcComponent;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumProject;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhR;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhRpcParam;
import cn.xunhou.web.xbbcloud.product.hrm.enums.EnumTenant;
import cn.xunhou.web.xbbcloud.product.salary.result.IdNameMapperResult;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通用服务类
 *
 * @author wangkm
 */
@Service
@Slf4j
public class CommonService {
    @Resource
    private XhRpcComponent xhRpcComponent;
    @GrpcClient("ins-xhportal-platform")
    private HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;


    /**
     * 通过id获取名称
     *
     * @param userIds
     * @return
     */
    public Map<Long, String> userXh(Collection<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Maps.newHashMap();
        }
        //查询userxh表信息
        HrmServiceProto.UserXhInfoBeRequest build = HrmServiceProto.UserXhInfoBeRequest.newBuilder()
                .addAllUserXhId(userIds)
                .build();
        HrmServiceProto.UserXhInfoBeResponseList userXhList = hrmServiceBlockingStub.findUserXhList(build);
        return userXhList.getDataList().stream().collect(Collectors.toMap(HrmServiceProto.UserXhInfoBeResponse::getUserXhId, HrmServiceProto.UserXhInfoBeResponse::getName));
    }

    /**
     * 通过id获取名称
     *
     * @param userIds
     * @return
     */
    public Map<Long, String> userXbbCid(Collection<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Maps.newHashMap();
        }
//        List<UserXhCDto> getUserXhCDtosByIds(List<Long> userXhCIds, Boolean withIdCardInfo, Boolean withBankCardInfo)
        Map<String, Object> param = new HashMap<>();
        param.put("userXhCIds", userIds);
        param.put("withIdCardInfo", false);
        param.put("withBankCardInfo", false);
        XhRpcParam xhRpcParam = new XhRpcParam();
        xhRpcParam.setRequest(param)
                .setServiceProject(EnumProject.USERXH)
                .setUri("IUserXhCService/getUserXhCDtosByIds");
        XhR<List<IdNameMapperResult>> resultXhR = xhRpcComponent.sendForList(xhRpcParam, IdNameMapperResult.class);
        return resultXhR.getData().stream().collect(Collectors.toMap(IdNameMapperResult::getId, IdNameMapperResult::getName));
    }

    /**
     * 通过id获取名称
     *
     * @param userIds
     * @return
     */
    public Map<Long, String> userSaas(Collection<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Maps.newHashMap();
        }
        HrmServiceProto.AccountBeResponses accountBeResponses = hrmServiceBlockingStub.findAccountByIds(HrmServiceProto.SnowAccountRequest.newBuilder().
                addAllId(userIds)
                .build());
        return accountBeResponses.getDataList().stream().collect(Collectors.toMap(HrmServiceProto.AccountDetailBeResponse::getId, HrmServiceProto.AccountDetailBeResponse::getNickName));
    }

    /**
     * TODO
     * 通过id获取名称
     *
     * @param ids
     * @return
     */
    public Map<Long, String> customerName(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Maps.newHashMap();
        }
//        List<CustomerDto> getCustomerDtoListByIds(List<Long> customerIds, EnumTenant tenant) throws BizException;
        Map<String, Object> param = new HashMap<>();
        param.put("customerIds", ids);
        param.put("tenant", EnumTenant.XUNHOU);
        XhRpcParam xhRpcParam = new XhRpcParam();
        xhRpcParam.setRequest(param)
                .setServiceProject(EnumProject.USERXH)
                .setUri("ICustomerService/getCustomerDtoListByIds");
        XhR<List<IdNameMapperResult>> resultXhR = xhRpcComponent.sendForList(xhRpcParam, IdNameMapperResult.class);
        return resultXhR.getData().stream().collect(Collectors.toMap(IdNameMapperResult::getId, IdNameMapperResult::getCustomerName));
    }
}
