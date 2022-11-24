package cn.xunhou.xbbcloud.config;

import cn.xunhou.cloud.framework.plugins.file.FileOperatorFactory;
import cn.xunhou.cloud.framework.plugins.file.IFileOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * 删除
 */
@Configuration
public class FileOperatorConfiguration {
    @Autowired
    @Lazy
    private FileOperatorFactory fileOperatorFactory;

    @Bean
    public IFileOperator fileOperator() {
        return fileOperatorFactory.getDefault();
    }
}
