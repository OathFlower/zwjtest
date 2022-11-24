package cn.xunhou.xbbcloud.rpc.salary.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.exception.SystemRuntimeException;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.core.json.XbbProtoJsonUtil;
import cn.xunhou.cloud.core.snow.SnowflakeIdGenerator;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeTenantEntity;
import cn.xunhou.cloud.redis.lock.IRedisLockService;
import cn.xunhou.common.tools.util.DesPlus;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.grpc.proto.asset.AssetXhServerGrpc;
import cn.xunhou.grpc.proto.asset.AssetXhServerProto;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.portal.PortalServiceGrpc;
import cn.xunhou.grpc.proto.portal.PortalServiceProto;
import cn.xunhou.grpc.proto.subject.SubjectServiceGrpc;
import cn.xunhou.grpc.proto.subject.SubjectServiceProto;
import cn.xunhou.grpc.proto.universal.UniversalServiceGrpc;
import cn.xunhou.grpc.proto.universal.UniversalServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.xbbcloud.common.constants.CommonConst;
import cn.xunhou.xbbcloud.common.enums.EnumDispatchStatus;
import cn.xunhou.xbbcloud.common.enums.EnumSalaryBatchStatus;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.common.utils.CommonUtil;
import cn.xunhou.xbbcloud.common.utils.ParamUtil;
import cn.xunhou.xbbcloud.common.utils.TaxUtil;
import cn.xunhou.xbbcloud.config.JdbcConfiguration;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.SendWaitFundDispatchingMessage;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.Xcy2XbbBackDetailMessage;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.XcyPayrollMessage;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.XcyPayrollMessageDto;
import cn.xunhou.xbbcloud.middleware.rocket.producer.RocketMsgService;
import cn.xunhou.xbbcloud.rpc.other.dao.UserXhRepository;
import cn.xunhou.xbbcloud.rpc.other.entity.UserXhCEntity;
import cn.xunhou.xbbcloud.rpc.salary.dao.*;
import cn.xunhou.xbbcloud.rpc.salary.entity.*;
import cn.xunhou.xbbcloud.rpc.salary.handler.event.FundDispatchingEvent;
import cn.xunhou.xbbcloud.rpc.salary.pojo.SalaryConvert;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.*;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.*;
import cn.xunhou.xbbcloud.rpc.sign.dao.ContractRepository;
import cn.xunhou.xbbcloud.rpc.sign.pojo.result.ContractResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.Empty;
import io.grpc.Status;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 业务处理层
 */
@Slf4j
@Service
public class SalaryService {
    @GrpcClient("ins-assetxh-platform")
    private AssetXhServerGrpc.AssetXhServerBlockingStub assetXhServerBlockingStub;
    @GrpcClient("ins-xhportal-platform")
    private SubjectServiceGrpc.SubjectServiceBlockingStub subjectServiceBlockingStub;
    @GrpcClient("ins-xhwallet-platform")
    private UniversalServiceGrpc.UniversalServiceBlockingStub universalServiceBlockingStub;
    @Setter(onMethod = @__(@GrpcClient("ins-xhportal-platform")))
    private HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;
    @Resource
    private SalaryMerchantInfoRepository salaryMerchantInfoRepository;
    @Resource
    private UserXhRepository userXhRepository;
    @Resource
    private SalaryProductRepository salaryProductRepository;
    @Resource
    private SalaryOpenIdRepository salaryOpenIdRepository;
    @Resource
    private SalaryMerchantFlowRepository salaryMerchantFlowRepository;
    @Resource
    private SalaryDetailRepository salaryDetailRepository;
    @Resource
    private SalaryBatchRepository salaryBatchRepository;
    @Resource
    private ContractRepository contractRepository;
    @Resource
    private IRedisLockService redisLockService;
    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();
    @Resource
    private FundDispatchingService fundDispatchingService;

    @GrpcClient("ins-xhportal-platform")
    private PortalServiceGrpc.PortalServiceBlockingStub portalServiceBlockingStub;
    @Resource
    private RocketMsgService rocketMsgService;

    /**
     * <pre>
     * 查询商户信息
     * </pre>
     *
     * @param tenantId 租户id
     */
    public SalaryMerchantInfoEntity queryMerchantInfo(Long tenantId) {
        return salaryMerchantInfoRepository.findById(tenantId);
    }

    /**
     * 保存租户商户信息 不存在创建，存在更新
     *
     * @param request 请求入参
     * @return 租户信息
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public SalaryMerchantInfoEntity saveMerchantInfo(SalaryServerProto.SaveMerchantInfoRequest request) {
        SalaryMerchantInfoEntity entity = SalaryConvert.request2Entity(request);
        if (CharSequenceUtil.isBlank(entity.getServiceMerchantNo())) {
            entity.setServiceMerchantNo("1612337499");
        }
        SalaryMerchantInfoEntity salaryMerchantInfo = queryMerchantInfo(request.getTenantId());
        if (salaryMerchantInfo != null && entity.getTenantType() != null && !salaryMerchantInfo.getTenantType().equals(entity.getTenantType())) {
            throw GrpcException.asRuntimeException("暂不支持切换客户类型");
        }
        if (SalaryServerProto.EnumTenantType.BEHALF_ISSUED == request.getTenantType()) {
            AssetXhServerProto.SubAccountResponses subAccountResponses = assetXhServerBlockingStub.querySubAccount(AssetXhServerProto.SubAccountQueryRequest.newBuilder()
                    .addSubAccountIds(request.getPayeeSubAccountId())
                    .build());
            AssetXhServerProto.SubAccountResponse response = CollUtil.getFirst(subAccountResponses.getSubAccountInfosList());
            if (NumberUtil.compare(response.getSubjectInfoId(), request.getPayerSubjectId()) == 0) {
                throw GrpcException.asRuntimeException("暂不支持相同的发薪主体和收款主体");
            }
        }

        return salaryMerchantInfoRepository.saveById(entity);
    }


    /**
     * 查询交易流水
     *
     * @param request 请求入参
     * @return 交易流水分页数据
     */
    public PagePojoList<SalaryMerchantFlowResult> findMerchantFlow(SalaryServerProto.MerchantFlowPageRequest request) {
        return salaryMerchantFlowRepository.findPageList(SalaryConvert.request2Param(request));
    }


    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public SalaryMerchantFlowResult operateMerchantFlow(SalaryMerchantFlowEntity salaryMerchantFlowEntity) {
        Number id = salaryMerchantFlowRepository.insert(salaryMerchantFlowEntity);
        return salaryMerchantFlowRepository.findByIdForResult(id.longValue());
    }

    public PagePojoList<SalaryDetailResult> findSalaryDetailPageList(SalaryServerProto.SalaryDetailConditionBeRequest request) {
        return salaryDetailRepository.findSalaryDetailPageList(SalaryConvert.request2Param(request));
    }

    public SalaryServerProto.MerchantInfoResponse findMerchantBalance(SalaryServerProto.MerchantInfoRequest request) {
        try {
            SalaryMerchantInfoEntity entity = salaryMerchantInfoRepository.findById(request.getTenantId());
            SalaryServerProto.MerchantInfoResponse.Builder builder = SalaryConvert.entity2Response(entity);
            String balance = "0";
            if (entity == null) {
                return builder.build();
            } else {
                SalaryServerProto.EnumTenantType tenantType = SalaryServerProto.EnumTenantType.forNumber(entity.getTenantType());
                AssetXhServerProto.ThirdPartyAccountsResponse response = null;
                switch (tenantType) {
                    case SAAS:
                        response = assetXhServerBlockingStub.queryThirdPartyAccountBalance(
                                AssetXhServerProto.ThirdPartyAccountsRequest.newBuilder()
                                        .addThirdPartyAccounts(
                                                AssetXhServerProto.ThirdPartyAccountRequest.newBuilder()
                                                        .setAccountNo(entity.getSpecialMerchantId())
                                                        .setSubjectNo(entity.getServiceMerchantNo())
                                                        .build())
                                        .setTransferWay(AssetXhServerProto.EnumTransferWay.WAY_WX_MERCHANT)
                                        .setOperator(XBB_USER_CONTEXT.get() == null ? -1 : XBB_USER_CONTEXT.get().getUserId())
                                        .build()
                        );
                        break;
                    case BEHALF_ISSUED:
                        response = assetXhServerBlockingStub.queryThirdPartyAccountBalance(
                                AssetXhServerProto.ThirdPartyAccountsRequest.newBuilder()
                                        .addThirdPartyAccounts(AssetXhServerProto.ThirdPartyAccountRequest.newBuilder()
                                                .setSubAccountId(entity.getPayeeSubAccountId())
                                                .build())
                                        .build());
                        break;
                    default:
                        break;
                }
                AssetXhServerProto.ThirdPartyAccountResponse thirdPartyAccountResponse = CollUtil.getFirst(response.getThirdPartyAccountResponsesList());
                if (thirdPartyAccountResponse != null) {
                    //元转分
                    balance = NumberUtil.mul(thirdPartyAccountResponse.getBalance(), "100").toString();
                }
            }
            return builder.setBalance(balance).build();
        } catch (Exception e) {
            String msg = CollUtil.getLast(CharSequenceUtil.split(e.getMessage(), "INTERNAL:"));
            throw GrpcException.asRuntimeException(msg);
        }
    }


    public SalaryServerProto.saveSalaryBatchResponse saveSalaryBatch(SalaryServerProto.SalaryBatchRequest request) {
        log.info("提交，保存，发薪 请求参数：" + XbbProtoJsonUtil.toJsonString(request));
        ExeWithdrawOrWxResult exeWithdrawOrWxResult;
        try {
            exeWithdrawOrWxResult = SpringContextUtil.getBean(SalaryService.class).handleSaveBatch(request);
            if (exeWithdrawOrWxResult.isSendWaitFundDispatching()) {
                rocketMsgService.sendWaitFundDispatching(exeWithdrawOrWxResult.getFundDispatchingMessage());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
        return SalaryServerProto.saveSalaryBatchResponse.newBuilder().setBatchId(exeWithdrawOrWxResult.getSalaryBatchResult().getBatchId()).build();
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public ExeWithdrawOrWxResult handleSaveBatch(SalaryServerProto.SalaryBatchRequest request) {
        ExeWithdrawOrWxResult exeWithdrawOrWxResult;
        //获取当前租户信息
        SalaryMerchantInfoEntity salaryMerchantInfoEntity = salaryMerchantInfoRepository.findById(XBB_USER_CONTEXT.tenantId().longValue());
        SalaryServerProto.MerchantInfoResponse merchantBalance = findMerchantBalance(SalaryServerProto.MerchantInfoRequest.newBuilder().setTenantId(XBB_USER_CONTEXT.get().getTenantId()).build());
        Long batchId = saveProductAndBatchTable(request, salaryMerchantInfoEntity);

        List<SalaryDetailEntity> detailEntityList = saveSalaryDetail(request, salaryMerchantInfoEntity, batchId, merchantBalance);
        //认证通过并且有OpenId的明细
        List<SalaryDetailEntity> detailSalaryEntityList = detailEntityList.stream().filter(o -> o.getStatus().equals(SalaryServerProto.EnumSalaryDetailStatus.PAYING_ALREADY_HANDLE.getNumber())).collect(Collectors.toList());
        //保存流水
        saveFlow(request, batchId, merchantBalance);
        BigDecimal serviceAmountTotal = new BigDecimal(0);
        BigDecimal taxAmountTotal = new BigDecimal(0);
        BigDecimal painInAmountTotal = new BigDecimal(0);
        for (SalaryDetailEntity salaryDetailEntity :
                detailEntityList) {
            serviceAmountTotal = serviceAmountTotal.add(new BigDecimal(salaryDetailEntity.getServiceAmount()));
            taxAmountTotal = taxAmountTotal.add(new BigDecimal(salaryDetailEntity.getTaxAmount()));
            painInAmountTotal = painInAmountTotal.add(new BigDecimal(salaryDetailEntity.getPaidInAmount()));
        }
        exeWithdrawOrWxResult = exeWithdrawOrWx(batchId, salaryMerchantInfoEntity, detailSalaryEntityList, serviceAmountTotal, taxAmountTotal, painInAmountTotal, false, false);

        SalaryBatchResult salaryBatchResult = new SalaryBatchResult();
        salaryBatchResult.setBatchId(batchId);
        salaryBatchResult.setSalaryDetailEntityList(detailSalaryEntityList);
        exeWithdrawOrWxResult.setSalaryBatchResult(salaryBatchResult);
        return exeWithdrawOrWxResult;
    }

    /**
     * 执行提现或者Wx转账
     */
    public ExeWithdrawOrWxResult exeWithdrawOrWx(Long batchId, SalaryMerchantInfoEntity salaryMerchantInfoEntity, List<SalaryDetailEntity> detailSalaryEntityList,
                                                 BigDecimal serviceAmountTotal, BigDecimal taxAmountTotal, BigDecimal painInAmountTotal, boolean isCertify, boolean isRetry) {
        ExeWithdrawOrWxResult exeWithdrawOrWxResult = new ExeWithdrawOrWxResult();
        //根据batchId查出
        SalaryMerchantInfoEntity.ExtInfo extInfo = XbbJsonUtil.fromJsonString(salaryMerchantInfoEntity.getExtInfo(), SalaryMerchantInfoEntity.ExtInfo.class);
        //================未认证认证后certify接口需要的参数=================={
        SendWaitFundDispatchingMessage fundDispatchingMessage = new SendWaitFundDispatchingMessage();
        fundDispatchingMessage.setXcxWithdraw(extInfo.getXcxWithdraw() == CommonConst.ONE);
        fundDispatchingMessage.setTenantType(salaryMerchantInfoEntity.getTenantType());
        fundDispatchingMessage.setBatchId(String.valueOf(batchId));
        fundDispatchingMessage.setSalaryDetailEntityList(detailSalaryEntityList);
        if ((isCertify || isRetry) && SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE == salaryMerchantInfoEntity.getTenantType()) {
            Long subjectId = salaryMerchantInfoEntity.getPayerSubjectId();
            SubjectServiceProto.SubjectDetailBeResponse subjectObjectById = subjectServiceBlockingStub.getSubjectObjectById(SubjectServiceProto.IdBeRequest.newBuilder().setId(subjectId).build());
            fundDispatchingMessage.setDelayTime(System.currentTimeMillis());
            fundDispatchingMessage.setPayeeMerchantName(subjectObjectById.getSubjectName());
            fundDispatchingMessage.setPayeeMerchantNo(subjectObjectById.getWxCollectionBankCardNum());
            fundDispatchingMessage.setSpecialMerchantId(subjectObjectById.getMerchantAccount());
        }

        //================未认证认证后certify接口需要的参数==================}
        if (extInfo.getXcxWithdraw() == CommonConst.ONE) {
            //小程序提现
            if (SalaryServerProto.EnumTenantType.SAAS_VALUE == salaryMerchantInfoEntity.getTenantType()) {
                pushWithdraw(detailSalaryEntityList, salaryMerchantInfoEntity.getSpecialMerchantId(), salaryMerchantInfoEntity.getPayeeMerchantName(), salaryMerchantInfoEntity.getPayeeMerchantNo());

            }
            if (SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE == salaryMerchantInfoEntity.getTenantType()) {
                if (isCertify || isRetry) {
                    exeWithdrawOrWxResult.setSendWaitFundDispatching(true);
                    exeWithdrawOrWxResult.setFundDispatchingMessage(fundDispatchingMessage);
                    //rocketMsgService.sendWaitFundDispatching(fundDispatchingMessage);
                } else {
                    AssetXhServerProto.SubAccountQueryRequest.Builder subAccountQueryBuild = AssetXhServerProto.SubAccountQueryRequest.newBuilder();
                    subAccountQueryBuild.addSubAccountIds(salaryMerchantInfoEntity.getPayeeSubAccountId());
                    AssetXhServerProto.SubAccountResponses subAccountResponses = assetXhServerBlockingStub.querySubAccount(subAccountQueryBuild.build());
                    AssetXhServerProto.SubAccountResponse subAccountResponse = CollUtil.getFirst(subAccountResponses.getSubAccountInfosList());

                    if (null == subAccountResponse) {
                        throw GrpcException.asRuntimeException("未找到收款子账户");
                    }
                    //1.调度
                    fundDispatchingService.fundDispatching(salaryMerchantInfoEntity, batchId.toString(), serviceAmountTotal, taxAmountTotal, painInAmountTotal, XBB_USER_CONTEXT.get().getUserId());
                }

            }
        } else {
            //微信转账
            if (SalaryServerProto.EnumTenantType.SAAS_VALUE == salaryMerchantInfoEntity.getTenantType()) {
                if (isCertify) {
                    transferWx("2ND" + batchId, salaryMerchantInfoEntity.getPayeeMerchantNo(), salaryMerchantInfoEntity.getPayeeMerchantName(), salaryMerchantInfoEntity.getSpecialMerchantId(), detailSalaryEntityList);
                } else {
                    //微信转账
                    transferWx(batchId.toString(), salaryMerchantInfoEntity.getPayeeMerchantNo(), salaryMerchantInfoEntity.getPayeeMerchantName(), salaryMerchantInfoEntity.getSpecialMerchantId(), detailSalaryEntityList);
                }
            }
            if (SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE == salaryMerchantInfoEntity.getTenantType()) {
                if (isCertify || isRetry) {
                    exeWithdrawOrWxResult.setSendWaitFundDispatching(true);
                    exeWithdrawOrWxResult.setFundDispatchingMessage(fundDispatchingMessage);
                    //rocketMsgService.sendWaitFundDispatching(fundDispatchingMessage);
                } else {
                    //1.调度
                    fundDispatchingService.fundDispatching(salaryMerchantInfoEntity, batchId.toString(), serviceAmountTotal, taxAmountTotal, painInAmountTotal, XBB_USER_CONTEXT.get().getUserId());
                }
            }
        }
        return exeWithdrawOrWxResult;
    }

    /**
     * 保存发薪详情  计算服务费税费
     *
     * @param request
     * @param salaryMerchantInfoEntity
     * @param batchId
     * @param merchantBalance
     * @return
     */
    private List<SalaryDetailEntity> saveSalaryDetail(SalaryServerProto.SalaryBatchRequest request, SalaryMerchantInfoEntity salaryMerchantInfoEntity, Long batchId, SalaryServerProto.MerchantInfoResponse merchantBalance) {
        Integer deductCustomerCount = 0;//应扣客户费用总金额
        List<SalaryDetailEntity> detailEntityList = new ArrayList<>();
        List<String> idCardNoEncryptList = new ArrayList<>();
        for (SalaryServerProto.SalaryDetailRequest detailRequest : request.getDataList()) {
            idCardNoEncryptList.add(DesPlus.getInstance().encrypt(detailRequest.getIdCardNo(),
                    ParamUtil.getInstance().getStringValue("des_userxh_key")));
        }
        //批量查询实名认证了的用户信息并解密
        List<UserXhCEntity> userXhCEntities = userXhRepository.queryByIdCards(idCardNoEncryptList);
        Map<String, UserXhCEntity> userXhCEntityMap = new HashMap<>();
        for (UserXhCEntity userXhCEntity :
                userXhCEntities) {
            userXhCEntityMap.put(DesPlus.getInstance().decrypt(userXhCEntity.getIdCardNo(),
                    ParamUtil.getInstance().getStringValue("des_userxh_key")), userXhCEntity);
        }
        List<String> encryptIdCardNos = request.getDataList().stream().map(salaryDetailRequest -> {
            return DesPlus.getInstance().encrypt(salaryDetailRequest.getIdCardNo());
        }).distinct().collect(Collectors.toList());
        //计算税费
        //如果是代发用户 并且开启税费 取签署合同时间
        Map<String, List<ContractResult>> contractResultMap = new HashMap<>();
        Map<String, OneTimeSalaryResult> oneTimeSalaryMap = new HashMap<>();
        if (SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE == salaryMerchantInfoEntity.getTenantType() && salaryMerchantInfoEntity.getIndividualTax() == CommonConst.ONE) {

      /*      ContractPageParam contractPageParam = new ContractPageParam();
            contractPageParam.setPageSize(0);
            contractPageParam.setStatusList(Arrays.asList(SignServerProto.EnumContractStatus.EFFECTING_VALUE, SignServerProto.EnumContractStatus.OVERTIME_VALUE, SignServerProto.EnumContractStatus.EARLY_TERMINATION_VALUE));
            contractPageParam.setIdCardNos(encryptIdCardNos);
            contractPageParam.setType(SignServerProto.EnumTemplateType.CONTRACT_VALUE);
            PagePojoList<ContractResult> contractResultPagePojoList = contractRepository.contractList(contractPageParam);
            contractResultMap = contractResultPagePojoList.getData().stream().collect(Collectors.groupingBy(ContractResult::getIdCardNo, Collectors.toList()));*/
            List<OneTimeSalaryResult> oneTimeSalary = salaryDetailRepository.getOneTimeSalary(encryptIdCardNos, salaryMerchantInfoEntity.getPayerSubjectId());
            oneTimeSalaryMap = oneTimeSalary.stream().collect(Collectors.toMap(OneTimeSalaryResult::getIdCardNo, Function.identity()));

        }
        //如果是开启税费 并且是Sass用户
        Map<String, List<HrmServiceProto.EmployeePageResponse>> employeePageResponseMap = new HashMap<>();
        if (SalaryServerProto.EnumTenantType.SAAS_VALUE == salaryMerchantInfoEntity.getTenantType() && salaryMerchantInfoEntity.getIndividualTax() == CommonConst.ONE) {

            HrmServiceProto.EmployeePageBeRequest.Builder employeeBuild = HrmServiceProto.EmployeePageBeRequest.newBuilder();
            employeeBuild.addAllIdCards(request.getDataList().stream().map(SalaryServerProto.SalaryDetailRequest::getIdCardNo).distinct().collect(Collectors.toList()));
            employeeBuild.setTenantId(salaryMerchantInfoEntity.getId().intValue());
            HrmServiceProto.EmployeePageResponses employeePageList = hrmServiceBlockingStub.findEmployeePageList(employeeBuild.build());
            if (CollectionUtils.isEmpty(employeePageList.getDataList())) {
                throw GrpcException.asRuntimeException("没有表单里的在职员工数据");
            }
            employeePageResponseMap = employeePageList.getDataList().stream().collect(Collectors.groupingBy(HrmServiceProto.EmployeePageResponse::getIdCard, Collectors.toList()));
        }
        //获取当年的已发薪资总和和已缴税费总额
        Map<String, PayCountByIdCardResult> payCountByIdCardResultMap = new HashMap<>();
        if (salaryMerchantInfoEntity.getIndividualTax() == CommonConst.ONE) {
            List<PayCountByIdCardResult> payCountByIdCardList = salaryDetailRepository.getPayCountByIdCards(encryptIdCardNos, salaryMerchantInfoEntity.getPayerSubjectId());
            payCountByIdCardResultMap = payCountByIdCardList.stream().collect(Collectors.toMap(PayCountByIdCardResult::getIdCardNo, Function.identity()));
        }
        //插入明细列表
        for (SalaryServerProto.SalaryDetailRequest detailRequest : request.getDataList()) {


            SalaryDetailEntity salaryDetailEntity = SalaryConvert.request2Entity(detailRequest);
            //服务费率计算
            BigDecimal bigServiceAmount = NumberUtil.mul(new BigDecimal(detailRequest.getPayableAmount()), salaryMerchantInfoEntity.getServiceRate());
            DecimalFormat formatDecimal = new DecimalFormat("0");
            formatDecimal.setRoundingMode(RoundingMode.HALF_UP);
            Integer serviceAmount = Integer.valueOf(formatDecimal.format(bigServiceAmount));
            Integer freeOfDutyMonth = 0;
            if (SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE == salaryMerchantInfoEntity.getTenantType() && salaryMerchantInfoEntity.getIndividualTax() == CommonConst.ONE) {
                //计算税费 代发客户 获取签约时间逻辑
                OneTimeSalaryResult oneTimeSalaryResult = oneTimeSalaryMap.get(salaryDetailEntity.getIdCardNo());
                if (oneTimeSalaryResult == null) {
                    freeOfDutyMonth = CommonUtil.taxMonthCount(new Timestamp(System.currentTimeMillis()));
                } else {
                    //免税收入月数
                    freeOfDutyMonth = CommonUtil.taxMonthCount(oneTimeSalaryResult.getStartTime());
                }

            }
            //Saas客户   取员工入职时间
            if (SalaryServerProto.EnumTenantType.SAAS_VALUE == salaryMerchantInfoEntity.getTenantType() && salaryMerchantInfoEntity.getIndividualTax() == CommonConst.ONE) {
                List<HrmServiceProto.EmployeePageResponse> employeePageResponses = employeePageResponseMap.get(detailRequest.getIdCardNo());
                if (CollectionUtils.isEmpty(employeePageResponses)) {
                    throw GrpcException.asRuntimeException("没有表单里" + salaryDetailEntity.getName() + "的在职员工数据");
                } else {
                    log.info("员工" + detailRequest.getIdCardNo() + "入职日期" + new Timestamp(employeePageResponses.get(0).getLastEntryDate()));
                    if (employeePageResponses.get(0).getLastEntryDate() == 0) {
                        throw GrpcException.asRuntimeException("员工" + detailRequest.getIdCardNo() + "没有在职时间");
                    }
                    freeOfDutyMonth = CommonUtil.taxMonthCount(new Timestamp(employeePageResponses.get(0).getLastEntryDate()));
                }
                log.info("员工" + detailRequest.getIdCardNo() + "免税月数" + freeOfDutyMonth);
            }
            //计算税费的方法
            Integer taxAmount = 0;
            if (salaryMerchantInfoEntity.getIndividualTax() == CommonConst.ONE) {
                PayCountByIdCardResult payCountByIdCardResult = payCountByIdCardResultMap.get(salaryDetailEntity.getIdCardNo());
                if (payCountByIdCardResult == null) {
                    taxAmount = TaxUtil.IIT(detailRequest.getPayableAmount(), detailRequest.getPayableAmount(), freeOfDutyMonth, 0, 0, 0);
                } else {
                    taxAmount = TaxUtil.IIT(payCountByIdCardResult.getTotalPaidAble() + detailRequest.getPayableAmount(), detailRequest.getPayableAmount(), freeOfDutyMonth, 0, 0, payCountByIdCardResult.getTotalTax());
                }

            }

            salaryDetailEntity.setBatchId(batchId);
            salaryDetailEntity.setTenantId(XBB_USER_CONTEXT.tenantId());

            if (salaryMerchantInfoEntity.getIndividualTax() == CommonConst.ONE) {
                log.info("员工" + detailRequest.getIdCardNo() + "计算的税费为" + taxAmount);
                salaryDetailEntity.setTaxAmount(taxAmount); //启用个税 用算出来的
            } else {
                log.info("员工" + detailRequest.getIdCardNo() + "导入时填写的税费为" + detailRequest.getTaxAmount());
                salaryDetailEntity.setTaxAmount(detailRequest.getTaxAmount()); //不启用个税 用表导入过来的
            }
            //重试次数 非重试 第一次默认为0
            salaryDetailEntity.setRetryCount(0);
            //服务费
            salaryDetailEntity.setServiceAmount(serviceAmount);
            //应发金额
            salaryDetailEntity.setPayableAmount(detailRequest.getPayableAmount());
            //实发金额
            salaryDetailEntity.setPaidInAmount(detailRequest.getPayableAmount() - salaryDetailEntity.getTaxAmount());
            //主体id
            salaryDetailEntity.setSubjectId(salaryMerchantInfoEntity.getPayerSubjectId());
            salaryDetailEntity.setOperatorId(XBB_USER_CONTEXT.get().getUserId());
            salaryDetailEntity.setId(SnowflakeIdGenerator.getId());
            boolean realNameAuthFlag = true;
            if (userXhCEntityMap.get(detailRequest.getIdCardNo()) == null) {
                //支付处理中（未认证）
                salaryDetailEntity.setStatus(SalaryServerProto.EnumSalaryDetailStatus.PAYING_NOT_AUTH.getNumber());
                realNameAuthFlag = false;
            }
            if (realNameAuthFlag) {
                SalaryOpenIdEntity salaryOpenIdEntity = salaryOpenIdRepository.queryByIdCard(detailRequest.getIdCardNo());
                if (salaryOpenIdEntity == null) {
                    //支付处理中（无openId）
                    salaryDetailEntity.setStatus(SalaryServerProto.EnumSalaryDetailStatus.PAYING_NOT_AUTH.getNumber());
                } else {
                    //set openId
                    salaryDetailEntity.setStatus(SalaryServerProto.EnumSalaryDetailStatus.PAYING_ALREADY_HANDLE.getNumber());
                    salaryDetailEntity.setOpenId(salaryOpenIdEntity.getOpenId());
                    //detailSalaryEntityList.add(salaryDetailEntity);
                }
            }
            deductCustomerCount += (salaryDetailEntity.getPayableAmount() + salaryDetailEntity.getServiceAmount());
            detailEntityList.add(salaryDetailEntity);
        }

        //判断余额是否充足
        if (new BigDecimal(merchantBalance.getBalance()).compareTo(new BigDecimal(deductCustomerCount)) < 0) {
            throw GrpcException.asRuntimeException("余额不足");
        }
        salaryDetailRepository.batchInsert(detailEntityList);
        return detailEntityList;
    }

    /**
     * 保存流水
     *
     * @param request
     * @param batchId
     * @param merchantBalance
     */
    private void saveFlow(SalaryServerProto.SalaryBatchRequest request, Long batchId, SalaryServerProto.MerchantInfoResponse merchantBalance) {
        SalaryMerchantFlowEntity salaryMerchantFlowEntity = new SalaryMerchantFlowEntity();
        salaryMerchantFlowEntity.setTenantId(XBB_USER_CONTEXT.get().getTenantId().longValue());
        salaryMerchantFlowEntity.setFlowNo(request.getSalaryFile());
        salaryMerchantFlowEntity.setOperationType(SalaryServerProto.EnumFlowOperationType.EXPENDITURE.getNumber());
        salaryMerchantFlowEntity.setOperatorId(XBB_USER_CONTEXT.get().getUserId());
        salaryMerchantFlowEntity.setOperationAmount(0);
        salaryMerchantFlowEntity.setRemarks("薪酬云无卡发薪");
        salaryMerchantFlowEntity.setSalaryBatchId(batchId);
        salaryMerchantFlowEntity.setSubjectName(merchantBalance.getPayeeMerchantName());
        String subAccountId = (merchantBalance.hasPayeeSubAccountId() ? (merchantBalance.getPayeeSubAccountId() + "") : null);
        salaryMerchantFlowEntity.setPayeeInfoId(merchantBalance.getTenantType() == SalaryServerProto.EnumTenantType.SAAS ? merchantBalance.getSpecialMerchantId() : subAccountId);

        salaryMerchantFlowRepository.insert(salaryMerchantFlowEntity);
    }


    //推送提现
    public void pushWithdraw(List<SalaryDetailEntity> salaryDetailEntityList,
                             String specialMerchantId,
                             String payeeMerchantName,
                             String payeeMerchantNo
    ) {

        if (CollUtil.isNotEmpty(salaryDetailEntityList)) {
            for (SalaryDetailEntity salaryDetailEntity :
                    salaryDetailEntityList) {
                salaryDetailEntity.setStatus(SalaryServerProto.EnumSalaryDetailStatus.WAIT_WITHDRAW.getNumber());
            }
            //更新状态 待提现 翻新批次状态
            updateDetailStatus(salaryDetailEntityList);
            //调MQ
            XcyPayrollMessage xcyPayrollMessage = new XcyPayrollMessage();
            Random r = new Random();
            xcyPayrollMessage.setBatchNo(System.currentTimeMillis() + "num" + StringUtils.substring(String.valueOf(r.nextLong()), -3));
            xcyPayrollMessage.setSourceId(salaryDetailEntityList.get(0).getBatchId());
            xcyPayrollMessage.setPayrollWxSubMchid(specialMerchantId);
            List<XcyPayrollMessageDto> xcyPayrollDetailDtoList = new ArrayList<>();
            for (SalaryDetailEntity salaryDetailEntity :
                    salaryDetailEntityList) {
                XcyPayrollMessageDto xcyPayrollMessageDto = new XcyPayrollMessageDto();
                xcyPayrollMessageDto.setSourceId(salaryDetailEntity.getId());
                xcyPayrollMessageDto.setName(salaryDetailEntity.getName());
                xcyPayrollMessageDto.setMoney(new BigDecimal(salaryDetailEntity.getPaidInAmount())); //分
                xcyPayrollMessageDto.setIdCardNo(DesPlus.getInstance().decrypt(salaryDetailEntity.getIdCardNo()));
                xcyPayrollMessageDto.setOpenId(salaryDetailEntity.getOpenId());
                xcyPayrollDetailDtoList.add(xcyPayrollMessageDto);
            }
            xcyPayrollMessage.setXcyPayrollDetailDtoList(xcyPayrollDetailDtoList);
            List<XcyPayrollMessage> xcyPayrollMessageList = new ArrayList<>();
            xcyPayrollMessageList.add(xcyPayrollMessage);
            rocketMsgService.sendToPayroll(xcyPayrollMessageList);

        }
    }

    /**
     * 微信转账
     *
     * @param batchId
     * @param payeeMerchantNo
     * @param payeeMerchantName
     * @param specialMerchantId
     * @param detailSalaryEntityList
     */
    public void transferWx(String batchId, String payeeMerchantNo, String payeeMerchantName, String specialMerchantId,
                           List<SalaryDetailEntity> detailSalaryEntityList) {
        if (CollUtil.isNotEmpty(detailSalaryEntityList)) {
            AssetXhServerProto.WxBatchTransferRequest.Builder builder = AssetXhServerProto.WxBatchTransferRequest.newBuilder();

            builder.setWxMerchantId(payeeMerchantNo);
            builder.setWxMerchantName(payeeMerchantName);
            builder.setWxSubMchid(specialMerchantId);

            if (XBB_USER_CONTEXT != null && XBB_USER_CONTEXT.get() != null) {
                builder.setOperator(XBB_USER_CONTEXT.get().getUserId());
            } else {
                builder.setOperator(1);
            }

            builder.setRemark1("薪酬云无卡发薪");

            builder.setRemark2("薪酬云无卡发薪");


            builder.setSystemPayType(AssetXhServerProto.EnumSystemPayType.SP_XCY_WITHOUT_CARD_PAY);
            builder.setBatchNo(batchId);
            builder.setBusinessType(AssetXhServerProto.EnumBusinessType.WX2C);

            Integer totalSalaryCount = 0;
            for (SalaryDetailEntity salaryDetailEntity :
                    detailSalaryEntityList) {

                AssetXhServerProto.WxDetailTransferRequest.Builder wxDetailTransferRequest = AssetXhServerProto.WxDetailTransferRequest.newBuilder();
                wxDetailTransferRequest.setOpenId(salaryDetailEntity.getOpenId());
                wxDetailTransferRequest.setUserName(salaryDetailEntity.getName());
                wxDetailTransferRequest.setRemark1("工资");
                wxDetailTransferRequest.setRemark2("工资");
                wxDetailTransferRequest.setDetailNo(salaryDetailEntity.getId().toString() + "_" + salaryDetailEntity.getRetryCount());
                wxDetailTransferRequest.setAmount(salaryDetailEntity.getPaidInAmount().toString());
                //加一层判断 不是支付处理中的不给发钱
                if (SalaryServerProto.EnumSalaryDetailStatus.PAYING_ALREADY_HANDLE.getNumber() == salaryDetailEntity.getStatus()) {
                    builder.addWxDetailTransferRequest(wxDetailTransferRequest);
                }
                totalSalaryCount = NumberUtil.add(totalSalaryCount, salaryDetailEntity.getPaidInAmount()).intValue();
            }
            if (CollUtil.isNotEmpty(builder.getWxDetailTransferRequestList())) {
                assetXhServerBlockingStub.wxBatchTransfer(builder.build());
            }
        }
    }

    /**
     * 保存项目和批次
     *
     * @param request
     * @param salaryMerchantInfoEntity
     * @return
     */
    @NotNull
    private Long saveProductAndBatchTable(SalaryServerProto.SalaryBatchRequest request, SalaryMerchantInfoEntity salaryMerchantInfoEntity) {
        //根据项目名称 查询数据库是否已经有 有的话直接用 没有的话新增
        List<SalaryProductEntity> salaryProductEntities = salaryProductRepository.queryByParam(request.getProductName());

        SalaryBatchEntity salaryBatchEntity = new SalaryBatchEntity();
        if (CollUtil.isNotEmpty(salaryProductEntities)) {
            salaryBatchEntity.setProductId(salaryProductEntities.get(0).getId());
        } else {
            SalaryProductEntity salaryProductEntity = new SalaryProductEntity();
            salaryProductEntity.setOperatorId(XBB_USER_CONTEXT.get().getUserId());
            salaryProductEntity.setTenantId(XBB_USER_CONTEXT.get().getTenantId());
            salaryProductEntity.setName(request.getProductName());
            Long productId = salaryProductRepository.insert(salaryProductEntity).longValue();
            salaryBatchEntity.setProductId(productId);
        }
        salaryBatchEntity.setSalaryFile(request.getSalaryFile());
        salaryBatchEntity.setMonth(request.getMonth());
        salaryBatchEntity.setSource(salaryMerchantInfoEntity.getTenantType());
        salaryBatchEntity.setStatus(EnumSalaryBatchStatus.PAYING.getCode());
        salaryBatchEntity.setOperatorId(XBB_USER_CONTEXT.get().getUserId());
        SalaryMerchantInfoEntity.ExtInfo extInfo = XbbJsonUtil.fromJsonString(salaryMerchantInfoEntity.getExtInfo(), SalaryMerchantInfoEntity.ExtInfo.class);
        salaryBatchEntity.setPayMethod(extInfo.getXcxWithdraw());
        //保存发薪主体
        salaryBatchEntity.setSubjectId(salaryMerchantInfoEntity.getPayerSubjectId());
        //保存账户信息的json
        salaryBatchEntity.setExpandJson(XbbJsonUtil.toJsonString(salaryMerchantInfoEntity));
        Long batchId = salaryBatchRepository.insert(salaryBatchEntity).longValue();
        return batchId;
    }

    public SalaryServerProto.SalaryProductListResponse querySalaryProduct() {
        List<SalaryProductEntity> salaryProductEntities = salaryProductRepository.queryByParam(null);
        SalaryServerProto.SalaryProductListResponse.Builder builder = SalaryServerProto.SalaryProductListResponse.newBuilder();
        for (SalaryProductEntity s :
                salaryProductEntities) {
            builder.addData(SalaryConvert.entity2Response(s));
        }
        return builder.build();
    }

    public PagePojoList<SalaryBatchResult> findSalaryBatchPageList(SalaryServerProto.SalaryBatchConditionBeRequest request) {
        return salaryBatchRepository.findSalaryBatchPageList(SalaryConvert.request2Param(request));
    }


    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public Empty updateDetailStatus(List<SalaryDetailEntity> salaryDetailEntities) {

        if (CollUtil.isNotEmpty(salaryDetailEntities)) {
            for (SalaryDetailEntity salaryDetailEntity : salaryDetailEntities) {
                salaryDetailEntity.setUpdatedAt(null);
                salaryDetailRepository.updateById(salaryDetailEntity.getId(), salaryDetailEntity);
            }
            List<Long> batchIdList = salaryDetailEntities.stream().map(SalaryDetailEntity::getBatchId).distinct().collect(Collectors.toList());
            log.info("updateDetailStatus batchIdList=============" + XbbJsonUtil.toJsonString(batchIdList));
            for (Long batchId :
                    batchIdList) {
                SalaryDetailStatusCountResult countStatusInfo = salaryDetailRepository.getCountStatusInfo(batchId);
                SalaryBatchEntity salaryBatchEntity = new SalaryBatchEntity();
                //所有进行中的都已经完成 才计算
                if (countStatusInfo.getPayingNotAuthCount() == 0 && countStatusInfo.getPayingAlreadyHandleCount() == 0) {

                    boolean updateFlowflag = false;

                    if ((countStatusInfo.getAlreadyPaidCount() > 0 || countStatusInfo.getWaitWithdrawCount() > 0 || countStatusInfo.getWithdrawing() > 0
                            || countStatusInfo.getWithdrawSuccess() > 0 || countStatusInfo.getWithdrawFailed() > 0 || countStatusInfo.getCanceling() > 0 || countStatusInfo.getCancelFailed() > 0)
                            && (countStatusInfo.getPayFailCount() > 0 || countStatusInfo.getCancelledCount() > 0)) {
                        //部分失败
                        salaryBatchEntity.setStatus(EnumSalaryBatchStatus.PART_FAIL.getCode());
                        updateFlowflag = true;
                    }
                    if (countStatusInfo.getPayFailCount() == 0 && countStatusInfo.getCancelledCount() == 0) {
                        //全部成功 已发薪
                        salaryBatchEntity.setStatus(EnumSalaryBatchStatus.ALL_SUCCESS.getCode());
                        updateFlowflag = true;
                    }
                    if (countStatusInfo.getAlreadyPaidCount() == 0 && countStatusInfo.getWaitWithdrawCount() == 0 && countStatusInfo.getWithdrawing() == 0
                            && countStatusInfo.getWithdrawSuccess() == 0 && countStatusInfo.getWithdrawFailed() == 0 && countStatusInfo.getCanceling() == 0 && countStatusInfo.getCancelFailed() == 0) {
                        //全部失败
                        salaryBatchEntity.setStatus(EnumSalaryBatchStatus.ALL_FAIL.getCode());
                        updateFlowflag = true;
                    }
                    log.info("updateDetailStatus updateFlowflag=============" + updateFlowflag);
                    if (updateFlowflag) {
                        salaryBatchEntity.setUpdatedAt(null);
                        salaryBatchRepository.updateById(batchId, salaryBatchEntity);
                        QuerySalaryMerchantFlow querySalaryMerchantFlow = new QuerySalaryMerchantFlow();
                        querySalaryMerchantFlow.setBatchIds(Collections.singletonList(batchId));
                        List<SalaryMerchantFlowEntity> merchantFlowList = salaryMerchantFlowRepository.query(querySalaryMerchantFlow);

                        SalaryMerchantFlowEntity salaryMerchantFlowEntity = CollUtil.getFirst(merchantFlowList);
                        SalaryDetailPageParam salaryDetailPageParam = new SalaryDetailPageParam();
                        salaryDetailPageParam.setBatchId(batchId);
                        salaryDetailPageParam.setPage(null);
                        PagePojoList<SalaryDetailResult> salaryDetailPageList = salaryDetailRepository.findSalaryDetailPageList(salaryDetailPageParam);
                        if (salaryDetailPageList != null && CollUtil.isNotEmpty(salaryDetailPageList.getData())) {
                            log.info("updateDetailStatus salaryDetailPageList=============" + XbbJsonUtil.toJsonString(salaryDetailPageList));
                            Integer updateFlowAmount = 0;
                            SalaryMerchantInfoEntity salaryMerchantInfoEntity = salaryMerchantInfoRepository.findById(salaryDetailPageList.getData().get(0).getTenantId());
                            for (SalaryDetailResult salaryDetailResult :
                                    salaryDetailPageList.getData()) {
                                if (SalaryServerProto.EnumSalaryDetailStatus.ALREADY_PAID.getNumber() == salaryDetailResult.getStatus() || SalaryServerProto.EnumSalaryDetailStatus.WAIT_WITHDRAW.getNumber() == salaryDetailResult.getStatus()) {
                                    if (SalaryServerProto.EnumTenantType.SAAS_VALUE == salaryMerchantInfoEntity.getTenantType()) {
                                        updateFlowAmount = NumberUtil.add(updateFlowAmount, salaryDetailResult.getPaidInAmount()).intValue();
                                    }
                                    if (SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE == salaryMerchantInfoEntity.getTenantType()) {
                                        updateFlowAmount = NumberUtil.add(updateFlowAmount, salaryDetailResult.getPayableAmount(), salaryDetailResult.getServiceAmount()).intValue();
                                    }

                                }
                            }
                            if (salaryMerchantFlowEntity != null) {
                                log.info("updateDetailStatus salaryMerchantFlowEntity=============" + XbbJsonUtil.toJsonString(salaryMerchantFlowEntity));
                                salaryMerchantFlowRepository.updateById(salaryMerchantFlowEntity.getId(), new SalaryMerchantFlowEntity().setOperationAmount(updateFlowAmount));
                            }
                        }
                    }


                    //发送短信
                    sendSms(salaryDetailEntities, batchId);

                }
            }

        }
        return Empty.newBuilder().build();
    }

    private void sendSms(List<SalaryDetailEntity> salaryDetailEntities, Long batchId) {
        try {
            SalaryBatchEntity salaryBatchEntityById = salaryBatchRepository.findById(batchId, SalaryBatchEntity.class);
            List<SalaryDetailEntity> salarySuccessList = salaryDetailEntities.stream().filter(detail -> SalaryServerProto.EnumSalaryDetailStatus.ALREADY_PAID.getNumber() == detail.getStatus()).collect(Collectors.toList());
            log.info("要发短信的list{}", XbbJsonUtil.toJsonString(salarySuccessList));
            if (CollUtil.isNotEmpty(salarySuccessList)) {
                List<List<SalaryDetailEntity>> partition = ListUtil.partition(salarySuccessList, 400);
                for (List<SalaryDetailEntity> part : partition) {
                    SalaryDetailPageParam param = new SalaryDetailPageParam();
                    param.setPage(null);
                    param.setIds(part.stream().map(XbbSnowTimeTenantEntity::getId).collect(Collectors.toList()));
                    PagePojoList<SalaryDetailResult> salaryDetailPageList = salaryDetailRepository.findSalaryDetailPageList(param);
                    if (CollUtil.isNotEmpty(salaryDetailPageList.getData())) {
                        List<String> telList = salaryDetailPageList.getData().stream().map(SalaryDetailResult::getPhone).distinct().collect(Collectors.toList());
                        UniversalServiceProto.SendMessageBeRequest.Builder sendBuilder = UniversalServiceProto.SendMessageBeRequest.newBuilder();
                        sendBuilder.setTemplateCode("T00046");
                        sendBuilder.setTenantId(salaryBatchEntityById.getTenantId());
                        sendBuilder.setContent("{}");
                        sendBuilder.addAllTels(telList);
                        universalServiceBlockingStub.sendSmsMessage(sendBuilder.build());
                    }
                }
            }
        } catch (Exception e) {
            log.info("短信发送异常{}", e);
        }
    }


    public Empty certifyUpdateSalaryDetail(SalaryServerProto.CertificationRequest request) {
        ExeWithdrawOrWxResult exeWithdrawOrWxResult = handleCertify(request);
        if (exeWithdrawOrWxResult.isSendWaitFundDispatching()) {
            rocketMsgService.sendWaitFundDispatching(exeWithdrawOrWxResult.getFundDispatchingMessage());
        }
        return Empty.newBuilder().build();
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public ExeWithdrawOrWxResult handleCertify(SalaryServerProto.CertificationRequest request) {
        ExeWithdrawOrWxResult exeWithdrawOrWxResult = new ExeWithdrawOrWxResult();
        //同步到openId表
        SalaryOpenIdEntity openIdEntity = new SalaryOpenIdEntity();
        openIdEntity.setOpenId(request.getOpenId());
        openIdEntity.setIdCardNo(DesPlus.getInstance().encrypt(request.getIdCardNo()));
        openIdEntity.setUserXhCId(request.getUserXhCId());
        salaryOpenIdRepository.insert(openIdEntity);

        //更新该批次下的所有发薪明细状态
        SalaryDetailPageParam salaryDetailPageParam = new SalaryDetailPageParam();
        salaryDetailPageParam.setBatchId(request.getBatchId());
        salaryDetailPageParam.setIdCardNo(DesPlus.getInstance().encrypt(request.getIdCardNo()));
        salaryDetailPageParam.setDetailStatus(Arrays.asList(SalaryServerProto.EnumSalaryDetailStatus.PAYING_NOT_AUTH.getNumber()));
        salaryDetailPageParam.setPage(null);
        PagePojoList<SalaryDetailResult> salaryDetailPageList = salaryDetailRepository.findSalaryDetailPageList(salaryDetailPageParam);
        if (salaryDetailPageList != null && CollUtil.isNotEmpty(salaryDetailPageList.getData())) {
            List<SalaryDetailResult> salaryDetailResultList = salaryDetailPageList.getData();

            //批量更新
            salaryDetailRepository.updateByDetailIds(salaryDetailResultList.stream().map(SalaryDetailResult::getId).collect(Collectors.toList()), request.getOpenId());

            List<SalaryDetailEntity> salaryDetailEntityList = new ArrayList<>();
            for (SalaryDetailResult salaryDetailResult :
                    salaryDetailResultList) {
                SalaryDetailEntity salaryDetailEntity = new SalaryDetailEntity();
                BeanUtil.copyProperties(salaryDetailResult, salaryDetailEntity);
                salaryDetailEntity.setId(salaryDetailResult.getDetailId());
                salaryDetailEntity.setStatus(SalaryServerProto.EnumSalaryDetailStatus.PAYING_ALREADY_HANDLE.getNumber());//主动更新set已经变更的状态
                salaryDetailEntity.setOpenId(request.getOpenId());
                salaryDetailEntityList.add(salaryDetailEntity);
            }

            SalaryBatchEntity salaryBatchEntity = salaryBatchRepository.findById(request.getBatchId(), SalaryBatchEntity.class);
            //商户信息取之前发薪批次里保存的json
            SalaryMerchantInfoEntity salaryMerchantInfoEntity = XbbJsonUtil.fromJsonString(salaryBatchEntity.getExpandJson(), SalaryMerchantInfoEntity.class);

            exeWithdrawOrWxResult = exeWithdrawOrWx(request.getBatchId(), salaryMerchantInfoEntity, salaryDetailEntityList, null, null, null, true, false);


        }
        return exeWithdrawOrWxResult;
    }

    public SalaryServerProto.UserXhCListResponse queryUserXhCByIdCards(SalaryServerProto.QueryByIdCardsRequest queryByIdCardsRequest) {

        List<String> idCardNoEncryptList = new ArrayList<>();
        for (String idCard : queryByIdCardsRequest.getIdCardsList()) {
            idCardNoEncryptList.add(DesPlus.getInstance().encrypt(idCard,
                    ParamUtil.getInstance().getStringValue("des_userxh_key")));
        }
        //批量查询实名认证了的用户信息并解密
        List<UserXhCEntity> userXhCEntities = userXhRepository.queryByIdCards(idCardNoEncryptList);
        List<SalaryServerProto.UserXhCResponse> userXhCResponseList = new ArrayList<>();
        for (UserXhCEntity userXhCEntity :
                userXhCEntities) {
            SalaryServerProto.UserXhCResponse.Builder userXhCBuilder = SalaryServerProto.UserXhCResponse.newBuilder();
            userXhCBuilder.setIdCardNo(DesPlus.getInstance().decrypt(userXhCEntity.getIdCardNo(),
                    ParamUtil.getInstance().getStringValue("des_userxh_key")));
            userXhCBuilder.setRealName(userXhCEntity.getRealName());
            userXhCResponseList.add(userXhCBuilder.build());
        }

        SalaryServerProto.UserXhCListResponse.Builder builder = SalaryServerProto.UserXhCListResponse.newBuilder();
        builder.addAllData(userXhCResponseList);

        return builder.build();
    }

    public SalaryServerProto.MerchantInfoListResponse queryMerchantInfoList(SalaryServerProto.MerchantInfoListRequest request) {
        List<SalaryMerchantInfoEntity> salaryMerchantInfoEntityList = salaryMerchantInfoRepository.query(SalaryConvert.request2Param(request));
        SalaryServerProto.MerchantInfoListResponse.Builder builder = SalaryServerProto.MerchantInfoListResponse.newBuilder();
        for (SalaryMerchantInfoEntity salaryMerchantInfo : salaryMerchantInfoEntityList) {
            builder.addMerchantInfoList(SalaryConvert.entity2Response(salaryMerchantInfo));
        }
        return builder.build();
    }

    public SalaryServerProto.TenantAccountPageResponse findTenantAccount(SalaryServerProto.TenantAccountPageRequest request) {
        SalaryServerProto.TenantAccountPageResponse.Builder builder = SalaryServerProto.TenantAccountPageResponse.newBuilder();
        builder.setPage(request.getPage())
                .setSize(request.getSize());

        Set<Long> subIds = new HashSet<>(request.getSubAccountIdsList());
        if (CollUtil.isNotEmpty(request.getTenantIdsList())) {
            List<SalaryMerchantInfoEntity> salaryMerchantInfoEntityList = salaryMerchantInfoRepository.query(new QuerySalaryMerchantInfo().setIds(request.getTenantIdsList())
                    .setTenantTypes(Collections.singletonList(SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE))
                    .setPayeeSubAccountIds(subIds)
            );
            subIds = new HashSet<>(salaryMerchantInfoEntityList.size());
            for (SalaryMerchantInfoEntity salaryMerchantInfo : salaryMerchantInfoEntityList) {
                if (salaryMerchantInfo.getPayeeSubAccountId() != null) {
                    subIds.add(salaryMerchantInfo.getPayeeSubAccountId());
                }
            }
            if (CollUtil.isEmpty(subIds)) {
                return builder.build();
            }
        }
        List<SalaryServerProto.TenantAccountResponse.Builder> list = Lists.newArrayList();
        AssetXhServerProto.SubAccountQueryRequest.Builder subAccountQueryBuilder = AssetXhServerProto.SubAccountQueryRequest.newBuilder();
        if (CollUtil.isNotEmpty(request.getCustomerSubAccountTypesList())) {
            subAccountQueryBuilder.addAllCustomerSubAccountTypesValue(request.getCustomerSubAccountTypesValueList());
        }
        Set<Long> subjectIds = new HashSet<>(request.getSubjectIdsList());
        AssetXhServerProto.SubAccountResponses subAccountResponses = assetXhServerBlockingStub.querySubAccount(subAccountQueryBuilder
                .addAllSubjectInfoIds(subjectIds)
                .addAllSubAccountIds(subIds)
                .setPageable(request.getSize() != CommonConst.ZERO)
                .setPage(request.getPage())
                .setPageSize(request.getSize())
                .build());
        for (AssetXhServerProto.SubAccountResponse subAccountResponse : subAccountResponses.getSubAccountInfosList()) {
            list.add(SalaryConvert.response2Response(subAccountResponse));
            subjectIds.add(subAccountResponse.getSubjectInfoId());
            subIds.add(subAccountResponse.getSubAccountId());
        }

        SubjectServiceProto.SubjectDetailBeResponses subjectDetailBeResponses = subjectServiceBlockingStub.getSubjectObjectByIds(SubjectServiceProto.IdBeRequests.newBuilder()
                .addAllId(subjectIds)
                .build());
        Map<Long, SubjectServiceProto.SubjectDetailBeResponse> subjectDetailBeResponseMap = subjectDetailBeResponses.getDataList().stream().collect(Collectors.toMap(SubjectServiceProto.SubjectDetailBeResponse::getSubjectId, Function.identity()));

        List<SalaryMerchantInfoEntity> salaryMerchantInfoEntityList = salaryMerchantInfoRepository.query(new QuerySalaryMerchantInfo().setPayeeSubAccountIds(subIds).setTenantTypes(Collections.singletonList(SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE)));
        Map<Long, List<SalaryMerchantInfoEntity>> salaryMerchantInfoEntityListMap = salaryMerchantInfoEntityList.stream().filter(item -> item.getPayeeSubAccountId() != null).collect(Collectors.groupingBy(SalaryMerchantInfoEntity::getPayeeSubAccountId));
        Set<Long> tenantIds = salaryMerchantInfoEntityList.stream().map(SalaryMerchantInfoEntity::getId).collect(Collectors.toSet());
        Map<Integer, PortalServiceProto.TenantBeResponse> tenantIdBeResponseMap = tenantIdBeResponseMap(tenantIds);
        for (SalaryServerProto.TenantAccountResponse.Builder responseBuild : list) {
            SubjectServiceProto.SubjectDetailBeResponse detailBeResponse = subjectDetailBeResponseMap.get(responseBuild.getSubjectId());
            if (detailBeResponse != null) {
                responseBuild.setSubjectName(detailBeResponse.getSubjectName());
                responseBuild.setBankCardNo(detailBeResponse.getBankCardNum() + responseBuild.getBankCardNo());
            }
            //一个子账号，多个租户
            List<SalaryMerchantInfoEntity> salaryInfoList = salaryMerchantInfoEntityListMap.get(responseBuild.getSubAccountId());
            if (CollUtil.isNotEmpty(salaryInfoList)) {
                for (SalaryMerchantInfoEntity salaryMerchantInfo : salaryInfoList) {
                    PortalServiceProto.TenantBeResponse tenantBeResponse = tenantIdBeResponseMap.get(salaryMerchantInfo.getId().intValue());
                    if (tenantBeResponse != null) {
                        responseBuild.addTenantInfo(SalaryConvert.response2Response(tenantBeResponse));
                    }
                }
            }
            builder.addData(responseBuild);
        }

        builder.setTotal(subAccountResponses.getTotalCount());

        return builder.build();
    }

    /**
     * 获取租户名称Map
     */
    private Map<Integer, PortalServiceProto.TenantBeResponse> tenantIdBeResponseMap(Collection<Long> tenantIds) {
        if (CollUtil.isEmpty(tenantIds)) {
            return Maps.newHashMap();
        }
        PortalServiceProto.TenantBeResponses tenantPageListRep = portalServiceBlockingStub.findTenantPageList(PortalServiceProto.TenantPageQueryBeRequest.newBuilder().setPaged(false).addAllTenantIds(tenantIds).build());
        return tenantPageListRep.getDataList().stream().collect(Collectors.toMap(PortalServiceProto.TenantBeResponse::getTenantId, Function.identity()));
    }

    /**
     * 交易重试
     *
     * @param batchId
     */
    public void breakpointRetry(Long batchId) {
        String redisLockKey = "CXY_BREAKPOINT_RETRY::" + batchId;
        try {
            //校验重试
            if (!redisLockService.tryLock(redisLockKey, TimeUnit.SECONDS, 1, 5)) {
                throw GrpcException.runtimeException(Status.ALREADY_EXISTS, "重试正在处理中,请勿重复发起");
            }
            SalaryBatchEntity salaryBatchEntity = salaryBatchRepository.findById(batchId, SalaryBatchEntity.class);


            checkBreakpointRetry(salaryBatchEntity);

            FundDispatchingEvent fundDispatchingEvent = fundDispatchingService.getFundDispatchingEvent(batchId.toString());
            if (fundDispatchingEvent.getFundDispatchingEnd()) {
                List<SalaryDetailEntity> detailEntityList = salaryDetailRepository.list(new SalaryDetailQueryListParam().setBatchId(batchId));
                List<SalaryDetailEntity> retryDetailEntityList = Lists.newArrayList();
                for (SalaryDetailEntity detailEntity : detailEntityList) {
                    if (detailEntity.getStatus() == SalaryServerProto.EnumSalaryDetailStatus.PAY_FAIL_VALUE) {
                        detailEntity.setStatus(SalaryServerProto.EnumSalaryDetailStatus.PAYING_ALREADY_HANDLE_VALUE)
                                .setRetryCount(detailEntity.getRetryCount() + 1);
                        retryDetailEntityList.add(detailEntity);
                    }
                }
                if (CollUtil.isEmpty(retryDetailEntityList)) {
                    throw new SystemRuntimeException("不存在可重试条目");
                }
                salaryDetailRepository.updateDetailStatus(retryDetailEntityList.stream().map(SalaryDetailEntity::getId).collect(Collectors.toList()),
                        SalaryServerProto.EnumSalaryDetailStatus.PAYING_ALREADY_HANDLE_VALUE,
                        true
                );
                //商户信息取之前发薪批次里保存的json
                SalaryMerchantInfoEntity salaryMerchantInfoEntity = XbbJsonUtil.fromJsonString(salaryBatchEntity.getExpandJson(), SalaryMerchantInfoEntity.class);

                //重试详情发薪
                ExeWithdrawOrWxResult exeWithdrawOrWxResult = exeWithdrawOrWx(batchId, salaryMerchantInfoEntity, retryDetailEntityList, null, null, null, false, true);
                if (exeWithdrawOrWxResult.isSendWaitFundDispatching()) {
                    rocketMsgService.sendWaitFundDispatching(exeWithdrawOrWxResult.getFundDispatchingMessage());
                }
            } else {
                List<FundDispatchingEntity> fundDispatchingEntityList = fundDispatchingEvent.getFundDispatchingEntityList();
                List<FundDispatchingEntity> retryFundDispatchingEntityList = Lists.newArrayList();
                for (FundDispatchingEntity fundDispatching : fundDispatchingEntityList) {
                    if (EnumDispatchStatus.FAILURE.getCode().equals(fundDispatching.getDispatchStatus())) {
                        fundDispatching.setDispatchStatus(EnumDispatchStatus.INIT.getCode())
                                .setRetryCount(fundDispatching.getRetryCount() + 1)
                                .setUpdatedAt(null);
                        retryFundDispatchingEntityList.add(fundDispatching);
                    }
                }
                if (CollUtil.isEmpty(retryFundDispatchingEntityList)) {
                    throw new SystemRuntimeException("异常数据，请联系管理员！");
                }
                fundDispatchingService.retryFundDispatchStatus(retryFundDispatchingEntityList);
                fundDispatchingService.executeFundDispatch(retryFundDispatchingEntityList, XBB_USER_CONTEXT.get().getUserId());
            }

        } catch (Exception e) {
            log.info("breakpointRetry失败", e);
        } finally {
            redisLockService.unlock(redisLockKey);
        }

    }

    /**
     * 校验批次是否允许重试
     *
     * @param salaryBatchEntity
     */
    private void checkBreakpointRetry(SalaryBatchEntity salaryBatchEntity) {
        SalaryMerchantInfoEntity salaryMerchantInfo = salaryMerchantInfoRepository.findById(salaryBatchEntity.getTenantId().longValue());
        //商户信息取之前发薪批次里保存的json
        SalaryMerchantInfoEntity oldSalaryMerchantInfo = XbbJsonUtil.fromJsonString(salaryBatchEntity.getExpandJson(), SalaryMerchantInfoEntity.class);

        if (ObjectUtil.equals(oldSalaryMerchantInfo.getPayeeSubAccountId(), salaryMerchantInfo.getPayeeSubAccountId())
                || ObjectUtil.equals(oldSalaryMerchantInfo.getPayerSubjectId(), salaryMerchantInfo.getPayerSubjectId())
                || ObjectUtil.equals(oldSalaryMerchantInfo.getPayeeSubjectId(), salaryMerchantInfo.getPayeeSubjectId())
        ) {
            throw new SystemRuntimeException("主体已变更，请联系管理员迁移账户后重新发起");
        }
    }

    public void fundBack(List<Long> detailIdsList) {
        Set<Long> idSet = new HashSet<>(detailIdsList);
        for (Long detailId : idSet) {
            String redisLockKey = "CXY_FUND_BACK::" + detailId;
            try {
                //校验重试
                if (!redisLockService.tryLock(redisLockKey, TimeUnit.SECONDS, 1, 5)) {
                    throw GrpcException.runtimeException(Status.ALREADY_EXISTS, "重试正在处理中,请勿重复发起");
                }
                rocketMsgService.xcy2xbbBackDetailTag(new Xcy2XbbBackDetailMessage().setDetailId(detailId));
                fundBack(detailId);
            } catch (Exception e) {
                log.info("fundBack 失败 detailId = " + detailId, e);
                throw e;
            } finally {
                redisLockService.unlock(redisLockKey);
            }
        }
    }

    /**
     * 资金撤回
     *
     * @param detailId
     */
    public void fundBack(Long detailId) {
        SalaryDetailEntity salaryDetailEntity = salaryDetailRepository.findById(detailId, SalaryDetailEntity.class, CommonConst.ZERO);
        if (salaryDetailEntity != null) {
            if (SalaryServerProto.EnumSalaryDetailStatus.WAIT_WITHDRAW_VALUE != salaryDetailEntity.getStatus()) {
                throw new SystemRuntimeException("该详情状态[" + SalaryServerProto.EnumSalaryDetailStatus.forNumber(salaryDetailEntity.getStatus()) + "]不允许进行撤回动作");
            } else {
                //更新详情状态
                SalaryDetailEntity updateSalaryDetailEntity = new SalaryDetailEntity();
                updateSalaryDetailEntity.setUpdatedAt(null);
                updateSalaryDetailEntity.setStatus(SalaryServerProto.EnumSalaryDetailStatus.CANCELING.getNumber());
                salaryDetailRepository.updateById(detailId, updateSalaryDetailEntity);

                SalaryBatchEntity salaryBatchEntity = salaryBatchRepository.findById(salaryDetailEntity.getBatchId(), SalaryBatchEntity.class, CommonConst.ZERO);
                //商户信息取之前发薪批次里保存的json
                SalaryMerchantInfoEntity salaryMerchantInfoEntity = XbbJsonUtil.fromJsonString(salaryBatchEntity.getExpandJson(), SalaryMerchantInfoEntity.class);
                fundDispatchingService.fundDispatchingBack(salaryMerchantInfoEntity, "BACK" + CommonConst.UNDERLINE + detailId,
                        new BigDecimal(salaryDetailEntity.getServiceAmount()),
                        new BigDecimal(salaryDetailEntity.getTaxAmount()),
                        new BigDecimal(salaryDetailEntity.getPaidInAmount()),
                        XBB_USER_CONTEXT.get().getUserId()
                );
            }
        } else {
            throw new SystemRuntimeException("未找到该条详情数据！");
        }

    }

    public PagePojoList<SalaryMerchantInfoEntity> queryMerchantPage(SalaryServerProto.MerchantInfoPageRequest request) {
        SalaryMerchantInfoPageParam salaryMerchantInfoPageParam = SalaryConvert.request2Param(request);
        return salaryMerchantInfoRepository.queryPage(salaryMerchantInfoPageParam);
    }
}
