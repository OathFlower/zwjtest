package cn.xunhou.xbbcloud.rpc.approve.config;

import com.aliyun.openservices.shade.com.google.common.base.Objects;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: chenning
 * @Date: 2022/09/26/17:11
 * @Description:
 */
@Component
//@PropertySource(value={"classpath:workflow_form.yml"},ignoreResourceNotFound = false,factory = MyPropertySourceFactory.class)
@ConfigurationProperties(prefix = "form")
@Data
public class FormTemplateConfig {

    List<FormTemplate> templates;
    
    public List<FormTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<FormTemplate> templates) {
        this.templates = templates;
    }

    public FormTemplate getTemplateById(Long id) {
        for (FormTemplate template : templates) {
            if (Objects.equal(template.getTemplateId(),id)){
                return template;
            }
        }
        return null;
    }
}
