package cn.xunhou.xbbcloud.rpc.salary.server;

import cn.hutool.core.util.ObjectUtil;
import cn.xunhou.cloud.core.json.XbbProtoJsonUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.grpc.proto.xbbcloud.AbstractSalaryServerImplBase;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryMerchantInfoEntity;
import cn.xunhou.xbbcloud.rpc.salary.pojo.SalaryConvert;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.SalaryBatchResult;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.SalaryDetailResult;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.SalaryMerchantFlowResult;
import cn.xunhou.xbbcloud.rpc.salary.service.SalaryService;
import com.google.protobuf.Empty;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import javax.annotation.Resource;

/**
 * 薪酬云-校验逻辑层
 */
@Slf4j
@GrpcService
public class SalaryServer extends AbstractSalaryServerImplBase {

    @Resource
    private SalaryService salaryService;

    @Override
    protected SalaryServerProto.MerchantInfoListResponse queryMerchantInfoList(SalaryServerProto.MerchantInfoListRequest request) {
        return salaryService.queryMerchantInfoList(request);
    }

    @Override
    protected SalaryServerProto.MerchantInfoPageResponse queryMerchantPage(SalaryServerProto.MerchantInfoPageRequest request) {
        PagePojoList<SalaryMerchantInfoEntity> pagePojoList = salaryService.queryMerchantPage(request);
        SalaryServerProto.MerchantInfoPageResponse.Builder infoPageResponse = SalaryServerProto.MerchantInfoPageResponse.newBuilder();
        for (SalaryMerchantInfoEntity salaryMerchantInfo : pagePojoList.getData()) {
            infoPageResponse.addMerchantInfoList(SalaryConvert.entity2Response(salaryMerchantInfo));
        }
        return infoPageResponse
                .setPage(request.getPage())
                .setSize(request.getSize())
                .setTotal(pagePojoList.getTotal())
                .build();
    }

    /**
     * <pre>
     * 查询商户信息
     * </pre>
     *
     * @param request 请求入参
     */
    @Override
    protected SalaryServerProto.MerchantInfoResponse queryMerchantInfo(SalaryServerProto.MerchantInfoRequest request) {
        SalaryMerchantInfoEntity entity = salaryService.queryMerchantInfo(request.getTenantId());
        SalaryServerProto.MerchantInfoResponse.Builder builder = SalaryConvert.entity2Response(entity);
        return builder.build();
    }


    /**
     * <pre>
     * 新增|修改商户信息（变更信息时需校验使用场景）
     * </pre>
     *
     * @param request 请求入参
     */
    @Override
    protected SalaryServerProto.MerchantInfoResponse saveMerchantInfo(SalaryServerProto.SaveMerchantInfoRequest request) {
        if (ObjectUtil.hasEmpty(request.getTenantId(), request.getCertificationType(), request.getPayeeMerchantNo(), request.getPayeeMerchantName(),
                request.getTenantType(), request.getPayrollMethodsList(), request.getIndividualTax(),
                request.getIsApproval(), request.getUseToDate())) {
            String msg = "客户类型（saas及代发类型，必填），商户号收款主体（商户号认证主体，必填），商户号收款账号（商户号收款账号-财付通账号，必填），提现认证类型（二要素认证、信息+人脸认证，下拉选择，必填），发薪方式（可多选：无卡发薪、有卡发薪，必填），个税能力（单选：支持，不支持  必填），审批流程（单选  必填 ：支持，不支持），服务费率（开放字段，非必填，不填默认为0），套餐有效期：（配置到模块上）指客户套餐到期时间，必填。";
            throw GrpcException.asRuntimeException(msg);
        }
        return SalaryConvert.entity2Response(salaryService.saveMerchantInfo(request)).build();
    }

    /**
     * <pre>
     * 商户流水操作
     * </pre>
     *
     * @param request 请求入参
     */
    @Override
    protected SalaryServerProto.MerchantFlowsResponse operateMerchantFlow(SalaryServerProto.OperateMerchantFlowRequest request) {
        SalaryServerProto.MerchantFlowsResponse.Builder builder = SalaryConvert.result2Response(salaryService.operateMerchantFlow(SalaryConvert.request2Entity(request)));
        return builder.build();
    }

    /**
     * <pre>
     * 商户交易流水查询（操作金额的相关逻辑）
     * </pre>
     *
     * @param request 请求入参
     */
    @Override
    protected SalaryServerProto.MerchantFlowsPageResponse findMerchantFlow(SalaryServerProto.MerchantFlowPageRequest request) {
        if (ObjectUtil.hasEmpty(request.getTenantId())) {
            String msg = "缺失必填参数！";
            throw GrpcException.asRuntimeException(msg);
        }
        SalaryServerProto.MerchantFlowsPageResponse.Builder builder = SalaryServerProto.MerchantFlowsPageResponse.newBuilder();
        PagePojoList<SalaryMerchantFlowResult> pagePojoList = salaryService.findMerchantFlow(request);
        for (SalaryMerchantFlowResult result : pagePojoList.getData()) {
            builder.addData(SalaryConvert.result2Response(result));
        }
        builder.setTotal(pagePojoList.getTotal()).setPage(request.getPage()).setSize(request.getSize());
        return builder.build();
    }

    /**
     * <pre>
     * 查询商户余额
     * </pre>
     *
     * @param request 请求入参
     */
    @Override
    protected SalaryServerProto.MerchantInfoResponse findMerchantBalance(SalaryServerProto.MerchantInfoRequest request) {
        return salaryService.findMerchantBalance(request);
    }

    /**
     * <pre>
     * 认证后更新该用户的所有发薪明细状态
     * </pre>
     *
     * @param request 请求入参
     */
    @Override
    protected Empty certifyUpdateSalaryDetail(SalaryServerProto.CertificationRequest request) {
        return salaryService.certifyUpdateSalaryDetail(request);
    }

    /**
     * <pre>
     * 查询项目下拉列表
     * </pre>
     *
     * @param request 请求入参
     */
    @Override
    protected SalaryServerProto.SalaryProductListResponse querySalaryProduct(Empty request) {
        return salaryService.querySalaryProduct();
    }

    @Override
    protected SalaryServerProto.UserXhCListResponse queryUserXhCByIdCards(SalaryServerProto.QueryByIdCardsRequest queryByIdCardsRequest) {
        return salaryService.queryUserXhCByIdCards(queryByIdCardsRequest);
    }

    /**
     * <pre>
     * 保存发薪批次
     * </pre>
     *
     * @param request 请求入参
     */
    @Override
    protected SalaryServerProto.saveSalaryBatchResponse saveSalaryBatch(SalaryServerProto.SalaryBatchRequest request) {
        return salaryService.saveSalaryBatch(request);
    }


    /**
     * <pre>
     * 发薪批次分页列表
     * </pre>
     *
     * @param request 请求入参
     */
    @Override
    protected SalaryServerProto.SalaryBatchPageBeResponse findSalaryBatchPageList(SalaryServerProto.SalaryBatchConditionBeRequest request) {
        log.info("发薪批次分页列表 findSalaryBatchPageList入参:" + XbbProtoJsonUtil.toJsonString(request));
        SalaryServerProto.SalaryBatchPageBeResponse.Builder builder = SalaryServerProto.SalaryBatchPageBeResponse.newBuilder();
        PagePojoList<SalaryBatchResult> pagePojoList = salaryService.findSalaryBatchPageList(request);
        for (SalaryBatchResult result : pagePojoList.getData()) {
            builder.addData(SalaryConvert.result2Response(result));
        }
        builder.setTotal(pagePojoList.getTotal());
        return builder.build();
    }


    /**
     * <pre>
     * 分页查询发薪明细列表
     * </pre>
     *
     * @param request 请求入参
     */
    @Override
    protected SalaryServerProto.SalaryDetailPageBeResponse findSalaryDetailPageList(SalaryServerProto.SalaryDetailConditionBeRequest request) {

        log.info("分页查询发薪明细列表 findSalaryDetailPageList入参:" + XbbProtoJsonUtil.toJsonString(request));
        SalaryServerProto.SalaryDetailPageBeResponse.Builder builder = SalaryServerProto.SalaryDetailPageBeResponse.newBuilder();
        PagePojoList<SalaryDetailResult> pagePojoList = salaryService.findSalaryDetailPageList(request);
        for (SalaryDetailResult result : pagePojoList.getData()) {
            builder.addData(SalaryConvert.result2Response(result));
        }
        builder.setTotal(pagePojoList.getTotal());
        return builder.build();
    }

    @Override
    protected SalaryServerProto.TenantAccountPageResponse findTenantAccount(SalaryServerProto.TenantAccountPageRequest request) {
        return salaryService.findTenantAccount(request);
    }

    @Override
    protected SalaryServerProto.FundBackResponse fundBack(SalaryServerProto.FundBackRequest fundBackRequest) {
        salaryService.fundBack(fundBackRequest.getDetailIdsList());
        return SalaryServerProto.FundBackResponse.newBuilder().build();
    }

    @Override
    protected SalaryServerProto.BreakpointRetryResponse breakpointRetry(SalaryServerProto.BreakpointRetryRequest breakpointRetryRequest) {
        salaryService.breakpointRetry(breakpointRetryRequest.getBatchId());
        return SalaryServerProto.BreakpointRetryResponse.newBuilder().build();
    }
}
