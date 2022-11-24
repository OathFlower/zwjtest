package cn.xunhou.web.xbbcloud.product.hrm.service;

import cn.xunhou.cloud.framework.XbbApplication;
import cn.xunhou.cloud.framework.plugins.file.IFileOperator;
import cn.xunhou.cloud.framework.plugins.file.dto.FileUploadDto;
import org.apache.poi.util.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/**
 * @author litb
 * @date 2022/9/22 11:26
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = XbbApplication.class)
@RunWith(SpringRunner.class)
public class ExcelServiceTest {

    @Autowired
    private IFileOperator fileOperator;

    @Test
    public void importEmployee() {

        File file = new File("C:\\Users\\li\\Desktop\\netty优雅停机.png");
        try (InputStream inputStream = new FileInputStream(file)) {
            FileUploadDto fileUploadDto = new FileUploadDto();
            fileUploadDto.setFileContent(IOUtils.toByteArray(inputStream));
            fileUploadDto.setFileName("netty优雅停机.png");
            fileUploadDto.setMetaData(Collections.singletonMap("author", "cube"));
            //fileUploadDto.setContentType("image/png");
            // 62582c34c07bab7ea772c184.png
            String fileId = fileOperator.uploadPublicFileOrImage(fileUploadDto);
            System.out.println("文件上传id: " + fileId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}