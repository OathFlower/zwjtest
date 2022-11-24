package cn.xunhou.web.xbbcloud.product.sxz.service;


import cn.xunhou.cloud.core.context.UserParam;
import cn.xunhou.cloud.core.json.XbbCamelJsonUtil;
import cn.xunhou.cloud.core.snow.SnowflakeIdGenerator;
import cn.xunhou.cloud.core.util.SystemUtil;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.cloud.core.web.XbbWebStatus;
import cn.xunhou.cloud.framework.plugins.file.IFileOperator;
import cn.xunhou.cloud.framework.plugins.file.dto.FileUpload4StreamDto;
import cn.xunhou.cloud.framework.util.DesPlus;
import cn.xunhou.cloud.web.mvc.auth.XbbJwtAuthService;
import cn.xunhou.grpc.proto.universal.UniversalServiceGrpc;
import cn.xunhou.grpc.proto.universal.UniversalServiceProto;
import cn.xunhou.web.xbbcloud.config.WxConfig;
import cn.xunhou.web.xbbcloud.product.sxz.dao.RecommendQrcodeRepository;
import cn.xunhou.web.xbbcloud.product.sxz.dto.JwtResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.RecommendResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.SmsAccountResult;
import cn.xunhou.web.xbbcloud.product.sxz.entity.RecommendQrcodeEntity;
import cn.xunhou.web.xbbcloud.product.sxz.entity.UserEntity;
import cn.xunhou.web.xbbcloud.product.sxz.enums.CommonConst;
import cn.xunhou.web.xbbcloud.product.sxz.param.LoginParam;
import cn.xunhou.web.xbbcloud.product.sxz.param.SendSmsVerifyParam;
import cn.xunhou.web.xbbcloud.util.CommonUtil;
import cn.xunhou.web.xbbcloud.util.MD5Utils;
import cn.xunhou.web.xbbcloud.util.ProtoConvert;
import cn.xunhou.web.xbbcloud.util.QRCodeUtils;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@Service
@Slf4j
public class AccountService {
    @GrpcClient("ins-xhwallet-platform")
    private UniversalServiceGrpc.UniversalServiceBlockingStub universalServiceBlockingStub;
    @Autowired
    private UserService userService;
    @Autowired
    private RecommendQrcodeRepository recommendQrcodeRepository;
    @Autowired
    private XbbJwtAuthService jwtAuthService;
    @Resource
    private WxConfig wxConfig;

    @Autowired
    private IFileOperator fileOperator;

    /**
     * 登录/注册
     *
     * @param param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public JsonResponse<JwtResult> login(@NonNull LoginParam param) {
        UserParam userParam = new UserParam();
        log.info("登录/注册 参数:" + XbbCamelJsonUtil.toJsonString(param));
        Integer SmsCode = 0;
        try {
            SmsCode = Integer.valueOf(param.getVerifyCode());
            UniversalServiceProto.VerifyCodeBeRequest build = UniversalServiceProto.VerifyCodeBeRequest.newBuilder()
                    .setTel(param.getTel())
                    .setTemplateCode("T00043")
                    .setCode(SmsCode)
                    .setTenantId(10000).build();
            universalServiceBlockingStub.smsVerifyCode(build);
        } catch (Exception e) {
            return JsonResponse.failed(XbbWebStatus.VerifyCodeErr);
        }

        //先查看是否有该手机号在数据库 有的话为登录 不需要校验是否有邀请码
        UserEntity userEntity = userService.getUserByMobile(param.getTel());
        Map<String, String> claims = new HashMap();
        if (userEntity == null) {
            //注册
            UserEntity saveUserEntity = new UserEntity();
            saveUserEntity.setTel(DesPlus.getInstance().encrypt(param.getTel()));
            saveUserEntity.setAdminType(1);
            saveUserEntity.setInterviewCode(StringUtils.isBlank(param.getInterviewCode()) ? CommonConst.DEFAULT_INTERVIEW_CODE : param.getInterviewCode());
            Long saveUserId = userService.register(saveUserEntity);
            userParam.setUserId(saveUserId);
        } else {
            userParam.setUserId(userEntity.getId());
        }
        userParam.setNonce(SnowflakeIdGenerator.getId());
        log.info("用户为==" + userParam.getUserId());
        String token = jwtAuthService.genJwt(userParam, CommonUtil.getTomorrowWeeHours());
        log.info("发出的token" + token);
        return JsonResponse.success(JwtResult.builder().token(token).build());

    }


    /**
     * 发送短信验证码
     *
     * @param param
     * @return
     */
    public JsonResponse<SmsAccountResult> sendSmsVerify(@NonNull SendSmsVerifyParam param) {
        log.info("发送短信验证码 参数:" + XbbCamelJsonUtil.toJsonString(param));
        UniversalServiceProto.SmsVerifyMessageBeRequest build = UniversalServiceProto.SmsVerifyMessageBeRequest.newBuilder()
                .setTel(param.getPhone())
                .setTemplateCode("T00043")
                .setTenantId(10000)
                .build();
        universalServiceBlockingStub.sendSmsVerify(build);
        return JsonResponse.success(new SmsAccountResult()
                .setMobile(ProtoConvert.nonnull(param.getPhone())));
    }


    @Transactional(rollbackFor = Exception.class)
    public JsonResponse<List<RecommendResult>> initRecommendResultList(String url, Integer count) throws Exception {
        List<RecommendResult> recommendResultList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            RecommendQrcodeEntity recommendQrcodeEntity = new RecommendQrcodeEntity();
            Long saveEntityId = recommendQrcodeRepository.insert(recommendQrcodeEntity).longValue();


            //通过md5生成对应的随机邀请码
            String interviewCode = MD5Utils.stringToMD5(saveEntityId.toString() + new Date());
            String interviewCodeSub = interviewCode.substring(0, 5);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            String qrcodeUlr = "";
            QRCodeUtils.createCodeToOutputStream(url + "?" + "interviewCode=" + interviewCodeSub, byteArrayOutputStream);
            byte[] buffer = byteArrayOutputStream.toByteArray();
            InputStream sbs = new ByteArrayInputStream(buffer);
            try {
                FileUpload4StreamDto streamDto = new FileUpload4StreamDto();
                streamDto.setFileName(interviewCodeSub);
                streamDto.setContentType("png");
                streamDto.setFileContent(sbs);
                qrcodeUlr = END_FRONT_STATIC_RESOURCE_URL_PREFIX + fileOperator.uploadPublicFileOrImage4Stream(streamDto);

            } catch (Exception e) {
                throw new Exception("文件上传失败");
            } finally {
                IOUtils.closeQuietly(sbs);
            }
            recommendQrcodeEntity.setQrcodeUrl(qrcodeUlr);
            recommendQrcodeEntity.setInterviewCode(interviewCodeSub);
            recommendQrcodeRepository.updateById(saveEntityId, recommendQrcodeEntity);
            RecommendResult recommendResult = new RecommendResult();
            BeanUtils.copyProperties(recommendQrcodeEntity, recommendResult);
            recommendResultList.add(recommendResult);
        }

        return JsonResponse.success(recommendResultList);
    }

    /**
     * 前端静态资源文件cdn域名
     */
    private static final String END_FRONT_STATIC_RESOURCE_URL_PREFIX;

    static {
        if (SystemUtil.isOffline()) {
            END_FRONT_STATIC_RESOURCE_URL_PREFIX = "https://qa-static.xunhou.cn/";
        } else {
            END_FRONT_STATIC_RESOURCE_URL_PREFIX = "https://static.xunhou.cn/";
        }
    }

    public JsonResponse<String> getOpenId(String code) {
        String openIdUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

            String requestUrl = String.format(openIdUrl, wxConfig.getAppId(), wxConfig.getSecret(), code);
            HttpGet httpGet = new HttpGet(requestUrl);
            HttpEntity responseEntity = httpClient.execute(httpGet).getEntity();
            if (responseEntity != null) {
                String responseStr = EntityUtils.toString(responseEntity);

                if (responseStr.contains("openid")) {
                    Gson gson = new Gson();
                    HashMap plainTextMap = gson.fromJson(responseStr, HashMap.class);
                    String openid = (String) plainTextMap.get("openid");
                    return JsonResponse.success(openid);
                }
                return JsonResponse.systemError(responseStr);
            }
        } catch (IOException e) {
            log.info("getOpenId报错" + e);
        }
        return null;
    }


}
