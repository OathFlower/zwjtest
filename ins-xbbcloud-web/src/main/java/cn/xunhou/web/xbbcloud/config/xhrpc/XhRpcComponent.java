package cn.xunhou.web.xbbcloud.config.xhrpc;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.core.exception.SystemRuntimeException;
import cn.xunhou.cloud.framework.util.SystemUtil;
import cn.xunhou.web.xbbcloud.config.ServiceInstanceDiscovery;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumXhTenant;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhP;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhR;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhRpcParam;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhTotal;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangkm
 */
@Slf4j
@Component
public class XhRpcComponent {
    @Resource
    private ServiceInstanceDiscovery serviceInstanceDiscovery;

    @Resource(name = "xhRestTemplate")
    private RestTemplate restTemplate;

    @Value("${spring.application.client_id}")
    private Long clientId;

    private static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        return (((ServletRequestAttributes) requestAttributes).getRequest());
    }

    public <R> XhR<List<R>> sendForList(@NonNull XhRpcParam param, Class<R> tClass) {
        XhR<List> r = send(param, List.class);
        XhR<List<R>> r1 = new XhR<>();
        r1.setStatus(r.getStatus()).setMessage(r.getMessage());
        if (r.getData() == null) {
            r1.setData(Collections.emptyList());
            return r1;
        } else {
            r1.setData(JSONUtil.toList(JSONUtil.parseArray(r.getData()), tClass));
        }
        return r1;
    }


    /**
     * @param param 入参
     * @return 响应结果
     */
    public <U> XhR<XhTotal<U>> sendForTotal(@NonNull XhRpcParam param, Class<U> tClass) {
        XhR<XhTotal> r = send(param, XhTotal.class);
        XhTotal<U> xhTotal = new XhTotal<>();
        xhTotal.setTotalCount(0L);

        XhR<XhTotal<U>> r1 = new XhR<>();
        r1.setData(xhTotal).setStatus(r.getStatus()).setMessage(r.getMessage());
        XhTotal data = r.getData();
        if (data == null) {
            xhTotal.setList(Collections.emptyList());
            return r1;
        }
        xhTotal.setTotalCount(data.getTotalCount());
        if (data.getList() != null) {
            xhTotal.setList(JSONUtil.toList(JSONUtil.parseArray(data.getList()), tClass));
        }
        return r1;
    }



    /**
     * @param param       入参
     * @return 响应结果
     */
    public <T> XhR<T> send(@NonNull XhRpcParam param, Class<T> tClass) {
        if (param.getServiceProject() == null) {
            throw new SystemRuntimeException("服务端项目不能为空");
        }
        if (CharSequenceUtil.isBlank(param.getUri())) {
            throw new SystemRuntimeException("rpc地址不能为空");
        }
        JSONObject response = getResponse(param);
        String responseLog = JSONUtil.toJsonStr(response);
        //截断日志
        int len = 5000;
        int length = CharSequenceUtil.length(JSONUtil.toJsonStr(response));
        if (length > len) {
            log.info("response ={}... length = {}", CharSequenceUtil.sub(responseLog, 0, len), length);
        } else {
            log.info("response ={}", responseLog);
        }
        if (response == null) {
            return new XhR<>();
        }
        XhR<T> xhR = new XhR<>();
        if (tClass != null) {
            T data = response.get("data", tClass);
            if (BeanUtil.isNotEmpty(data)) {
                xhR.setData(data);
            }
        }
        Integer code = response.getInt("status");
        String message = response.getStr("message");
        xhR.setStatus(code).setMessage(message);
        return xhR;
    }

    private JSONObject getResponse(XhRpcParam param) {
        String ip = null;
        String uri = param.getUri();
        try {
            ip = serviceInstanceDiscovery.getBalanceInstance(param.getServiceProject().getProjectName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(bean2MultiValueMap(requestParam(param, ip)), getHeader());
        log.info("request ={}", JSONUtil.toJsonStr(httpEntity));

        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }
        String url = String.format("%s/RPC%s", ip, uri);
        return restTemplate.postForObject(url, httpEntity, JSONObject.class);
    }


    private XhP requestParam(XhRpcParam param, String ip) {
        XhP xhP = new XhP();
        Long time = System.currentTimeMillis();
        String version = this.getClass().getPackage().getImplementationVersion();
        xhP.setTraceId("bs." + time);
        xhP.setArea(SystemUtil.getLogicAreaStr());

        Map<String, Object> ext = new HashMap<>(2);
        if (param.getXhTenant() == null) {
            ext.put("tenant_id", EnumXhTenant.XUNHOU.getId());
        } else {
            ext.put("tenant_id", param.getXhTenant().getId());
        }
        xhP.setTransmitExtend(ext);
        if (param.getUserId() != null) {
            xhP.setCurrentUserId(param.getUserId());
        } else {
            xhP.setCurrentUserId(0L);
        }
        xhP.setClientId(param.getClientId() == null ? this.clientId : param.getClientId());
        if (param.getRequest() != null) {
            xhP.setData(JSONUtil.toJsonStr(param.getRequest()));
        }
        xhP.setCat(null);

        try {
            HttpServletRequest httpServletRequest = getRequest();
            xhP.setInitiateUrl(httpServletRequest.getRequestURI());
            xhP.setRootDomain(httpServletRequest.getRemoteAddr());
        } catch (Exception e) {
            log.warn("获取不到请求信息！", e);
        }
        xhP.setTimeRiver(time);
        xhP.setVersion(version);
        xhP.setOriginalIp(ip);
        return xhP;
    }

    private HttpHeaders getHeader() {
        HttpHeaders header = new HttpHeaders();
        // 需求需要传参为form-data格式
        header.remove("Accept-Encoding");
        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        header.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        return header;
    }

    private MultiValueMap<String, Object> bean2MultiValueMap(Object param) {
        Map<String, Object> params = JSONUtil.toBean(JSONUtil.toJsonStr(param), Map.class);
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        for (String key : params.keySet()) {
            multiValueMap.add(key, params.get(key));
        }
        return multiValueMap;
    }
}
