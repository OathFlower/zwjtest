package cn.xunhou.web.xbbcloud.config;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.ScheduledUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;


@Configuration
@PropertySource("classpath:wxpay.properties") //读取配置文件
@ConfigurationProperties(prefix = "wxpay") //读取wxpay节点
@Data //使用set方法将wxpay节点中的值填充到当前类的属性中
@Slf4j
public class WxPayConfig {


    // 微信服务器地址
    private String domain;

    // 测试环境回调
    private String qaNotifyDomain;

    // 正式环境回调
    private String onlineNotifyDomain;

    @Resource
    private WxConfig wxConfig;

    /**
     * 获取商户的私钥文件
     *
     * @return
     */
    public PrivateKey getPrivateKey() {

       /* try {
            Resource resource = new ClassPathResource("/" + filename);
            InputStream is = resource.getInputStream();

            return PemUtil.loadPrivateKey(is);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("私钥文件不存在", e);
        } catch (IOException e) {
            throw new RuntimeException("文件IO异常", e);
        }*/
        log.info(wxConfig.getPrivateKey());
        return PemUtil.loadPrivateKey(wxConfig.getPrivateKey());
    }
    /**
     * 获取签名验证器
     *
     * @return
     */
    @Bean
    public ScheduledUpdateCertificatesVerifier getVerifier() {

        log.info("获取签名验证器");
        //获取商户私钥
        PrivateKey privateKey = getPrivateKey();

        //私钥签名对象
        PrivateKeySigner privateKeySigner = new PrivateKeySigner(wxConfig.getMchSerialNo(), privateKey);

        //身份认证对象
        WechatPay2Credentials wechatPay2Credentials = new WechatPay2Credentials(wxConfig.getMchId(), privateKeySigner);

        // 使用定时更新的签名验证器，不需要传入证书
        ScheduledUpdateCertificatesVerifier verifier = new ScheduledUpdateCertificatesVerifier(
                wechatPay2Credentials,
                wxConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));

        return verifier;
    }


    /**
     * 获取http请求对象
     *
     * @param verifier
     * @return
     */
    @Bean(name = "wxPayClient")
    public CloseableHttpClient getWxPayClient(ScheduledUpdateCertificatesVerifier verifier) {

        log.info("获取httpClient");

        //获取商户私钥
        PrivateKey privateKey = getPrivateKey();

        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                .withMerchant(wxConfig.getMchId(), wxConfig.getMchSerialNo(), privateKey)
                .withValidator(new WechatPay2Validator(verifier));
        // ... 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient

        // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新
        CloseableHttpClient httpClient = builder.build();

        return httpClient;
    }

    /**
     * 获取HttpClient，无需进行应答签名验证，跳过验签的流程
     */
    @Bean(name = "wxPayNoSignClient")
    public CloseableHttpClient getWxPayNoSignClient() {

        //获取商户私钥
        PrivateKey privateKey = getPrivateKey();

        //用于构造HttpClient
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                //设置商户信息
                .withMerchant(wxConfig.getMchId(), wxConfig.getMchSerialNo(), privateKey)
                //无需进行签名验证、通过withValidator((response) -> true)实现
                .withValidator((response) -> true);

        // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新
        CloseableHttpClient httpClient = builder.build();

        log.info("== getWxPayNoSignClient END ==");

        return httpClient;
    }


}
