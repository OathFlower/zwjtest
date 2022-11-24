package cn.xunhou.xbbcloud.rpc.sign.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.core.json.XbbProtoJsonUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.framework.plugins.file.IFileOperator;
import cn.xunhou.cloud.framework.plugins.file.dto.FileUpload4StreamDto;
import cn.xunhou.cloud.framework.util.DesPlus;
import cn.xunhou.grpc.proto.crm.CrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.subject.SubjectServiceGrpc;
import cn.xunhou.grpc.proto.subject.SubjectServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.SignServerProto;
import cn.xunhou.xbbcloud.common.bean.TemplateBean;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.common.utils.BestSignApi;
import cn.xunhou.xbbcloud.common.utils.MD5Util;
import cn.xunhou.xbbcloud.rpc.sign.dao.*;
import cn.xunhou.xbbcloud.rpc.sign.entity.*;
import cn.xunhou.xbbcloud.rpc.sign.pojo.SignConvert;
import cn.xunhou.xbbcloud.rpc.sign.pojo.param.ContractPageParam;
import cn.xunhou.xbbcloud.rpc.sign.pojo.param.QueryPositionTemplateParam;
import cn.xunhou.xbbcloud.rpc.sign.pojo.param.QueryPositionUserParam;
import cn.xunhou.xbbcloud.rpc.sign.pojo.result.ContractResult;
import cn.xunhou.xbbcloud.rpc.sign.pojo.result.PositionQrcodeResult;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.protobuf.Empty;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 签约云业务处理层
 */
@Slf4j
@Service
public class SignService {
    @Resource
    private PositionQrcodeRepository positionQrcodeRepository;
    @Resource
    private PositionContractTemplateRepository positionContractTemplateRepository;
    @Resource
    private PositionQrcodeUserRepository positionQrcodeUserRepository;

    @Resource
    private ContractRepository contractRepository;
    @Resource
    private SignInfoRepository signInfoRepository;
    @GrpcClient("ins-xhcrm-platform")
    private CrmServiceGrpc.CrmServiceBlockingStub crmServiceBlockingStub;
    @Resource
    private SignRelationProjectRepository signRelationProjectRepository;
    @Setter(onMethod = @__(@GrpcClient("ins-xhportal-platform")))
    private HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;

    @GrpcClient("ins-xhportal-platform")
    private static SubjectServiceGrpc.SubjectServiceBlockingStub subjectServiceBlockingStub;
    @Autowired
    private IFileOperator fileOperator;


    @Transactional(rollbackFor = Exception.class)
    public SignServerProto.PositionQrcodeSaveResponse saveOrUpdateQrcode(SignServerProto.PositionQrcodeSaveRequest request) {
        log.info("saveOrUpdateQrcode请求参数：" + XbbProtoJsonUtil.toJsonString(request));
        long returnId = 0L;
        PositionQrcodeEntity entity = SignConvert.request2Entity(request);
        if (request.hasId()) {
            PositionQrcodeEntity findByIdEntity = positionQrcodeRepository.findById(request.getId(), PositionQrcodeEntity.class);
            if (findByIdEntity != null) {
                positionQrcodeRepository.updateById(request.getId(), entity);
                returnId = request.getId();

                //对于关联表 如果是删除标识则直接逻辑删除   否则先逻辑删后插
                if (request.hasDeletedFlag() && request.getDeletedFlag() == 1) {
                    positionContractTemplateRepository.updateByQrCodeId(request.getId());
                    positionQrcodeUserRepository.updateByQrCodeId(request.getId());
                } else {

                    positionContractTemplateRepository.updateByQrCodeId(request.getId());
                    savePositionContract(request, returnId);

                    positionQrcodeUserRepository.updateByQrCodeId(request.getId());
                    savePositionUser(request, returnId);
                }

            } else {
                throw GrpcException.asRuntimeException("编辑时未找到对应的岗位二维码");
            }
        } else {
            returnId = positionQrcodeRepository.insert(entity).longValue();
            savePositionContract(request, returnId);
            savePositionUser(request, returnId);
        }
        return SignServerProto.PositionQrcodeSaveResponse.newBuilder().setId(returnId).build();
    }

    public void savePositionContract(SignServerProto.PositionQrcodeSaveRequest request, Long qrcodeId) {
        if (CollectionUtils.isNotEmpty(request.getContractTemplateList())) {
            List<PositionContractTemplateEntity> positionContractTemplateEntityList = new ArrayList<>();

            for (SignServerProto.ContractTemplate templateRequest :
                    request.getContractTemplateList()) {
                PositionContractTemplateEntity positionContractTemplateEntity = new PositionContractTemplateEntity();
                positionContractTemplateEntity.setPositionQrcodeId(qrcodeId);
                positionContractTemplateEntity.setContractTemplateId(templateRequest.getTemplateId());
                positionContractTemplateEntity.setType(templateRequest.getType());
                positionContractTemplateEntityList.add(positionContractTemplateEntity);
            }
            positionContractTemplateRepository.batchInsert(positionContractTemplateEntityList);
        }
    }

    public void savePositionUser(SignServerProto.PositionQrcodeSaveRequest request, Long qrcodeId) {
        if (CollectionUtils.isNotEmpty(request.getTelList())) {
            List<PositionQrcodeUserEntity> positionQrcodeUserEntityList = new ArrayList<>();
            for (String tel : request.getTelList()) {
                PositionQrcodeUserEntity positionQrcodeUserEntity = new PositionQrcodeUserEntity();
                //手机号加密
                positionQrcodeUserEntity.setTel(DesPlus.getInstance().encrypt(tel));
                positionQrcodeUserEntity.setPositionQrcodeId(qrcodeId);
                positionQrcodeUserEntityList.add(positionQrcodeUserEntity);
            }

            positionQrcodeUserRepository.batchInsert(positionQrcodeUserEntityList);
        }
    }

    /**
     * 保存签约云基本信息
     * @param request 参数
     * @return 最新结果
     */
    public SignServerProto.SignInfoResponse saveSignInfo(SignServerProto.SignInfoRequest request) {
        SignInfoEntity signInfoEntity = SignConvert.request2Entity(request);
        List<SignRelationProjectEntity> relationProjectEntityList = Lists.newArrayList();
        for (Long projectId : request.getProjectIdsList()) {
            SignRelationProjectEntity signRelationProject = new SignRelationProjectEntity();
            signRelationProject.setProjectId(projectId);
            signRelationProject.setSignInfoId(signInfoEntity.getId());
            signRelationProject.setOperatorId(request.getOperatorId());
            relationProjectEntityList.add(signRelationProject);
        }
        signInfoRepository.save(signInfoEntity, relationProjectEntityList);
        //岗位ids
        positionQrcodeRepository.updateNotInPositionIds(request.getPositionIdsList(), request.getTenantId());
        return signInfo(SignServerProto.SignInfoIdRequest.newBuilder().setTenantId(request.getTenantId()).build());
    }

    /**
     * 签约云基本信息
     * @param request 租户id
     * @return 最新结果
     */
    public SignServerProto.SignInfoResponse signInfo(SignServerProto.SignInfoIdRequest request) {
        SignInfoEntity signInfoEntity = signInfoRepository.findById(request.getTenantId());
        SignServerProto.SignInfoResponse.Builder builder = SignConvert.entity2Response(signInfoEntity);
        if (signInfoEntity == null) {
            return builder.build();
        }
        List<SignRelationProjectEntity> relationProjectEntityList = signRelationProjectRepository.findProjectInfoBySignId(request.getTenantId());
        builder.addAllProjectIds(relationProjectEntityList.stream().map(SignRelationProjectEntity::getProjectId).collect(Collectors.toList()));
        return builder.build();
    }

    @Transactional(rollbackFor = Exception.class)
    public SignServerProto.BatchInsertContractResponse batchInsertContract(SignServerProto.BatchInsertContractRequest request) {
        log.info("batchInsertContract请求参数：" + XbbProtoJsonUtil.toJsonString(request));
        SignServerProto.BatchInsertContractResponse.Builder build = SignServerProto.BatchInsertContractResponse.newBuilder();
        List<ContractEntity> contractEntityList = new ArrayList<>();
        for (SignServerProto.InsertContractRequest insertContractRequest :
                request.getInsertContractRequestList()) {

            ContractEntity contractEntity = SignConvert.request2Entity(insertContractRequest);
            contractEntityList.add(contractEntity);
        }

        List<Number> numbers = contractRepository.batchInsert(contractEntityList);
        List<Long> ids = new ArrayList<>();
        for (Number number :
                numbers) {
            Long id = number.longValue();
            ids.add(id);
        }
        return build.addAllIdList(ids).build();

    }

    public PagePojoList<ContractResult> contractList(SignServerProto.ContractListRequest request) {
        PagePojoList<ContractResult> contractResultPagePojoList = contractRepository.contractList(SignConvert.request2Param(request));
        for (ContractResult contractResult :
                contractResultPagePojoList.getData()) {
            contractResult.setIdCardNo(DesPlus.getInstance().decrypt(contractResult.getIdCardNo()));
        }
        return contractResultPagePojoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public Empty batchUpdateContract(SignServerProto.BatchUpdateContractRequest request) {


        List<SignServerProto.UpdateContractRequest> updateContractRequestList = request.getUpdateContractRequestList();


        List<Long> contractRequestIds = updateContractRequestList.stream().map(SignServerProto.UpdateContractRequest::getId).collect(Collectors.toList());
        ContractPageParam contractPageParam = new ContractPageParam();
        contractPageParam.setPageSize(0);
        contractPageParam.setIds(contractRequestIds);
        PagePojoList<ContractResult> contractResultPagePojoList = contractRepository.contractList(contractPageParam);
        if (contractResultPagePojoList == null || CollectionUtils.isEmpty(contractResultPagePojoList.getData())) {
            throw GrpcException.asRuntimeException("未找到对应的合同");
        }

        Map<Long, ContractResult> contractResultMap = contractResultPagePojoList.getData().stream().collect(Collectors.toMap(ContractResult::getId, Function.identity()));

        for (SignServerProto.UpdateContractRequest updateContractRequest : updateContractRequestList) {
            ContractEntity contractEntity = new ContractEntity();
            ContractResult contractResult = contractResultMap.get(updateContractRequest.getId());
            if (updateContractRequest.hasTemplateJson() && contractResult != null) {
                TemplateBean targetTemplateBean = JSONUtil.toBean(contractResult.getTemplateJson(), TemplateBean.class);
                TemplateBean sourceTemplateBean = JSONUtil.toBean(updateContractRequest.getTemplateJson(), TemplateBean.class);
                BeanUtil.copyProperties(sourceTemplateBean, targetTemplateBean, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
                contractEntity.setTemplateJson(JSONUtil.toJsonStr(targetTemplateBean));
            }
            if (updateContractRequest.hasDeletedFlag()) {
                contractEntity.setDeletedFlag(updateContractRequest.getDeletedFlag());
            }
            if (updateContractRequest.hasSignDate()) {
                contractEntity.setSignDate(new Timestamp(updateContractRequest.getSignDate()));
            }
            if (updateContractRequest.hasStartTime()) {
                contractEntity.setStartTime(new Timestamp(updateContractRequest.getStartTime()));
            }
            if (updateContractRequest.hasEndTime()) {
                contractEntity.setEndTime(new Timestamp(updateContractRequest.getEndTime()));
            }
            if (updateContractRequest.hasContractOssId()) {
                contractEntity.setContractOssId(updateContractRequest.getContractOssId());
            }
            if (updateContractRequest.hasStatus()) {
                contractEntity.setStatus(updateContractRequest.getStatus());
            }
            if (updateContractRequest.hasEmployeeId()) {
                contractEntity.setEmployeeId(updateContractRequest.getEmployeeId());
            }
            contractEntity.setUpdatedAt(null);
            contractRepository.updateById(contractResult.getId(), contractEntity);
        }
        return Empty.newBuilder().build();
    }

    /**
     * 合同上传到oss
     *
     * @param serviceBusinessId
     * @return
     * @throws Exception
     */
    public String saveContractPdfToOss(String serviceBusinessId) throws Exception {

        byte[] pdfBytes = null;
        pdfBytes = BestSignApi.contractDownload(serviceBusinessId);
        InputStream sbs = new ByteArrayInputStream(pdfBytes);

        try {
            FileUpload4StreamDto streamDto = new FileUpload4StreamDto();
            streamDto.setFileName(serviceBusinessId + ".pdf");
            streamDto.setContentType("application/pdf");
            streamDto.setWithDownloadContentDisposition(false);
            streamDto.setFileContent(sbs);
            return fileOperator.uploadPublicFileOrImage4Stream(streamDto);


        } catch (Exception e) {
            throw new Exception("文件上传失败");
        } finally {
            IOUtils.closeQuietly(sbs);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void contractSignComplete(SignServerProto.ContractSignCompleteRequest request) throws Exception {
        ContractEntity contractEntity = contractRepository.findById(request.getContractId(), ContractEntity.class);
        if (null == contractEntity) {
            throw GrpcException.asRuntimeException("未找到合同");
        }
        if (SignServerProto.EnumContractStatus.WAIT_SIGN_VALUE != contractEntity.getStatus()) {
            log.info("contractSignComplete ---> 当前合同状态不可提交，contractId = " + request.getContractId());
            throw GrpcException.asRuntimeException("当前合同状态不可提交");
        }
        TemplateBean templateBean = JSONUtil.toBean(contractEntity.getTemplateJson(), TemplateBean.class);
        if (null == templateBean) {
            log.info("contractSignComplete ---> 未找到合同签署信息，contractId = " + request.getContractId());
            throw GrpcException.asRuntimeException("未找到合同签署信息");
        }
        
        SubjectServiceProto.SubjectDetailBeResponse subjectDetailBeResponse = subjectServiceBlockingStub
                .getSubjectObjectById(SubjectServiceProto.IdBeRequest.newBuilder().setId(contractEntity.getSubjectId()).build());
        BestSignApi.sealAcrossPage(contractEntity.getServiceBusinessId(), subjectDetailBeResponse.getBestSignEnterpriseAccount());

        try {
            BestSignApi.signContractByTemplate(contractEntity.getServiceBusinessId(), subjectDetailBeResponse.getBestSignEnterpriseAccount(), request.getStampVarsList(), request.getTemplateNo());

        } catch (Exception e) {
            if (e.getMessage().contains("bestSignErr_" + "241423")) {
                //合同完成时返回
                throw GrpcException.asRuntimeException("合同已经签署完成，请刷新列表后重试");
            } else {
                throw GrpcException.asRuntimeException(e.getMessage());
            }
        }
        //完成签署
        BestSignApi.finishContract(contractEntity.getServiceBusinessId());


    }

    /**
     * 完成签署操作
     */
    public void completeContract(String idCardNo, String realName, String tel, ContractEntity contractEntity) throws Exception {


       /* ContractPageParam contractPageParam = new ContractPageParam();
        contractPageParam.setPageSize(0);//不分页
        contractPageParam.setIdCardNo(idCardNo);
        contractPageParam.setType(SignServerProto.EnumTemplateType.CONTRACT_VALUE);
        contractPageParam.setStatus(SignServerProto.EnumContractStatus.EFFECTING_VALUE);

        PagePojoList<ContractResult> contractResultPagePojoList = contractRepository.contractList(contractPageParam);
        List<ContractResult> contractResultList = contractResultPagePojoList.getData();
        if (CollectionUtils.isNotEmpty(contractResultList)) {
            throw GrpcException.asRuntimeException("已经存在生效中的合同，不可签署");
        }*/


       /* if (contractEntity.getType() == SignServerProto.EnumTemplateType.CONTRACT_VALUE) {

            HrmServiceProto.SaveOrUpdateEmployeeListRequest.Builder saveOrUpdateEmployeeListRequest = HrmServiceProto.SaveOrUpdateEmployeeListRequest.newBuilder();
            HrmServiceProto.SaveOrUpdateEmployeeRequest.Builder saveOrUpdateEmployeeRequest = HrmServiceProto.SaveOrUpdateEmployeeRequest.newBuilder();

            saveOrUpdateEmployeeRequest.setName(realName);
            saveOrUpdateEmployeeRequest.setMobile(tel);
            saveOrUpdateEmployeeRequest.setIdCardType(1);
            saveOrUpdateEmployeeRequest.setIdCard(idCardNo);


            HrmServiceProto.EmployeePageBeRequest.Builder builderIdCard = HrmServiceProto.EmployeePageBeRequest.newBuilder();
            builderIdCard.setIdCard(idCardNo);
            HrmServiceProto.EmployeePageResponses employeePageListForIdCard = hrmServiceBlockingStub.findEmployeePageList(builderIdCard.build());
            if (CollectionUtils.isNotEmpty(employeePageListForIdCard.getDataList())) {
                throw GrpcException.asRuntimeException("已有员工，请勿重复入职");
            }
            String personNumber = RedisUtil.generateNos("xcy", 1).get(0);
            //查重
            HrmServiceProto.EmployeePageBeRequest.Builder builderPersonNum = HrmServiceProto.EmployeePageBeRequest.newBuilder();
            builderPersonNum.addAllPersonNumber(Arrays.asList(personNumber));
            HrmServiceProto.EmployeePageResponses employeePageListForPersonNum = hrmServiceBlockingStub.findEmployeePageList(builderPersonNum.build());
            if (CollectionUtils.isNotEmpty(employeePageListForPersonNum.getDataList())) {
                throw GrpcException.asRuntimeException("员工编号生成重复，请重新尝试");
            }
            saveOrUpdateEmployeeRequest.setPersonNumber(personNumber);
            saveOrUpdateEmployeeRequest.setJobType(4);//其他
            saveOrUpdateEmployeeListRequest.addData(saveOrUpdateEmployeeRequest.build());
            HrmServiceProto.SaveOrUpdateEmployeeResponse saveOrUpdateEmployeeResponse = hrmServiceBlockingStub.saveOrUpdateEmployee(saveOrUpdateEmployeeListRequest.build());
            updateContractEntity.setEmployeeId(saveOrUpdateEmployeeResponse.getEmployeeId());
        } else {

            updateContractEntity.setEmployeeId(contractResultList.get(0).getEmployeeId());
        }*/

    }

    public String savePdfToOos(ContractEntity contractEntity) throws Exception {


        String serviceBusinessId = contractEntity.getServiceBusinessId();
        byte[] pdfBytes = null;
        pdfBytes = BestSignApi.contractDownload(serviceBusinessId);
        InputStream sbs = new ByteArrayInputStream(pdfBytes);

        try {
            FileUpload4StreamDto streamDto = new FileUpload4StreamDto();
            streamDto.setFileName(serviceBusinessId);
            streamDto.setContentType("pdf");
            streamDto.setFileContent(sbs);
            return fileOperator.uploadPublicFileOrImage4Stream(streamDto);


        } catch (Exception e) {
            throw new Exception("文件上传失败");
        } finally {
            IOUtils.closeQuietly(sbs);
        }

    }



    public SignServerProto.BestSignUrlResponse getBestSignUrl(SignServerProto.GetBestSignUrlRequest request) {
        SignServerProto.BestSignUrlResponse.Builder builder = SignServerProto.BestSignUrlResponse.newBuilder();
        ContractEntity contractEntity = contractRepository.findById(request.getContractId(), ContractEntity.class);
        if (null == contractEntity) {
            throw GrpcException.asRuntimeException("未找到合同信息");
        }
        BestSignApi.registPersonByBestSign(MD5Util.MD5Encode("sign" + request.getIdCardNo()), request.getRealName(), request.getIdCardNo(), request.getTel());
        return builder.setUrl(BestSignApi.sendByTemplate(contractEntity.getServiceBusinessId(), MD5Util.MD5Encode("sign" + request.getIdCardNo())
                , request.getTemplateNo(), request.getSignVarsList(), request.getSignReturnUrl(), request.getNotifyUrl())).build();
    }

    public void initTemplate(SignServerProto.InitTemplateListRequest request) throws Exception {

        for (SignServerProto.InitTemplateRequest initTemplateRequest :
                request.getDataList()) {

            ContractEntity contractEntity = contractRepository.findById(initTemplateRequest.getContractId(), ContractEntity.class);
            TemplateBean templateBean = new TemplateBean();
            templateBean = JSONUtil.toBean(contractEntity.getTemplateJson(), TemplateBean.class);
            //配置签约时间
            String date = formatYMD(DateUtil.formatDate(new Date()));
            templateBean.setSecondPartySignDate(date);
            templateBean.setFirstPartySignDate(date);
            templateBean.setLastSignDate(date);

            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(templateBean);
            String serviceBusinessId = BestSignApi.createContractPdf(initTemplateRequest.getBestSignEnterpriseAccount(), initTemplateRequest.getTemplateNo(), jsonObject, initTemplateRequest.getName());
            contractEntity.setServiceBusinessId(serviceBusinessId);
            contractEntity.setTemplateJson(jsonObject.toJSONString());
            contractEntity.setContractOssId(savePdfToOos(contractEntity));
            contractRepository.updateById(initTemplateRequest.getContractId(), contractEntity);
        }


    }

    /**
     * 格式化日期 —— 年月日
     *
     * @param date
     * @return
     */
    public static String formatYMD(String date) {
        if (date == null || date.length() < 8) {
            return StringUtils.EMPTY;
        }
        StringBuilder dateBuilder = new StringBuilder();
        dateBuilder.append(date.substring(0, 4));
        dateBuilder.append("年");
        dateBuilder.append(date.substring(5, 7));
        dateBuilder.append("月");
        dateBuilder.append(date.substring(8, 10));
        dateBuilder.append("日");
        return dateBuilder.toString();
    }


    public PagePojoList<PositionQrcodeResult> positionQrcodeList(SignServerProto.QrcodeListQueryRequest request) {
        return positionQrcodeRepository.positionQrcodeList(SignConvert.request2Param(request));
    }

    public SignServerProto.PositionQrcodeResponse positionQrcodeDetail(SignServerProto.QrcodeDetailQueryRequest request) {
        SignServerProto.PositionQrcodeResponse.Builder builder = SignServerProto.PositionQrcodeResponse.newBuilder();
        PositionQrcodeEntity positionQrcodeEntity = positionQrcodeRepository.findById(request.getId());
        if (positionQrcodeEntity == null) {
            throw GrpcException.asRuntimeException("不存在的二维码");
        }
        builder.setId(positionQrcodeEntity.getId());
        builder.setHroPositionId(positionQrcodeEntity.getHroPositionId());
        builder.setExpireDate(DateUtil.format(positionQrcodeEntity.getExpireDate(), DatePattern.NORM_DATETIME_PATTERN));
        builder.setRemark(positionQrcodeEntity.getRemark());
        builder.setSocialInsurance(positionQrcodeEntity.getSocialInsurance());
        builder.setContractTemplateType(positionQrcodeEntity.getContractTemplateType());
        builder.setTenantId(positionQrcodeEntity.getTenantId());
        builder.setTemplateJson(positionQrcodeEntity.getTemplateJson());
        builder.setSubjectId(positionQrcodeEntity.getSubjectId());
        builder.setCreatedAt(DateUtil.format(positionQrcodeEntity.getCreatedAt(), DatePattern.NORM_DATETIME_PATTERN));
        builder.setUpdatedAt(DateUtil.format(positionQrcodeEntity.getUpdatedAt(), DatePattern.NORM_DATETIME_PATTERN));
        builder.setOperatorId(positionQrcodeEntity.getOperatorId());
        QueryPositionTemplateParam queryPositionTemplateParam = new QueryPositionTemplateParam();
        queryPositionTemplateParam.setPositionQrcodeId(request.getId());
        List<PositionContractTemplateEntity> positionContractTemplateList = positionContractTemplateRepository.list(queryPositionTemplateParam);
        if (CollectionUtils.isNotEmpty(positionContractTemplateList)) {
            for (PositionContractTemplateEntity positionContractTemplateEntity :
                    positionContractTemplateList) {
                SignServerProto.ContractTemplate.Builder templateBuild = SignServerProto.ContractTemplate.newBuilder();
                templateBuild.setTemplateId(positionContractTemplateEntity.getContractTemplateId());
                templateBuild.setType(positionContractTemplateEntity.getType());
                builder.addContractTemplate(templateBuild);
            }
        }
        QueryPositionUserParam queryPositionUserParam = new QueryPositionUserParam();
        queryPositionUserParam.setPositionQrcodeId(request.getId());
        List<PositionQrcodeUserEntity> positionQrcodeUserEntityList = positionQrcodeUserRepository.list(queryPositionUserParam);
        if (CollectionUtils.isNotEmpty(positionQrcodeUserEntityList)) {
            for (PositionQrcodeUserEntity positionQrcodeUserEntity :
                    positionQrcodeUserEntityList) {
                builder.addTel(DesPlus.getInstance().decrypt(positionQrcodeUserEntity.getTel()));
            }

        }
        return builder.build();
    }
}
