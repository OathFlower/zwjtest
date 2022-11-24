package cn.xunhou.web.xbbcloud.product.hrm.controller;

import cn.xunhou.cloud.framework.plugins.file.IFileOperator;
import cn.xunhou.cloud.framework.plugins.file.dto.FileDownloadDto;
import cn.xunhou.web.xbbcloud.product.hrm.constant.ConstantData;
import cn.xunhou.web.xbbcloud.util.MvcUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.UnexpectedTypeException;
import java.io.IOException;

/**
 * 人事云-文件处理
 * @author sha.li
 * @since 2022/9/16
 */
@RestController
@RequestMapping("/hrm/file")
public class FileController {
    @Autowired
    HttpServletResponse response;

    @Autowired
    private IFileOperator fileOperator;


    @GetMapping("/download/{fileId}")
    public void fileDownload(@PathVariable("fileId") String fileId) throws IOException {
        FileDownloadDto fileDownloadDto = fileOperator.downloadFile(fileId);
        String contentType = fileDownloadDto.getContentType();
        if (ConstantData.XLSX_CONTENT_TYPE.equals(contentType)) {
            MvcUtil.setExcelResponseInfo(response, fileDownloadDto.getFileName());
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                outputStream.write(fileDownloadDto.getBytes());
            }
            return;
        }
        throw new UnexpectedTypeException("文件类型不支持");
    }
}
