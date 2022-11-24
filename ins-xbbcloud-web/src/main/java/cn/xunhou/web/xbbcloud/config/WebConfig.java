package cn.xunhou.web.xbbcloud.config;


import cn.xunhou.cloud.framework.plugins.file.FileOperatorFactory;
import cn.xunhou.cloud.framework.plugins.file.IFileOperator;
import cn.xunhou.cloud.web.mvc.auth.XbbJwtAuthService;
import cn.xunhou.cloud.web.mvc.interceptor.XbbAuthInterceptor;
import com.aliyun.openservices.shade.com.alibaba.fastjson.serializer.SerializerFeature;
import com.aliyun.openservices.shade.com.alibaba.fastjson.support.config.FastJsonConfig;
import com.aliyun.openservices.shade.com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    private XbbJwtAuthService xbbJwtAuthService;

    @Autowired
    @Lazy
    private FileOperatorFactory fileOperatorFactory;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new XbbAuthInterceptor(xbbJwtAuthService)).addPathPatterns("/**").excludePathPatterns("/accounts/**").excludePathPatterns("/hrm/api/**");
    }

    @Bean
    public IFileOperator fileOperator() {
        return fileOperatorFactory.getDefault();
    }

    @Bean("xhRestTemplate")
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        // 重置编码UTF8
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setDefaultCharset(StandardCharsets.UTF_8);
        restTemplate.getMessageConverters().set(6, mappingJackson2HttpMessageConverter);
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);
        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        fastJsonHttpMessageConverter.setDefaultCharset(StandardCharsets.UTF_8);

        restTemplate.getMessageConverters().add(fastJsonHttpMessageConverter);
        return restTemplate;
    }
}
