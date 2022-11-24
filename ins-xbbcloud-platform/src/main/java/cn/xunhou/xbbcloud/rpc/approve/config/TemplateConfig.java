package cn.xunhou.xbbcloud.rpc.approve.config;

import com.aliyun.openservices.shade.com.google.common.base.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: chenning
 * @Date: 2022/09/26/17:11
 * @Description:
 */
@Component
//@PropertySource(value={"classpath:workflow_template.yml"},ignoreResourceNotFound = false,factory = MyPropertySourceFactory.class)
@ConfigurationProperties(prefix = "workflow")
public class TemplateConfig {

    List<WorkflowTemplate> templates;
    
    public List<WorkflowTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<WorkflowTemplate> templates) {
        this.templates = templates;
    }

    public WorkflowTemplate getTemplateById(Long id) {
        for (WorkflowTemplate template : templates) {
            if (Objects.equal(template.getTemplateId(),id)){
                return template;
            }
        }
        return null;
    }
}
