package cn.xunhou.web.xbbcloud.product.hrm.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.constant.dto.RegionDto;
import cn.xunhou.cloud.constant.utils.AreaUtil;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.exception.SystemRuntimeException;
import cn.xunhou.cloud.core.util.SystemUtil;
import cn.xunhou.cloud.framework.plugins.file.IFileOperator;
import cn.xunhou.cloud.framework.plugins.file.dto.FileUpload4StreamDto;
import cn.xunhou.grpc.proto.crm.CrmServiceGrpc;
import cn.xunhou.grpc.proto.crm.CrmServiceProto.ProjectBeResponse;
import cn.xunhou.grpc.proto.crm.CrmServiceProto.ProjectIdListBeRequest;
import cn.xunhou.grpc.proto.crm.CrmServiceProto.ProjectListBeResponse;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.portal.PortalServiceGrpc;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerGrpc;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.web.xbbcloud.common.FilterHashSet;
import cn.xunhou.web.xbbcloud.common.IThreadLocalPool;
import cn.xunhou.web.xbbcloud.config.xhrpc.XhRpcComponent;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumProject;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhR;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhRpcParam;
import cn.xunhou.web.xbbcloud.product.hrm.constant.ConstantData;
import cn.xunhou.web.xbbcloud.product.hrm.constant.enums.IdCardTypeEnum;
import cn.xunhou.web.xbbcloud.product.hrm.enums.EnumEmploymentType;
import cn.xunhou.web.xbbcloud.product.hrm.param.ImportEmployeeRowData;
import cn.xunhou.web.xbbcloud.product.hrm.param.ImportSalaryRowData;
import cn.xunhou.web.xbbcloud.product.hrm.param.StaffAddRepQueryParam;
import cn.xunhou.web.xbbcloud.product.hrm.param.StaffImportParam;
import cn.xunhou.web.xbbcloud.product.hrm.result.*;
import cn.xunhou.web.xbbcloud.product.salary.service.SalaryService;
import cn.xunhou.web.xbbcloud.util.CommonUtil;
import cn.xunhou.web.xbbcloud.util.NameUtil;
import cn.xunhou.web.xbbcloud.util.XhNumberUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.ttl.TransmittableThreadLocal;
import io.grpc.Status;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.annotation.WillClose;
import javax.annotation.WillNotClose;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author sha.li
 * @since 2022/9/15
 */
@Service
@Slf4j
public class ExcelService {
    @GrpcClient("ins-xbbcloud-platform")
    private SalaryServerGrpc.SalaryServerBlockingStub salaryServerBlockingStub;
    @GrpcClient("ins-xhportal-platform")
    private PortalServiceGrpc.PortalServiceBlockingStub portalServiceBlockingStub;

    @GrpcClient("ins-xhcrm-platform")
    private CrmServiceGrpc.CrmServiceBlockingStub crmServiceBlockingStub;
    @Autowired
    HttpServletResponse response;

    @Resource
    private XhRpcComponent xhRpcComponent;

    @Resource
    private SalaryService salaryService;
    private static final List<String> IMPORT_SALARY_HEADS = Arrays.asList("??????", "?????????", "????????????", "????????????", "???????????????????????????????????????????????????", "??????");


    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();

    @Autowired
    IFileOperator fileOperator;
    @Autowired
    HrmEmployeeService hrmEmployeeService;
    private static final List<String> IMPORT_EMPLOYEE_HEADS = Arrays.asList("??????", "??????", "??????", "????????????", "??????????????????????????????", "????????????", "????????????", "????????????", "????????????", "????????????");
    private static final Map<String, Integer> EMPLOYEE_ATTRIBUTION = MapBuilder.<String, Integer>create().put("??????", 1).put("??????", 2).put("??????", 3).build();
    private static final Map<String, Integer> SALARY_TYPES = MapBuilder.<String, Integer>create().put("?????????", 1).put("??????", 2).build();
    @Setter(onMethod = @__(@GrpcClient("ins-xhportal-platform")))
    private HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;

    /**
     * ?????????????????????????????????
     */
    @SneakyThrows(IOException.class)
    @WillNotClose
    public Workbook getImportTemplateWorkbook() {
        InputStream templateInputStream = ResourceUtil.getStream(ConstantData.EMPLOYEE_IMPORT_TEMPLATE_PATH);
        SXSSFWorkbook workbook = new SXSSFWorkbook(new XSSFWorkbook(templateInputStream));
        Sheet dataSheet = workbook.getSheetAt(0);
        String[] orgNames = getOrgNames().entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toArray(String[]::new);
        String[] employmentTypes = getEmploymentTypes().entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toArray(String[]::new);
        // String[] employmentSources = getEmploymentSources().entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toArray(String[]::new);
        String[] emptyStrArray = new String[0];
        String[] idCardTypes = IdCardTypeEnum.getNames().toArray(emptyStrArray);
        String[] salaryTypes = SALARY_TYPES.keySet().toArray(emptyStrArray);
        String[] employeeAttributions = EMPLOYEE_ATTRIBUTION.keySet().toArray(emptyStrArray);
        setComboboxInfo(dataSheet, orgNames, 3);
        setComboboxInfo(dataSheet, employmentTypes, 5);
        // setComboboxInfo(dataSheet, employmentSources, 6);
        setComboboxInfo(dataSheet, idCardTypes, 6);
        setComboboxInfo(dataSheet, salaryTypes, 9);
        setComboboxInfo(dataSheet, employeeAttributions, 10);
        return workbook;
    }

    /**
     * ???????????????????????????????????????
     */
    @SneakyThrows(IOException.class)
    @WillNotClose
    public Workbook getSalaryImportTemplateWorkbook() {
        InputStream templateInputStream = ResourceUtil.getStream(ConstantData.SALARY_IMPORT_TEMPLATE_PATH);
        SXSSFWorkbook workbook = new SXSSFWorkbook(new XSSFWorkbook(templateInputStream));
        return workbook;
    }
    
    /**
     * ????????????
     */
    @SneakyThrows(IOException.class)
    public String importEmployee(MultipartFile file) {
        ExcelRowErrorInfo errorInfo = new ExcelRowErrorInfo();
        List<ImportEmployeeRowData> importData = getAndCheckImportData(file, errorInfo);
        if (errorInfo.isNotEmpty()) {
            log.info("errorInfo={}", errorInfo);
            try (InputStream inputStream = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet dataSheet = workbook.getSheetAt(0);
                dataSheet.setColumnHidden(0, false);
                errorInfo.forEach((k, v) -> {
                    Cell cell = dataSheet.getRow(k).createCell(0);
                    cell.getCellStyle().setWrapText(true);
                    cell.setCellValue(errorInfo.formatMsg(v));
                });
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);
                FileUpload4StreamDto fileUpload4StreamDto = new FileUpload4StreamDto();
                fileUpload4StreamDto.setFileName("????????????????????????.xlsx");
                fileUpload4StreamDto.setContentType(ConstantData.XLSX_CONTENT_TYPE);
                fileUpload4StreamDto.setFileContent(IoUtil.toStream(outputStream));
                // ?????????OSS???????????????????????????
                return fileOperator.uploadPublicFileForUrl(fileUpload4StreamDto);
            }
        }
        List<HrmServiceProto.SaveOrUpdateEmployeeRequest> saveOrUpdateEmployeeRequests = ImportEmployeeRowData.toRequestList(importData);
        Integer tenantId = XBB_USER_CONTEXT.get().getTenantId();
        // ??????????????????,????????????????????????
        SalaryServerProto.MerchantInfoResponse merchantInfoResponse = salaryServerBlockingStub.queryMerchantInfo(SalaryServerProto.MerchantInfoRequest.newBuilder()
                .setTenantId(tenantId)
                .build());
        if (SalaryServerProto.EnumTenantType.BEHALF_ISSUED == merchantInfoResponse.getTenantType()) {
            log.info("importEmployee-????????????");
            importStaff(importData, merchantInfoResponse.getProjectId());
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            hrmServiceBlockingStub.withDeadlineAfter(30, TimeUnit.SECONDS).saveOrUpdateEmployee(HrmServiceProto.SaveOrUpdateEmployeeListRequest.newBuilder().addAllData(saveOrUpdateEmployeeRequests).build());
        } catch (Exception e) {
            log.error("??????????????????", e);
            throw new SystemRuntimeException("??????????????????");
        }

        // ????????????
        return null;

    }

    private void importStaff(List<ImportEmployeeRowData> importData, String projectIdStr) {
        if (ObjectUtils.isEmpty(importData)) {
            return;
        }
        // ?????????????????????
        log.info("importEmployee:{}", projectIdStr);
        if (ObjectUtils.isEmpty(projectIdStr)) {
            throw Status.UNKNOWN.withDescription("???????????????????????????????????????????????????").asRuntimeException();
        }
        long projectId = Long.parseLong(projectIdStr);
        // ???????????????????????????
        if (projectId == 0L) {
            throw Status.UNKNOWN.withDescription("???????????????????????????????????????????????????").asRuntimeException();
        }
        ProjectBeResponse project = getProject(projectId);
        if (ObjectUtils.isEmpty(project)) {
            log.info("importEmployee-project:empty");
            throw Status.UNKNOWN.withDescription("???????????????????????????????????????????????????").asRuntimeException();
        }
        CustomerResult company = getCompany(project.getProjectBasicInformation().getCustomerId());
        if (ObjectUtils.isEmpty(company)) {
            log.info("importEmployee-company:empty");
            throw Status.UNKNOWN.withDescription("???????????????????????????????????????????????????????????????").asRuntimeException();
        }
        List<ProjectPositionResult> positionDtosByProjectId = getProjectPositionDtosByProjectId(projectId);
        if (ObjectUtils.isEmpty(positionDtosByProjectId)) {
            log.info("importEmployee-jobByProjectId:empty");
            throw Status.UNKNOWN.withDescription("????????????????????????????????????????????????????????????").asRuntimeException();
        }
        ProjectPositionResult projectPositionResult = positionDtosByProjectId.stream().max(Comparator.comparing(ProjectPositionResult::getId)).get();
        // ?????????????????????????????????
        List<StaffAddRepQueryParam> idCardNumList = importData.stream().map(t -> new StaffAddRepQueryParam(t.getIdCardNum(), t.getIdCardNum())).collect(Collectors.toList());
        // ????????????
        Map<String, StaffAddRepResult> existIdCardMap = queryReptStaffs(idCardNumList);
        List<StaffImportParam> staffImportParamList = importData.stream().filter(t -> existIdCardMap.get(t.getIdCardNum()) == null || !existIdCardMap.get(t.getIdCardNum()).getRepeatIdcardNoFlag())
                .map(t -> toImportStaffDto(t, project, company, projectPositionResult))
                .collect(Collectors.toList());
        Long opId = 10153L;
        if (SystemUtil.isOnline()) {
            opId = 10003L;
        }
        for (StaffImportParam staffImportParam : staffImportParamList) {
            try {
                importBatchAddStaff(Collections.singletonList(staffImportParam), opId);
            } catch (Exception e) {
                log.info("importBatchAddStaff", e);
            }
        }
    }

    private List<ProjectPositionResult> getProjectPositionDtosByProjectId(long projectId) {
        if (projectId == 0L) {
            return new ArrayList<>();
        }
        Map<String, Object> params3 = new HashMap<>();
        params3.put("projectId", projectId);
        params3.put("tenant", "XUNHOU");

        XhRpcParam xhRpcParam = new XhRpcParam();
        xhRpcParam.setRequest(params3)
                .setServiceProject(EnumProject.HROSTAFF)
                .setUri("IProjectPositionService/getProjectPositionDtosByProjectId");
        XhR<List<ProjectPositionResult>> listXhR = xhRpcComponent.sendForList(xhRpcParam, ProjectPositionResult.class);
        if (listXhR.getStatus() != 0) {
            throw Status.INTERNAL.withDescription(listXhR.getMessage()).asRuntimeException();
        }
        return listXhR.getData();
    }


    private ProjectBeResponse getProject(long projectId) {
        if (projectId == 0L) {
            return null;
        }
        ProjectListBeResponse batchProjectById = crmServiceBlockingStub.findBatchProjectById(ProjectIdListBeRequest.newBuilder().addProjectIds(projectId).build());
        if (ObjectUtils.isEmpty(batchProjectById.getProjectListBeResponseList())) {
            log.info("getProject-batchProjectById:empty");
            return null;
        }
        return batchProjectById.getProjectListBeResponseList().get(0);
    }


    private CustomerResult getCompany(long companyId) {
        Map<String, Object> params3 = new HashMap<>();
        params3.put("customerId", companyId);
        params3.put("tenant", "XUNHOU");
        XhRpcParam xhRpcParam = new XhRpcParam();
        xhRpcParam.setRequest(params3)
                .setServiceProject(EnumProject.USERXH)
                .setUri("ICustomerService/getCustomerDtoById");
        XhR<CustomerResult> send = xhRpcComponent.send(xhRpcParam, CustomerResult.class);
        if (send.getStatus() != 0) {
            throw Status.INTERNAL.withDescription(send.getMessage()).asRuntimeException();
        }
        return send.getData();
    }

    private StaffImportParam toImportStaffDto(ImportEmployeeRowData data, ProjectBeResponse project, CustomerResult company, ProjectPositionResult projectPositionResult) {
        StaffImportParam param = new StaffImportParam();
        param.setName(data.getName());
        param.setTelephone(data.getMobile());
        param.setIdCardType(data.getIdCardTypeCode());
        param.setIdcardNo(data.getIdCardNum());
        param.setCompanyName(company.getCustomerName());
        param.setCompanyId(company.getId());
        param.setJobName(projectPositionResult.getName());
        param.setStatus(1);//??????
        param.setSource(5);// ???????????????5#SAAS
        RegionDto regionByCode = AreaUtil.getRegionByCode(projectPositionResult.getAreaCode());
        if (!ObjectUtils.isEmpty(regionByCode)) {
            param.setCityName(regionByCode.getName());
        }
        param.setPostId(projectPositionResult.getId());
        if (!ObjectUtils.isEmpty(data.getEntryDateStr())) {
            param.setOnboardDate(new DateTime(data.getEntryDateStr()).toString(DatePattern.PURE_DATETIME_PATTERN));
        } else {
            param.setOnboardDate(new DateTime().toString(DatePattern.PURE_DATETIME_PATTERN));
        }
        param.setStaffType(EnumEmploymentType.getEnumByBusinessType(project.getProjectBasicInformation().getBusinessType().getNumber()));
        return param;
    }

    private void importBatchAddStaff(List<StaffImportParam> staffImportParamList, long userXhId) {
        if (ObjectUtils.isEmpty(staffImportParamList)) {
            log.info("importBatchAddStaff-empty");
            return;
        }
        Map<String, Object> params3 = new HashMap<>();
        params3.put("staffImportAddDtos", staffImportParamList);
        params3.put("operatorId", userXhId);

        XhRpcParam xhRpcParam = new XhRpcParam();
        xhRpcParam.setRequest(params3)
                .setClientId(EnumProject.STARPRO_WEB.getClientId())
                .setServiceProject(EnumProject.HROSTAFF)
                .setUri("IStaffService/importBatchAddStaff");
        XhR<Map> xhR = xhRpcComponent.send(xhRpcParam, Map.class);
        if (xhR.getStatus() != 0) {
            throw Status.INTERNAL.withDescription(xhR.getMessage()).asRuntimeException();
        }
    }

    private Map<String ,StaffAddRepResult> queryReptStaffs(List<StaffAddRepQueryParam> idCardNumList) {
        if (ObjectUtils.isEmpty(idCardNumList)) {
            return new HashMap<>();
        }
        Map<String, Object> params3 = new HashMap<>();
        params3.put("staffAddRepQueryDtos", idCardNumList);

        XhRpcParam xhRpcParam = new XhRpcParam();
        xhRpcParam.setRequest(params3)
                .setServiceProject(EnumProject.HROSTAFF)
                .setUri("IStaffService/queryReptStaffs");
        XhR<Map> xhR = xhRpcComponent.send(xhRpcParam, Map.class);
        if (xhR.getStatus() != 0) {
            throw Status.INTERNAL.withDescription(xhR.getMessage()).asRuntimeException();
        }
        if (ObjectUtils.isEmpty(xhR.getData())) {
            return xhR.getData();
        }
        Map<String, Object> data = xhR.getData();
        Map<String, StaffAddRepResult> xhRMap = new HashMap<>();
        for (Map.Entry<String, Object> temp : data.entrySet()) {
            xhRMap.put(temp.getKey(), JSONUtil.toBean(JSONUtil.toJsonStr(temp.getValue()),StaffAddRepResult.class));
        }
        return xhRMap;
    }

    private static final String END_FRONT_STATIC_RESOURCE_URL_PREFIX;

    static {
        if (SystemUtil.isOffline()) {
            END_FRONT_STATIC_RESOURCE_URL_PREFIX = "https://qa-static.xunhou.cn/";
        } else {
            END_FRONT_STATIC_RESOURCE_URL_PREFIX = "https://static.xunhou.cn/";
        }
    }

    /**
     * ???????????????
     */
    @SneakyThrows(IOException.class)
    public ImportSalaryResult<ImportSalaryRowData> importSalary(MultipartFile file) {
        ImportSalaryResult<ImportSalaryRowData> importResult = new ImportSalaryResult();
        ExcelRowErrorInfo errorInfo = new ExcelRowErrorInfo();
        SalaryServerProto.MerchantInfoRequest.Builder merchantInfoRequest = SalaryServerProto.MerchantInfoRequest.newBuilder();
        merchantInfoRequest.setTenantId(XBB_USER_CONTEXT.tenantId());
        SalaryServerProto.MerchantInfoResponse merchantInfoResponse = salaryServerBlockingStub.queryMerchantInfo(merchantInfoRequest.build());
        List<ImportSalaryRowData> importData = getAndCheckImportDataSalary(file, errorInfo, merchantInfoResponse);
        InputStream inputStream = file.getInputStream();
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (errorInfo.isNotEmpty()) {
            log.info("errorInfo={}", errorInfo);

            Sheet dataSheet = workbook.getSheetAt(0);
            dataSheet.setColumnHidden(0, false);
            try {
                errorInfo.forEach((k, v) -> {
                    Cell cell = dataSheet.getRow(k).createCell(0);
                    cell.getCellStyle().setWrapText(true);
                    cell.setCellValue(errorInfo.formatMsg(v));
                });

            } catch (Exception e) {
                throw new SystemRuntimeException("?????????????????????");
            }


            workbook.write(outputStream);
            FileUpload4StreamDto fileUpload4StreamDto = new FileUpload4StreamDto();
            fileUpload4StreamDto.setFileName("errorInfo.xlsx");
            fileUpload4StreamDto.setContentType(ConstantData.XLSX_CONTENT_TYPE);
            fileUpload4StreamDto.setWithDownloadContentDisposition(true);
            fileUpload4StreamDto.setFileContent(IoUtil.toStream(outputStream));
            String fileId = fileOperator.uploadPublicFileOrImage4Stream(fileUpload4StreamDto);
            // ?????????OSS???????????????????????????
            importResult.setSuccess(false);
            importResult.setFileId(fileId);
            importResult.setFileUrl(END_FRONT_STATIC_RESOURCE_URL_PREFIX + fileId);
            return importResult;

        } else {
            workbook.write(outputStream);
            FileUpload4StreamDto fileUpload4StreamDto = new FileUpload4StreamDto();
            fileUpload4StreamDto.setFileName("success.xlsx");
            fileUpload4StreamDto.setContentType(ConstantData.XLSX_CONTENT_TYPE);
            fileUpload4StreamDto.setWithDownloadContentDisposition(true);
            fileUpload4StreamDto.setFileContent(IoUtil.toStream(outputStream));
            String fileId = fileOperator.uploadPublicFileOrImage4Stream(fileUpload4StreamDto);
            BigDecimal countFee = new BigDecimal("0.00");
            for (ImportSalaryRowData importSalaryRowData :
                    importData) {
                //??????????????????
                BigDecimal bigServiceAmount = NumberUtil.mul(importSalaryRowData.getPaidAbleAmountBig(), merchantInfoResponse.getServiceRate());
                DecimalFormat formatDecimal = new DecimalFormat("0");
                formatDecimal.setRoundingMode(RoundingMode.HALF_UP);
                Integer serviceAmount = Integer.valueOf(formatDecimal.format(bigServiceAmount));

                countFee = NumberUtil.add(countFee, importSalaryRowData.getPaidAbleAmountBig(), serviceAmount);
            }


            importResult.setNumCount(XhNumberUtils.millimeterFormat(NumberUtil.parseNumber(countFee.toString()), 2));
            importResult.setChinaCount(XhNumberUtils.digitToChinese(NumberUtil.parseNumber(countFee.toString())));
            importResult.setSuccess(true);
            importResult.setFileId(fileId);
            importResult.setFileUrl(END_FRONT_STATIC_RESOURCE_URL_PREFIX + fileId);
        }
        // ????????????
        return importResult;
    }

    /**
     * ????????????cell??????????????????????????????sheet??????
     * ????????????????????????????????????
     *
     * @param sheet   ??????????????????sheet
     * @param data    ??????????????????
     * @param cellNum ?????????????????????
     */
    private void setComboboxInfo(Sheet sheet, String[] data, int cellNum) {
        Workbook workbook = sheet.getWorkbook();
        String sheetName = "comboboxData" + (cellNum + 1);
        //1.???????????????sheet????????????????????????
        Sheet comboboxDataSheet = workbook.createSheet(sheetName);
        //2.????????????
        for (int i = 0; i < data.length; i++) {
            comboboxDataSheet.createRow(i).createCell(0).setCellValue(data[i]);
        }
        String listFormula = sheetName + "!A$1:A$" + data.length;
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createFormulaListConstraint(listFormula);
        CellRangeAddressList addressList = new CellRangeAddressList(1, workbook.getSpreadsheetVersion().getMaxRows() - 1, cellNum, cellNum);
        DataValidation dataValidation = helper.createValidation(constraint, addressList);
        // ?????????????????????????????????
        dataValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        dataValidation.setShowErrorBox(true);
        dataValidation.setSuppressDropDownArrow(true);
        dataValidation.createErrorBox("??????", "?????????????????????????????????");
        sheet.addValidationData(dataValidation);
        // ??????sheet??????
        workbook.setSheetHidden(workbook.getSheetIndex(comboboxDataSheet), true);
        comboboxDataSheet.protectSheet(RandomUtil.randomString(8));
    }

    /**
     * ??????????????????????????????
     */
    private List<ImportEmployeeRowData> getAndCheckImportData(MultipartFile file, ExcelRowErrorInfo errorInfo) throws IOException {
        List<ImportEmployeeRowData> importData = new ArrayList<>();
        // ????????????
        String fileName = file.getOriginalFilename();
        log.info("fileName={}", fileName);
        try (InputStream inputStream = file.getInputStream()) {
            long fileSize = file.getSize();
            if (fileSize == 0) {
                // ??????????????????
                throw new SystemRuntimeException("??????????????????");
            }
            // ?????????????????? 10M??????
            if (fileSize > ConstantData.ONE_MB_BYTES * 10) {
                throw new SystemRuntimeException("????????????");
            }
            try (ExcelReader excelReader = EasyExcel.read(inputStream).charset(UTF_8).build()) {
                ExcelTypeEnum excelType = excelReader.analysisContext().readWorkbookHolder().getExcelType();
                if (excelType != ExcelTypeEnum.XLSX) {
                    // ???????????? .xlsx
                    throw new SystemRuntimeException("?????????????????????????????????excel2007??????????????????(.xlsx)");
                }
                // ????????????????????????sheet
                ReadSheet readSheet = EasyExcel.readSheet(0).head(ImportEmployeeRowData.class).registerReadListener(new AnalysisEventListener<ImportEmployeeRowData>() {
                    @Override
                    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                        checkImportEmployeeSheetHead(headMap, context);
                        super.invokeHeadMap(headMap, context);
                    }

                    @Override
                    public void invoke(ImportEmployeeRowData data, AnalysisContext context) {
                        importData.add(data);
                    }

                    @Override
                    public void doAfterAllAnalysed(AnalysisContext context) {
                        log.info("allAnalysed");
                    }
                }).build();
                excelReader.read(readSheet);
                excelReader.finish();
            }
        }

        // ??????????????????????????????????????????
        Filter<String> stringFilter = StrUtil::isNotBlank;
        Set<String> employeeNums = new FilterHashSet<>(stringFilter);
        Set<String> mobiles = new FilterHashSet<>(stringFilter);
        Set<String> idCarNums = new FilterHashSet<>(stringFilter);
        Set<String> names = new FilterHashSet<>(stringFilter);
        importData.forEach(e -> {
            employeeNums.add(e.getEmployeeNum());
            mobiles.add(e.getMobile());
            idCarNums.add(e.getIdCardNum());
            names.add(e.getName());
        });
        Integer tenantId = XBB_USER_CONTEXT.tenantId();
        CompletableFuture<Map<String, HrmServiceProto.EmployeePageResponse>> map1Feature = CompletableFuture.supplyAsync(() -> {
            List<HrmServiceProto.EmployeePageResponse> list1 =
                    hrmServiceBlockingStub.withDeadlineAfter(20, TimeUnit.SECONDS).findEmployeePageList(HrmServiceProto.EmployeePageBeRequest.newBuilder()
                            .setType(1).setTenantId(tenantId).addAllPersonNumber(employeeNums).build()).getDataList();
            return CollUtil.toMap(list1, new HashMap<>(32), HrmServiceProto.EmployeePageResponse::getPersonNumber);
        });
        CompletableFuture<Map<String, HrmServiceProto.EmployeePageResponse>> map2Feature = CompletableFuture.supplyAsync(() -> {
            List<HrmServiceProto.EmployeePageResponse> list2 =
                    hrmServiceBlockingStub.withDeadlineAfter(20, TimeUnit.SECONDS).findEmployeePageList(HrmServiceProto.EmployeePageBeRequest.newBuilder()
                            .setType(1).setTenantId(tenantId).addAllMobiles(mobiles).addAllStatus(Arrays.asList(1, 2, 3)).build()).getDataList();
            return CollUtil.toMap(list2, new HashMap<>(32), HrmServiceProto.EmployeePageResponse::getMobile);
        });
        CompletableFuture<Map<String, HrmServiceProto.EmployeePageResponse>> map3Feature = CompletableFuture.supplyAsync(() -> {
            List<HrmServiceProto.EmployeePageResponse> list3 =
                    hrmServiceBlockingStub.withDeadlineAfter(20, TimeUnit.SECONDS).findEmployeePageList(HrmServiceProto.EmployeePageBeRequest.newBuilder()
                            .setType(1).setTenantId(tenantId).addAllIdCards(idCarNums).build()).getDataList();
            return CollUtil.toMap(list3, new HashMap<>(32), HrmServiceProto.EmployeePageResponse::getIdCard);
        });
        CompletableFuture<Map<String, HrmServiceProto.EmployeePageResponse>> map4Feature = CompletableFuture.supplyAsync(() -> {
            List<HrmServiceProto.EmployeePageResponse> list4 =
                    hrmServiceBlockingStub.withDeadlineAfter(20, TimeUnit.SECONDS).findEmployeePageList(HrmServiceProto.EmployeePageBeRequest.newBuilder()
                            .setType(1).setTenantId(tenantId).addAllNames(names).addAllStatus(Arrays.asList(1, 2, 3)).build()).getDataList();
            return CollUtil.toMap(list4, new HashMap<>(32), HrmServiceProto.EmployeePageResponse::getName);
        });

        try {
            Map<String, HrmServiceProto.EmployeePageResponse> map1 = map1Feature.get();
            Map<String, HrmServiceProto.EmployeePageResponse> map2 = map2Feature.get();
            Map<String, HrmServiceProto.EmployeePageResponse> map3 = map3Feature.get();
            Map<String, HrmServiceProto.EmployeePageResponse> map4 = map4Feature.get();
            ThreadLocalPool.EMPLOYEE_NUM_MAP.set(map1);
            ThreadLocalPool.MOBILE_MAP.set(map2);
            ThreadLocalPool.ID_CARD_NUM_MAP.set(map3);
            ThreadLocalPool.NAME_MAP.set(map4);
        } catch (InterruptedException | ExecutionException e) {
            log.warn("??????????????????", e);
            throw new SystemRuntimeException("????????????,????????????");
        }

        ThreadLocalPool.EMPLOYMENT_TYPES.set(MapUtil.inverse(getEmploymentTypes()));
        ThreadLocalPool.EMPLOYMENT_SOURCES.set(MapUtil.inverse(getEmploymentSources()));
        ThreadLocalPool.ORG_NAMES.set(MapUtil.inverse(getOrgNames()));

        for (int i = 0; i < importData.size(); i++) {
            ImportEmployeeRowData data = importData.get(i);
            validImportData(data, i + 1, errorInfo);
        }
        return importData;
    }


    /**
     * ????????????????????????????????????
     */
    private List<ImportSalaryRowData> getAndCheckImportDataSalary(MultipartFile file, ExcelRowErrorInfo errorInfo, SalaryServerProto.MerchantInfoResponse merchantInfoResponse) throws IOException {
        List<ImportSalaryRowData> importData = new ArrayList<>();
        // ????????????
        String fileName = file.getOriginalFilename();
        log.info("fileName={}", fileName);
        if (fileName.lastIndexOf(".") == -1) {
            throw Status.INVALID_ARGUMENT.withDescription("?????????????????????").asRuntimeException();
        }

        String lastName = fileName.substring(fileName.lastIndexOf("."));
        if (!".xlsx".equals(lastName)) {
            throw Status.INVALID_ARGUMENT.withDescription("??????????????????????????????").asRuntimeException();
        }
        try (InputStream inputStream = file.getInputStream()) {
            long fileSize = file.getSize();
            if (fileSize == 0) {
                // ??????????????????
                throw Status.INVALID_ARGUMENT.withDescription("??????????????????").asRuntimeException();
            }
            // ?????????????????? 10M??????
            if (fileSize > ConstantData.ONE_MB_BYTES * 10) {
                throw Status.INVALID_ARGUMENT.withDescription("????????????").asRuntimeException();
            }
            try (ExcelReader excelReader = EasyExcel.read(inputStream).charset(UTF_8).build()) {
                ExcelTypeEnum excelType = excelReader.analysisContext().readWorkbookHolder().getExcelType();
                if (excelType != ExcelTypeEnum.XLSX) {
                    // ???????????? .xlsx
                    throw Status.INVALID_ARGUMENT.withDescription("?????????????????????????????????excel2007??????????????????(.xlsx)").asRuntimeException();
                }
                // ????????????????????????sheet
                ReadSheet readSheet = EasyExcel.readSheet(0).head(ImportSalaryRowData.class).registerReadListener(new AnalysisEventListener<ImportSalaryRowData>() {
                    @Override
                    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                        checkImportSalarySheetHead(headMap, context);
                        super.invokeHeadMap(headMap, context);
                    }

                    @Override
                    public void invoke(ImportSalaryRowData data, AnalysisContext context) {
                        importData.add(data);
                    }

                    @Override
                    public void doAfterAllAnalysed(AnalysisContext context) {
                        log.info("allAnalysed");
                    }
                }).build();
                excelReader.read(readSheet);
                excelReader.finish();
            }
        }

        for (int i = 0; i < importData.size(); i++) {
            validImportDataSalary(importData.get(i), i + 1, errorInfo, merchantInfoResponse);
        }
        return importData;
    }

    /**
     * ?????????????????????excel??????????????????
     */
    private void checkImportEmployeeSheetHead(Map<Integer, String> headMap, AnalysisContext context) {
        if (!"????????????".equals(headMap.get(0))) {
            throw new SystemRuntimeException("??????????????????????????????????????????????????????????????????");
        }
        Collection<String> excelHeads = headMap.values();
        if (CollUtil.isEmpty(excelHeads)) {
            log.info("excelHeads is empty");
            throw new SystemRuntimeException("??????????????????????????????????????????????????????????????????");
        }
        Collection<String> intersection = CollUtil.intersection(excelHeads, IMPORT_EMPLOYEE_HEADS);
        if (CollUtil.isEmpty(intersection)) {
            throw new SystemRuntimeException("??????????????????????????????????????????????????????????????????");
        }
        if (intersection.size() != IMPORT_EMPLOYEE_HEADS.size()) {
            Collection<String> disjunction = CollUtil.disjunction(IMPORT_EMPLOYEE_HEADS, intersection);
            throw new SystemRuntimeException("???????????????" + String.join("???", disjunction));
        }
        Integer approximateTotalRowNumber = context.readSheetHolder().getApproximateTotalRowNumber();
        log.info("approximateTotalRowNumber={}", approximateTotalRowNumber);
        --approximateTotalRowNumber;
        if (approximateTotalRowNumber > ConstantData.EMPLOYEE_IMPORT_TEMPLATE_MAX_ROW) {
            // ???????????? ???????????????1000???
            throw new SystemRuntimeException("????????????????????????1000???????????????????????????????????????");
        }
        if (approximateTotalRowNumber == 0) {
            // ????????????
            throw new SystemRuntimeException("???????????????????????????");
        }
    }

    /**
     * ?????????????????????excel??????????????????
     */
    private void checkImportSalarySheetHead(Map<Integer, String> headMap, AnalysisContext context) {
        Collection<String> excelHeads = headMap.values();
        if (CollUtil.isEmpty(excelHeads)) {
            log.info("excelHeads is empty");
            throw Status.INVALID_ARGUMENT.withDescription("?????????????????????").asRuntimeException();
        }
        Collection<String> intersection = CollUtil.intersection(excelHeads, IMPORT_SALARY_HEADS);
        if (CollUtil.isEmpty(intersection)) {
            throw Status.INVALID_ARGUMENT.withDescription("?????????????????????").asRuntimeException();
        }
        if (intersection.size() != IMPORT_SALARY_HEADS.size()) {
            Collection<String> disjunction = CollUtil.disjunction(IMPORT_SALARY_HEADS, intersection);
            throw Status.INVALID_ARGUMENT.withDescription("???????????????" + String.join("???", disjunction)).asRuntimeException();
        }
        Integer approximateTotalRowNumber = context.readSheetHolder().getApproximateTotalRowNumber();
        log.info("approximateTotalRowNumber={}", approximateTotalRowNumber);
        --approximateTotalRowNumber;
        if (approximateTotalRowNumber > ConstantData.EMPLOYEE_IMPORT_TEMPLATE_MAX_ROW) {
            // ???????????? ???????????????1000???
            throw Status.INVALID_ARGUMENT.withDescription("????????????????????????1000???????????????????????????????????????").asRuntimeException();

        }
        if (approximateTotalRowNumber == 0) {
            // ????????????
            throw Status.INVALID_ARGUMENT.withDescription("????????????????????????").asRuntimeException();
        }
    }

    /**
     * ???????????????????????????
     */
    private void validImportDataSalary(ImportSalaryRowData data, int rowIndex, ExcelRowErrorInfo errorInfo, SalaryServerProto.MerchantInfoResponse merchantInfoResponse) {
        log.info("row={} data={}", rowIndex, data);
        String name = data.getName();
        String phone = data.getPhone();
        String idCardNo = data.getIdCardNo();
        String taxAmount = data.getTaxAmount();

        String paidAbleAmount = data.getPaidAbleAmount();
        paidAbleAmount = paidAbleAmount.replaceAll(",", "");
        paidAbleAmount = paidAbleAmount.replaceAll("???", "");

        if (name == null) {
            errorInfo.add(rowIndex, "??????????????????");
        } else {
            // ??????????????? ????????? ????????????????????????
            if (!NameUtil.isEmployeeName(name)) {
                errorInfo.add(rowIndex, "?????????????????????");
            }
        }
        if (phone == null) {
            errorInfo.add(rowIndex, "?????????????????????");
        } else {
            // ???????????? ?????????????????????????????????????????????????????????
            if (!Validator.isMobile(phone)) {
                errorInfo.add(rowIndex, "????????????????????????");
            }
        }
        if (idCardNo == null) {
            errorInfo.add(rowIndex, "????????????????????????");
        } else {
            // ???????????????????????????
            if (!IdCardTypeEnum.T1.validIdCard(idCardNo)) {
                errorInfo.add(rowIndex, "???????????????????????????");
            } else {
                SalaryServerProto.QueryByIdCardsRequest.Builder request = SalaryServerProto.QueryByIdCardsRequest.newBuilder();
                List<String> idCards = new ArrayList<>();
                idCards.add(idCardNo);
                request.addAllIdCards(idCards);
                SalaryServerProto.UserXhCListResponse userXhCListResponse = salaryServerBlockingStub.queryUserXhCByIdCards(request.build());
                if (CollectionUtil.isNotEmpty(userXhCListResponse.getDataList())) {
                    //??????????????????????????????
                    if (StringUtils.isNotBlank(name) && !name.equals(userXhCListResponse.getData(0).getRealName())) {
                        errorInfo.add(rowIndex, "????????????????????????????????????");
                    }
                }
                if (SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE == merchantInfoResponse.getTenantType().getNumber()) {
                    String str = salaryService.checkUserPayerSubjectId(idCardNo, merchantInfoResponse.getPayerSubjectId());
                    if (StringUtils.isNotBlank(str)) {
                        errorInfo.add(rowIndex, str);
                    }
                }

            }
        }

        //??????????????? ???????????????
        if (!merchantInfoResponse.getIndividualTax()) {
            if (taxAmount == null) {
                errorInfo.add(rowIndex, "????????????????????????");
            } else {
                try {
                    BigDecimal bigDecimal = new BigDecimal(taxAmount.replaceAll(",", ""));
                    if (bigDecimal.signum() < 0) {
                        errorInfo.add(rowIndex, "???????????????????????????");
                    }
                    if (getNumberOfDecimalPlace(bigDecimal) > 2) {
                        errorInfo.add(rowIndex, "????????????????????????????????????");
                    }
                    data.setTaxAmountBig(bigDecimal);
                } catch (Exception e) {
                    errorInfo.add(rowIndex, "???????????????????????????");
                }

            }
        }
        if (paidAbleAmount == null) {
            errorInfo.add(rowIndex, "??????????????????????????????");
        } else {
            try {
                BigDecimal bigDecimal = new BigDecimal(paidAbleAmount.replaceAll(",", ""));
                if (bigDecimal.signum() < 0) {
                    errorInfo.add(rowIndex, "?????????????????????????????????");
                }
                if (getNumberOfDecimalPlace(bigDecimal) > 2) {
                    errorInfo.add(rowIndex, "??????????????????????????????????????????");
                }
                data.setPaidAbleAmountBig(bigDecimal);
            } catch (Exception e) {
                errorInfo.add(rowIndex, "?????????????????????????????????");
            }
        }
    }


    public static int getNumberOfDecimalPlace(BigDecimal bigDecimal) {

        final String s = bigDecimal.toPlainString();

        final int index = s.indexOf('.');
        if (index < 0) {
            return 0;
        }
        return s.length() - 1 - index;
    }

    /**
     * ???????????????????????????
     */
    private void validImportData(ImportEmployeeRowData data, int rowIndex, ExcelRowErrorInfo errorInfo) {
        log.info("row={} data={}", rowIndex, data);
        String employeeNum = data.getEmployeeNum();
        String name = data.getName();
        String orgName = data.getOrgName();
        String mobile = data.getMobile();
        String employmentType = data.getEmploymentType();
        // String employmentSource = data.getEmploymentSource();
        String idCardType = data.getIdCardType();
        String idCardNum = data.getIdCardNum();
        String entryDateStr = data.getEntryDateStr();
        String salaryType = data.getSalaryType();
        String employeeAttribution = data.getEmployeeAttribution();
        // ??????
        // ???????????????????????????????????????????????????  ???????????? ??????????????????xxx???????????????????????????????????????????????????
        if (employeeNum == null) {
            errorInfo.add(rowIndex, "??????????????????");
        } else {
            if (StrUtil.length(employeeNum) > 20) {
                errorInfo.add(rowIndex, "????????????????????????20");
            }
            // ????????????????????????????????????????????????
            if (!CommonUtil.isEmployeeNum(employeeNum)) {
                errorInfo.add(rowIndex, "?????????????????????");
            } else {
                // ????????????
                String employeeName = getNameByEmployeeNum(employeeNum);
                if (employeeName != null) {
                    errorInfo.add(rowIndex, "??????????????????");
                }
            }
        }
        if (name == null) {
            errorInfo.add(rowIndex, "??????????????????");
        } else {
            if (StrUtil.length(name) > 20) {
                errorInfo.add(rowIndex, "????????????????????????20");
            }
            // ??????????????? ????????? ????????????????????????
            if (!NameUtil.isEmployeeName(name)) {
                errorInfo.add(rowIndex, "?????????????????????");
            } else {
                if (nameExits(name)) {
                    errorInfo.add(rowIndex, "??????????????????????????????????????????????????????-??????????????????????????????-A001???");
                }
            }
        }
        Opt.ofNullable(orgName).ifPresent(x -> getOrgId(orgName).ifPresentOrElse(data::setOrgId, () -> errorInfo.add(rowIndex, "???????????????????????????")));
        if (mobile == null) {
            errorInfo.add(rowIndex, "????????????????????????");
        } else {
            // ???????????? ?????????????????????????????????????????????????????????
            if (mobile.length() == 11 && Validator.isMobile(mobile)) {
                String employeeName = getEmployeeNameByMobile(mobile);
                if ("".equals(employeeName)) {
                    errorInfo.add(rowIndex, "?????????????????????");
                } else if (StrUtil.isNotEmpty(employeeName)) {
                    errorInfo.add(rowIndex, "??????????????????{}??????", employeeName);
                }
            } else {
                errorInfo.add(rowIndex, "???????????????????????????");
            }
        }
        if (employmentType == null) {
            errorInfo.add(rowIndex, "??????????????????????????????????????????");
        } else {
            getEmploymentTypeId(employmentType).ifPresentOrElse(data::setEmploymentTypeId, () -> errorInfo.add(rowIndex, "?????????????????????????????????"));
        }
        // if (employmentSource == null) {
        //     errorInfo.add(rowIndex, "??????????????????????????????????????????");
        // } else {
        //     getEmploymentSourceId(employmentSource).ifPresentOrElse(data::setEmploymentSourceId, () -> errorInfo.add(rowIndex, "?????????????????????????????????"));
        // }
        if (idCardType == null) {
            errorInfo.add(rowIndex, "????????????????????????");
        } else {
            getIdCardType(idCardType).ifPresentOrElse(e -> data.setIdCardTypeCode(e.getCode()), () -> errorInfo.add(rowIndex, "?????????????????????????????????"));
        }
        if (idCardNum == null) {
            errorInfo.add(rowIndex, "????????????????????????");
        } else {
            // ???????????????????????????????????????????????????????????? ?????????????????????????????????????????????????????????
            getIdCardType(idCardType).ifPresent(e -> {
                if (!e.validIdCard(idCardNum)) {
                    errorInfo.add(rowIndex, "???????????????????????????");
                }
            });
            HrmServiceProto.EmployeePageResponse employeeInfo = getEmployeeInfoByIdCardNum(idCardNum);
            if (employeeInfo != null) {
                if (HrmServiceProto.EmployeePageResponse.getDefaultInstance().equals(employeeInfo)) {
                    errorInfo.add(rowIndex, "????????????????????????");
                } else {
                    if (employeeInfo.getStatus() == 4) {
                        errorInfo.add(rowIndex, "?????????????????????????????????");
                    } else {
                        errorInfo.add(rowIndex, "??????????????????{}??????", employeeInfo.getName());
                    }
                }
            }
        }
        if (entryDateStr != null) {
            // ???????????????2022???9???8??? 2019/04/01???2019/4/1???2019-04-01???2019-4-1???2019.04.01???2019.4.1
            Opt.ofTry(() -> new DateTime(entryDateStr)).ifPresentOrElse(data::setEntryDate, () -> errorInfo.add(rowIndex, "???????????????????????????"));
        } else {
            errorInfo.add(rowIndex, "????????????????????????");
        }
        Opt.ofNullable(salaryType).ifPresent(s -> getSalaryTypeId(salaryType).ifPresentOrElse(data::setSalaryTypeId, () -> errorInfo.add(rowIndex, "?????????????????????????????????")));
        if (employeeAttribution == null) {
            // errorInfo.add(rowIndex, "????????????????????????");
        } else {
            getEmployeeAttributionId(employeeAttribution).ifPresentOrElse(data::setEmployeeAttributionId, () -> errorInfo.add(rowIndex, "?????????????????????????????????"));
        }
    }

    /**
     * ????????????
     */
    @NonNull
    private Map<Long, String> getEmploymentSources() {
        // ?????????????????? 1???????????? 2????????????
        List<EmployeeDictionaryResult> employeeDictList = hrmEmployeeService.findEmployeeDictList(2);
        return employeeDictList.stream().collect(Collectors.toMap(EmployeeDictionaryResult::getId, EmployeeDictionaryResult::getName));
    }

    /**
     * ??????????????????
     */
    @NonNull
    private Map<Long, String> getEmploymentTypes() {
        // ?????????????????? 1???????????? 2????????????
        List<EmployeeDictionaryResult> employeeDictList = hrmEmployeeService.findEmployeeDictList(1);
        return employeeDictList.stream().collect(Collectors.toMap(EmployeeDictionaryResult::getId, EmployeeDictionaryResult::getName));
    }

    /**
     * ????????????
     */
    private Map<Long, String> getOrgNames() {
        Long accountId = XBB_USER_CONTEXT.get().getUserId();
        HrmServiceProto.FindSubordinateRequest build = HrmServiceProto.FindSubordinateRequest.newBuilder().setLevel(1).setAccountId(accountId).build();
        List<HrmServiceProto.OrgListResponse> dataList = hrmServiceBlockingStub.findSubordinateOrg(build).getDataList();
        Map<Long, String> map = dataList.stream().collect(Collectors.toMap(HrmServiceProto.OrgListResponse::getId, HrmServiceProto.OrgListResponse::getName));
        return dataList.stream().collect(Collectors.toMap(HrmServiceProto.OrgListResponse::getId, e -> Arrays.stream(ArrayUtil.reverse(e.getUpperPath().split(","))).map(Long::valueOf).map(map::get).collect(Collectors.joining("/"))));
    }

    private HrmServiceProto.EmployeePageResponse getEmployeeInfoByIdCardNum(String idCardNum) {
        Set<String> idCardNums = ThreadLocalPool.IMPORT_EMPLOYEE_ID_CARD_NUMS.get();
        if (!idCardNums.add(idCardNum)) {
            return HrmServiceProto.EmployeePageResponse.getDefaultInstance();
        }
        return ThreadLocalPool.ID_CARD_NUM_MAP.get().get(idCardNum);
    }

    private boolean nameExits(String name) {
        Set<String> names = ThreadLocalPool.IMPORT_EMPLOYEE_NAMES.get();
        boolean notExits = names.add(name);
        if (!notExits) {
            return true;
        }
        // ??????????????????????????????
        return ThreadLocalPool.NAME_MAP.get().containsKey(name);
    }

    private String getEmployeeNameByMobile(String mobile) {
        Set<String> mobiles = ThreadLocalPool.IMPORT_EMPLOYEE_MOBILES.get();
        if (!mobiles.add(mobile)) {
            return "";
        }
        // ??????????????????????????????????????????empty?????????null
        String name = Opt.ofNullable(ThreadLocalPool.MOBILE_MAP.get().get(mobile)).map(HrmServiceProto.EmployeePageResponse::getName).orElse(null);
        return StrUtil.emptyToNull(name);
    }

    private String getNameByEmployeeNum(String employeeNum) {
        if (!ThreadLocalPool.IMPORT_EMPLOYEE_NUMS.get().add(employeeNum)) {
            return "";
        }
        String name = Opt.ofNullable(ThreadLocalPool.EMPLOYEE_NUM_MAP.get().get(employeeNum)).map(HrmServiceProto.EmployeePageResponse::getName).orElse(null);
        return StrUtil.emptyToNull(name);
    }

    /**
     * ??????????????????????????????id
     */
    private Opt<Integer> getSalaryTypeId(String salaryType) {
        return Opt.ofNullable(SALARY_TYPES.get(salaryType));
    }

    /**
     * ????????????????????????id
     */
    private Opt<Long> getOrgId(String orgName) {
        return Opt.ofNullable(ThreadLocalPool.ORG_NAMES.get().get(orgName));
    }

    /**
     * ????????????????????????????????????id
     */
    private Opt<Integer> getEmployeeAttributionId(String employeeAttribution) {
        return Opt.ofNullable(EMPLOYEE_ATTRIBUTION.get(employeeAttribution));
    }

    /**
     * ??????????????????????????????id
     */
    private Opt<IdCardTypeEnum> getIdCardType(String idCardType) {
        return IdCardTypeEnum.get(idCardType);
    }

    /**
     * ??????????????????????????????id
     */
    private Opt<Long> getEmploymentSourceId(String employmentSource) {
        return Opt.ofNullable(ThreadLocalPool.EMPLOYMENT_SOURCES.get().get(employmentSource));
    }

    /**
     * ??????????????????????????????id
     */
    private Opt<Long> getEmploymentTypeId(String employmentType) {
        return Opt.ofNullable(ThreadLocalPool.EMPLOYMENT_TYPES.get().get(employmentType));
    }

    /**
     * ???????????????
     */
    @WillClose
    @Component("ExcelService.ThreadLocalPool")
    public static class ThreadLocalPool implements IThreadLocalPool {
        public static final ThreadLocal<Set<String>> IMPORT_EMPLOYEE_NUMS = TransmittableThreadLocal.withInitial(HashSet::new);
        public static final ThreadLocal<Set<String>> IMPORT_EMPLOYEE_NAMES = TransmittableThreadLocal.withInitial(HashSet::new);
        public static final ThreadLocal<Set<String>> IMPORT_EMPLOYEE_MOBILES = TransmittableThreadLocal.withInitial(HashSet::new);
        public static final ThreadLocal<Set<String>> IMPORT_EMPLOYEE_ID_CARD_NUMS = TransmittableThreadLocal.withInitial(HashSet::new);
        public static final ThreadLocal<Map<String, Long>> EMPLOYMENT_TYPES = TransmittableThreadLocal.withInitial(Collections::emptyMap);
        public static final ThreadLocal<Map<String, Long>> EMPLOYMENT_SOURCES = TransmittableThreadLocal.withInitial(Collections::emptyMap);
        public static final ThreadLocal<Map<String, Long>> ORG_NAMES = TransmittableThreadLocal.withInitial(Collections::emptyMap);
        public static final ThreadLocal<Map<String, HrmServiceProto.EmployeePageResponse>> EMPLOYEE_NUM_MAP = TransmittableThreadLocal.withInitial(Collections::emptyMap);
        public static final ThreadLocal<Map<String, HrmServiceProto.EmployeePageResponse>> MOBILE_MAP = TransmittableThreadLocal.withInitial(Collections::emptyMap);
        public static final ThreadLocal<Map<String, HrmServiceProto.EmployeePageResponse>> ID_CARD_NUM_MAP = TransmittableThreadLocal.withInitial(Collections::emptyMap);
        public static final ThreadLocal<Map<String, HrmServiceProto.EmployeePageResponse>> NAME_MAP = TransmittableThreadLocal.withInitial(Collections::emptyMap);

        public static void removeAll() {
            IMPORT_EMPLOYEE_NUMS.remove();
            IMPORT_EMPLOYEE_NAMES.remove();
            IMPORT_EMPLOYEE_MOBILES.remove();
            IMPORT_EMPLOYEE_ID_CARD_NUMS.remove();
            log.info("removed");
        }

        @Override
        public void remove() {
            removeAll();
        }
    }
}
