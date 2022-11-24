package cn.xunhou.web.xbbcloud.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ZipUtil;
import cn.xunhou.cloud.core.exception.SystemRuntimeException;
import cn.xunhou.cloud.framework.plugins.file.IFileOperator;
import cn.xunhou.cloud.framework.plugins.file.dto.FileDownload4StreamDto;
import cn.xunhou.cloud.framework.plugins.file.dto.FileDownloadDto;
import cn.xunhou.cloud.framework.plugins.file.dto.FileUpload4StreamDto;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.web.xbbcloud.product.hrm.constant.ConstantData;
import cn.xunhou.web.xbbcloud.util.pojo.result.ZipFilesResult;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 文件操作
 */
@Slf4j
public class XhFileUtils {

    /**
     * 下载excel文件 解析成list
     *
     * @param ossFileId oss文件id地址
     * @param target    输出类型
     * @param <T>       输出类型
     * @return 读取数据集
     */
    public static <T> List<T> readExcel(String ossFileId, Class<T> target) {
        IFileOperator fileOperator = SpringContextUtil.getBean(IFileOperator.class);
        FileDownloadDto fileDownloadDto = fileOperator.downloadFile(ossFileId);
        return readExcel(fileDownloadDto, target);
    }

    public static <T> List<T> readExcel(FileDownloadDto fileDownloadDto, Class<T> target) {
        List<T> importData = new ArrayList<>();
        // 基础校验
        InputStream inputStream = IoUtil.toStream(fileDownloadDto.getBytes());
        long fileSize = fileDownloadDto.getBytes().length;
        if (fileSize == 0) {
            // 文件不能为空
            throw new SystemRuntimeException("文件为空");
        }
        // 校验文件大小 10M以内
        if (fileSize > ConstantData.ONE_MB_BYTES * 10) {
                throw new SystemRuntimeException("文件大小不能超过10MB");
            }
            try (ExcelReader excelReader = EasyExcelFactory.read(inputStream).charset(UTF_8).build()) {
                ExcelTypeEnum excelType = excelReader.analysisContext().readWorkbookHolder().getExcelType();
                if (excelType != ExcelTypeEnum.XLSX) {
                    // 文件格式 .xlsx
                    throw new SystemRuntimeException("文件格式不正确，需要【excel2007】以上的版本(.xlsx)");
                }
                // 校验表格的第一个sheet
                ReadSheet readSheet = EasyExcelFactory.readSheet(0).head(target).registerReadListener(new AnalysisEventListener<T>() {
                    @Override
                    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                        super.invokeHeadMap(headMap, context);
                    }

                    @Override
                    public void invoke(T data, AnalysisContext context) {
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

        return importData;
    }

    /**
     * 压缩上传
     * 默认两天过期
     *
     * @param files   文件id
     * @param zipName 压缩后文件名
     */
    public static ZipFilesResult zipUrlFiles(Collection<String> files, String zipName) {
        ZipFilesResult result = new ZipFilesResult();
        result.setAllCount(files.size());
        result.setSuccessCount(0);
        IFileOperator fileOperator = SpringContextUtil.getBean(IFileOperator.class);
        List<InputStream> inputStreamList = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (String fileId : files) {
            try {
                FileDownload4StreamDto file4Stream = fileOperator.downloadFile4Stream(fileId);
                inputStreamList.add(file4Stream.getStreamContent());
                names.add(file4Stream.getFileName());
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("文件下载异常");
            }
        }

        //定义临时输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //压缩方法
        ZipUtil.zip(outputStream, names.toArray(new String[0]), inputStreamList.toArray(new InputStream[0]));

        FileUpload4StreamDto fileUpload4StreamDto = new FileUpload4StreamDto();
        fileUpload4StreamDto.setFileName(zipName);
        fileUpload4StreamDto.setFileContent(IoUtil.toStream(outputStream.toByteArray()));
        result.setZipFileId(fileOperator.uploadPublicFileForUrl(fileUpload4StreamDto));
        return result;
    }
}
