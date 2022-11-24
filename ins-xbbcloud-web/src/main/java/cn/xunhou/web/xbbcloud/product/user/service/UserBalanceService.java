package cn.xunhou.web.xbbcloud.product.user.service;
import java.time.LocalDate;
import java.time.LocalDateTime;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.portal.PortalServiceProto;
import cn.xunhou.grpc.proto.subject.SubjectServiceGrpc;
import cn.xunhou.grpc.proto.subject.SubjectServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerGrpc;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.web.xbbcloud.config.xhrpc.XhRpcComponent;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumProject;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumXhTenant;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhR;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhRpcParam;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhTotal;
import cn.xunhou.web.xbbcloud.product.user.result.UserBalanceDetailResult;
import cn.xunhou.web.xbbcloud.product.user.param.UserBalanceQueryParam;
import cn.xunhou.web.xbbcloud.product.user.result.UserBalanceResult;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户余额service
 */
@Service
@Slf4j
public class UserBalanceService {

    @Resource
    private XhRpcComponent xhRpcComponent;
    @GrpcClient("ins-xbbcloud-platform")
    private SalaryServerGrpc.SalaryServerBlockingStub salaryServerBlockingStub;
    @GrpcClient("ins-xhportal-platform")
    private static SubjectServiceGrpc.SubjectServiceBlockingStub subjectServiceBlockingStub;
    @GrpcClient("ins-xhportal-platform")
    private static HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;

    public static void main(String[] args) {
        System.out.println(DesensitizedUtil.idCardNum("51343620000320711X", 3, 3));
    }
    public JsonListResponse<UserBalanceResult> page(@RequestBody UserBalanceQueryParam param){

        XhRpcParam xhRpcParam = new XhRpcParam();
        Map<String, Object> paramsMap = new HashMap<>();
        Map<String, Object> usercParamsMap = new HashMap<>();
        usercParamsMap.put("pageSize", param.getPageSize());
        usercParamsMap.put("curPage", param.getCurPage());
        usercParamsMap.put("idCardNo",param.getIdCardNo());
        usercParamsMap.put("name",param.getName());
        usercParamsMap.put("tel",param.getTel());
        paramsMap.put("queryDto",usercParamsMap);
        xhRpcParam.setRequest(paramsMap)
                .setServiceProject(EnumProject.XBB)
                .setXhTenant(EnumXhTenant.XUNHOU)
                .setUri("IUserCXbbService/list4Cloud");
        XhR<XhTotal<UserBalanceResult>> xhTotalXhR = xhRpcComponent.sendForTotal(xhRpcParam, UserBalanceResult.class);
        if(xhTotalXhR == null || CollUtil.isEmpty(xhTotalXhR.getData().getList())){
            return JsonListResponse.success();
        }
        //查询寻薪酬云发薪
        Map<String, List<SalaryServerProto.SalaryDetailBeResponse>> salaryDetailBeResponseListMap = new HashMap<>();
        SalaryServerProto.SalaryDetailConditionBeRequest salaryDetailConditionBeRequest = SalaryServerProto.SalaryDetailConditionBeRequest.newBuilder()
                .setCurPage(0)
                .setPageSize(10000)
                .addAllNotInDetailStatus(Arrays.asList(SalaryServerProto.EnumSalaryDetailStatus.PAY_FAIL_VALUE,SalaryServerProto.EnumSalaryDetailStatus.CANCELLED_VALUE))
                .addAllIdCardNos( xhTotalXhR.getData().getList().stream().map(UserBalanceResult :: getIdCardNo).collect(Collectors.toList()))
                .build();
        SalaryServerProto.SalaryDetailPageBeResponse salaryDetailPageList = salaryServerBlockingStub.findSalaryDetailPageList(salaryDetailConditionBeRequest);
        if(CollUtil.isNotEmpty(salaryDetailPageList.getDataList())){
            salaryDetailBeResponseListMap =
                    salaryDetailPageList.getDataList().stream().collect(Collectors.groupingBy(SalaryServerProto.SalaryDetailBeResponse::getIdCardNo));
        }
        List<UserBalanceResult> result = xhTotalXhR.getData().getList();
        for (UserBalanceResult userBalanceResult : result) {
            List<SalaryServerProto.SalaryDetailBeResponse> salaryDetailBeResponses = salaryDetailBeResponseListMap.get(userBalanceResult.getIdCardNo());
            if(CollUtil.isNotEmpty(salaryDetailBeResponses)){
                BigDecimal xcyTotalImcome = salaryDetailBeResponses.stream().map(SalaryServerProto.SalaryDetailBeResponse::getPayableAmount).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
                userBalanceResult.setXcyTotalImcome(xcyTotalImcome.divide(new BigDecimal("100"),2, RoundingMode.HALF_UP));
            }
        }
        return JsonListResponse.success(result,xhTotalXhR.getData().getTotalCount().intValue());
    }

    public JsonListResponse<UserBalanceDetailResult> detail(Long userXhCId) {

        //构建协议模板查询条件
        Map<String, Object> paramsMap = new HashMap<>();
        Map<String, Object> usercParamsMap = new HashMap<>();

        XhRpcParam xhRpcParam = new XhRpcParam();

        usercParamsMap.put("pageSize", 10000);
        usercParamsMap.put("curPage", 0);
        usercParamsMap.put("userXhCId",userXhCId);
        usercParamsMap.put("types",Arrays.asList("FLEXIBLE_EMPLOYMENT_PAY"
                ,"LABOR_SENDSAKLARY","SIGN_IN_REWARD","RECOMMEND_INTERVIEW_REWARD","QUIT_REWARD","CAMPUS_SEND_SALARY","SETTLEMENT_PAY","XCY_PAY"));
        paramsMap.put("queryDto",usercParamsMap);
        xhRpcParam.setRequest(paramsMap)
                .setServiceProject(EnumProject.XBB)
                .setXhTenant(EnumXhTenant.XUNHOU)
                    .setUri("IUserCXbbMoneyTraceabilityService/list");
        //从薪班班获取该用户所有流水
        XhR<XhTotal<JSONObject>> xhTotalXhR = xhRpcComponent.sendForTotal(xhRpcParam, JSONObject.class);
        if(xhTotalXhR == null || CollUtil.isEmpty(xhTotalXhR.getData().getList())){
            return JsonListResponse.success();
        }
        List<UserBalanceDetailResult> resultList = new ArrayList<>();
        Map<Long, SubjectServiceProto.SubjectDetailBeResponse> subjectDetailBeResponseMap = new HashMap<>();

        List<Long> subjectIds = xhTotalXhR.getData().getList().stream().map(t -> (Integer) t.get("payrollCmbankSubjectId")).map(Long :: valueOf).collect(Collectors.toList());
        SubjectServiceProto.IdBeRequests idBeRequests = SubjectServiceProto.IdBeRequests.newBuilder()
                .addAllId(subjectIds)
                .build();
        //获取主体信息
        SubjectServiceProto.SubjectDetailBeResponses subjectDetailBeResponses = subjectServiceBlockingStub.getSubjectObjectByIds(idBeRequests);
        if(CollUtil.isNotEmpty(subjectDetailBeResponses.getDataList())){
            subjectDetailBeResponseMap = subjectDetailBeResponses.getDataList().stream()
                    .collect(Collectors.toMap(SubjectServiceProto.SubjectDetailBeResponse::getSubjectId, Function.identity()));
        }

        Map<Long, SalaryServerProto.SalaryDetailBeResponse> salaryDetailBeResponseMap = new HashMap<>();
        Map<Long, HrmServiceProto.TenantBeResponse> tenantBeResponseMap = new HashMap<>();
        Map<Long, SalaryServerProto.SalaryBatchBeResponse> salaryBatchBeResponseMap = new HashMap<>();

        //获取薪酬云发薪详情id
        List<Long> objIds = xhTotalXhR.getData().getList().stream().filter(t -> t.get("moneyTraceabilityType").toString().equals("XCY_PAY"))
                .map(t -> (Long) t.get("objectId")).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(objIds)){
            SalaryServerProto.SalaryDetailConditionBeRequest salaryDetailConditionBeRequest = SalaryServerProto.SalaryDetailConditionBeRequest
                    .newBuilder()
                    .setCurPage(0)
                    .setPageSize(1000)
                    .addAllIds(objIds)
                    .build();
            SalaryServerProto.SalaryDetailPageBeResponse salaryDetailPageList = salaryServerBlockingStub.findSalaryDetailPageList(salaryDetailConditionBeRequest);
           salaryDetailBeResponseMap = salaryDetailPageList.getDataList().stream().collect(Collectors.toMap(SalaryServerProto.SalaryDetailBeResponse::getDetailId, Function.identity()));
            //获取薪酬云发薪租户id
            List<Long> tenantIds = salaryDetailPageList.getDataList().stream().map(SalaryServerProto.SalaryDetailBeResponse::getTenantId).collect(Collectors.toList());
            List<Long> batchIds = salaryDetailPageList.getDataList().stream().map(SalaryServerProto.SalaryDetailBeResponse::getBatchId).collect(Collectors.toList());
            SalaryServerProto.SalaryBatchConditionBeRequest conditionBeRequest = SalaryServerProto.SalaryBatchConditionBeRequest.newBuilder()
                    .addAllBatchIds(batchIds)
                    .build();
            SalaryServerProto.SalaryBatchPageBeResponse salaryBatchPageList = salaryServerBlockingStub.findSalaryBatchPageList(conditionBeRequest);
            if(CollUtil.isNotEmpty(salaryBatchPageList.getDataList())){
                salaryBatchBeResponseMap = salaryBatchPageList.getDataList().stream().collect(Collectors.toMap(SalaryServerProto.SalaryBatchBeResponse::getBatchId, Function.identity()));
            }

            HrmServiceProto.TenantRequest tenantRequest = HrmServiceProto.TenantRequest.newBuilder()
                    .addAllId(tenantIds.stream().map(Long::intValue).collect(Collectors.toList()))
                    .build();
            HrmServiceProto.TenantBeResponses tenantBeResponses = hrmServiceBlockingStub.findTenant(tenantRequest);
            if(CollUtil.isNotEmpty(tenantBeResponses.getDataList())){
                tenantBeResponseMap = tenantBeResponses.getDataList().stream().collect(Collectors.toMap(HrmServiceProto.TenantBeResponse::getId, Function.identity()));
            }

        }

        //聚合信息
        for (JSONObject t : xhTotalXhR.getData().getList()) {
            String createtime = t.get("createtime").toString();
            Integer payrollCmbankSubjectId = (Integer)t.get("payrollCmbankSubjectId");
            String moneyTraceabilityType = t.get("moneyTraceabilityType").toString();
            BigDecimal money = BigDecimal.valueOf((Double) t.get("money"));
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            SubjectServiceProto.SubjectDetailBeResponse subjectDetailBeResponse = subjectDetailBeResponseMap.get(Long.valueOf(payrollCmbankSubjectId));

            UserBalanceDetailResult userBalanceDetailResult = new UserBalanceDetailResult();
            if("XCY_PAY".equals(moneyTraceabilityType)){
                String lockFlag = t.get("lockFlag").toString();
                if("YES".equals(lockFlag)){
                    continue;
                }
                Long objectId = (Long)t.get("objectId");
                SalaryServerProto.SalaryDetailBeResponse salaryDetailBeResponse = salaryDetailBeResponseMap.get(objectId);
                if(null != salaryDetailBeResponse){
                    long batchId = salaryDetailBeResponse.getBatchId();
                    SalaryServerProto.SalaryBatchBeResponse salaryBatchBeResponse = salaryBatchBeResponseMap.get(batchId);
                    String expandJson = salaryBatchBeResponse.getExpandJson();
                    if(StringUtils.isNotBlank(expandJson)){
                        JSONObject entries = XbbJsonUtil.fromJsonString(expandJson, JSONObject.class);
                        //设置主体名称
                        userBalanceDetailResult.setSubjectName(entries != null ?  entries.get("payeeMerchantName").toString() : null);
                    }
                    //设置租户名称
                    userBalanceDetailResult.setTenantName(tenantBeResponseMap.get(salaryDetailBeResponse.getTenantId()).getAlias());
                }
            }else{
                userBalanceDetailResult.setSubjectName(subjectDetailBeResponse.getSubjectName());
            }
            userBalanceDetailResult.setPayDate(LocalDateTime.parse(createtime, dateTimeFormatter).toLocalDate());
            userBalanceDetailResult.setMoney(money);
            userBalanceDetailResult.setPayTime(LocalDateTime.parse(createtime, dateTimeFormatter));
            resultList.add(userBalanceDetailResult);
        }
        return JsonListResponse.success(resultList);
    }
}
