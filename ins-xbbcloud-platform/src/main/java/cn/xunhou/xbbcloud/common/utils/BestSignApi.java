package cn.xunhou.xbbcloud.common.utils;

import cn.xunhou.cloud.core.util.SystemUtil;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class BestSignApi {


    public static final String BEST_SIGN_ERR_PREFIX = "bestSignErr_";
    public static final String REGEX_HONGKONG_MACAO_MAINLAND_TRAVEL_PASS = "^[HMhm]{1}([0-9]{10}|[0-9]{8})$";
    public static final String REGEX_TAIWAN_TRAVEL_PASS = "^([0-9]{8}|[0-9]{10})$";
    public static final Pattern CHECK_HONGKONG_MACAO_MAINLAND_TRAVEL_PASS = Pattern.compile(REGEX_HONGKONG_MACAO_MAINLAND_TRAVEL_PASS);
    public static final Pattern CHECK_TAIWAN_TRAVEL_PASS = Pattern.compile(REGEX_TAIWAN_TRAVEL_PASS);

    private static final String URL_SIGN_PARAMS = "?developerId=%s&rtick=%s&signType=rsa&sign=%s";

    private static String developerId;

    private static String privateKey;

    private static String serverHost;

    private static String previewUrl;

    static {
        /**  沙盒环境账号13633518954 密码：Ly3633518954*/
        if (SystemUtil.isOffline()) {
            developerId = "1597235246013199311";
            privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCG2m0j05qGxGyG+VIRBSaQF1NS2iWiWFSoDvoRJGW2Ql/Zrir2UsS4eoSuPl09W6OSM9t/EgVEuCjtfFmeNEr0nZ7FN9sb+2xYlAnZ1z+WOpJI/LVhVXT47ah/C/nL5KsRQ73Ul5ec4NfG+9qxh8CY7iGgkDcrmBd8ZF2GtwxDMgI1tcGrcBcnWFDd2a6GPzZkhdppTZCPPo1U7GEmKVyBMCsI3hnvaEd7lKe0t1Xj8mVlx9kZI1hJ0pGe2R3ft64tDLbWKbWvBXET97uYvIqnc+ooarP3Hs0YADzoTm61iE8A8O+pZlRxiZYsDMw3rrQotx12uI6sh6KQ7ug0O6qTAgMBAAECggEAaxydqeJOkBL+vOrV44z6x2KvnVSGpf+RJnfW6rzFSVJJPebYWxT9CibOsnP4rV7Xeq5T/wd6Rc4dPB0UNdQFYWy9CptGH2DB+02C0ZPl/taBERYgn7I4GezzdA44GFqGAk2TP4Q1ObdhzLyJRY+1TAOBlD5Yg/jnVreFjhZg2BF9lsXrylPXge7ESIKnr0oZxCJh/+Z89Sl+1z6iRpcBfRSC8yWVPp2TX3NBzm3h6TYAmNrEGAzGug0Na6dBz0uZS68OaDWJXZ3eoZ8xD4lFqVThTqg8S6BHGqETHsB+3vOqGUuESQsikHsXiJItoj1X8XcUioozjTfDI5+iMrHtiQKBgQDJHeSgJ7HP8NPyze8SuswJ86EhYW1N2UUBm4V9fFT0qTqXQl2qfHLdXHpwSRWOl3SNKkhwos3FF+49J0kjTT+h7Ok4B6F8MNPBICnNUXtfPeF0fX/gtxabJaS9Qy1LPfQFTyvK4Pbc242ENS+H1IWsMODYoFByUVmpixxUshLINQKBgQCrp1UwFwkTMBfMomLFXudS3jycBhQeytqWd1U0/TxJGLuNFDTYfnAXS2UFYfH8xTiOwpM/PJHOJ/lA0RB9jVeuL1jobnx+HH6SeOIL4+Rl1rT5ndQBqSndPJyNsj4NdltjRwPGXyqODDZJtIYK7gQ/2xUBlmMVkUPL6L/LtBDQpwKBgQCRTvbbvDJd0lvgp2ffuXUoj41Q5+qof527HpnhY46iQw8hMIMqZcY8J9ATBsr7fnRodi9mqkpP6A1qw9K1HeAm51xN7j1/1Gt6svlg7klcNAKMPwzA2KgNqrIPSNUV8wo3N8+lQss6Iatq5V6VHjkCg0JBYfsdzacht2/dxdeKLQKBgB7rZFfmPlc7eX393ibJQofVyjsciTFGvvsNgcaquto730S5WA+LLuuzQ/4lN26do+NrPxTL90OYRbicMAk9lYaltrEffcNFpLMDnUwg/7osY6r2+AJYk1brnROVAMJyCGIon5BzAfpWTs0V/HMpjpBmCTGhAjYXVYv8X9f0lnbPAoGBAIvlAxhl6NSskC3m9aY32jdJdc/FeNMQQ7e9FHSdQdo5vHFJim6aDA8qloTpACW5KMpx8NuyOoF2AvHQL/riNKQHEtxWHL0UvXzajaPRGJWRZHpy/mvY8fRLKVq7FK5UYS3+pfaF7iF/fy5GEzDO8fMFYx3GyXCdLc/mUJCqJGyU";
            serverHost = "https://openapi.bestsign.info/openapi/v2";
            previewUrl = "https://wx103.bestsign.info/openapi/v2";
        } else {
            developerId = "1597313234014043698";
            privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC+G0HNCeA/x69K5V3vrFjFCaHTQDk2BW7MZzJRSrgENht7zXZ4CAUk6hjqQAEKmjqJIcJK0sIbwISb65NpYqfgPxXxnrk0qix/FhQw7bUO7nqCEugR7hzDfBRr7fNPgxdGVQxCjfzLa2zkZK19GlSP2RWzo2Fr52mZd9/jiFrFU+YwATFuGlzwqTy2rLEYDqJu5ta0RSRW9oWb6iGNMDZjSTzsUL9Krl/7clpdDFXDS6Ak+mgtHx33NO6X3+fScD0ExADoBaK92yAF38cpFzZG2mL0Zljft1OvFjwA/w9ZWk4MsRrBEWUKmBabSV5V2SRIqP9bF40gA3lSFpKrmLD1AgMBAAECggEAbkKq4HxY50UvA1aTLB6fAfE7PaZjSFTQVZeJFKMspayoNh8OFkI4kZeBSeRDdl3Lk5vT78fbs3LutuxkfFwvGB4e4Rv5OXV2B2PL5DEKsZVZ0rWaikO7xRnjUlIZq8VRq4ZKbROjSmyNJM7L3OJ3p1L2hlE9gPMgSVXAslx0oTutIljNN5khEuS1pk87vvyC72T/NaHZqxZJM9ChN+aRCbggeTciZe/nsnRBQIqd4yFo9RtUpoV7weUy4Nh1sQGxwvZ1tNhxig5i8Tn+mAX5Bk7IClCdGFtazNoeEMxkOGub+8s6ys143HN5NATRztjqu/6yECSoV8TuARFnEBtk4QKBgQD63JxXh91HWo+60U63rYTp9YT9zCQcEbT2G6rv9nFvIgu8Wx35UVUtMIxeLYk6G8K4x9JUvt29yu2wuIsqixL44XdafBZigxXeYiBQhmF+arQYtg9j6XrWVdl6NUZstPL4DrzOQLj/dbpOfj9/6cwIrd1FBHPhAusp00vkvdnO2QKBgQDCABO4BuMU2Gf3GmlxhJ0eHG184/I+NDeVj8rH5r5lFcrW4Rcb5257rpCkfDXBQPCuXqgPifY50mo3rsbirKsAeYgPkJCMig3fPR2EdJlUjw561coe66Ht9hmrNSA+qfs2Ephls/5QKkHpYyDgW3OYczEWbrR6tdjjuvOnUzeZfQKBgQDUGAsy0hLazg1xBJ/KGbGjYMo1DhCFjs7xuQi34yOasy067cqUzRf3ofE7esJn16q1MXR54/l0CosVe6FlsbmzpGSVow4F8hBeOZZanpL1H9/ueJbggTky19iMQ1isdfxqj4M/2mbOzUlcLc8gJZo0yFKolJAocKBEd3nWBlGNWQKBgAysJp2ZzZGaBUiN8B9bM0w5PSmwVi2cCZl6eBuQxhUiVEqvD2xiNMRdhdNIzdYe4NnzEvVY5L2oMgoLH7DE6LKyPaIOpnKXvgo8GsaBO1bbtW1us7JLYBuEPO8DHGUbg25nzo6uMEmaSQ3uzMH7F1T5CVbWynFNmapelvXyKKqdAoGBAJVe9efs4qZJC4v0dctaU+vnIF0i5iuL9KKoQNcOIoObRFSBF+Pw9i28KC6cQez+xamm1JpeIvBye4LA+PcIVjGShftqKq3T3Xi28oK8TxaPPSny3ZFeOMO+VrN335ECL0QCgcU7cpKxxHYbVwGTzzw7OUeZmOq6u9CKLK23aGxF";
            serverHost = "https://openapi.bestsign.cn/openapi/v2";
            previewUrl = "https://wx104.bestsign.cn/openapi/v2";
            serverHost = "https://wx104.bestsign.cn/openapi/v2";
        }

    }

    public static void registPersonByBestSign(String cAccountId, String name, String idCardNo, String telephone) {
        if (getCert(cAccountId)) {
            setPersonalCredential(cAccountId, name, idCardNo, telephone);
        } else {
            personalReg(cAccountId, name, idCardNo, telephone);
        }
    }

    /**
     * 注册个人用户并申请证书
     *
     * @param cAccount C端用户账号
     * @param name     C端用户名称
     * @param idNo     C端用户证件号
     * @param _mobile  C端用户手机号（非必填）
     * @return
     * @author: liuyang
     * @link https://openapi.bestsign.cn/#/dev/doc/2385375905850589185
     * @Date: 2021/8/16 14:05
     */
    public static String personalReg(String cAccount, String name, String idNo,
                                     String _mobile) {
        String method = "/user/reg/";

        // 组装请求参数，作为requestbody
        JSONObject requestBody = new JSONObject();
        requestBody.put("account", cAccount);
        requestBody.put("name", name);
        //用户类型 1表示个人
        requestBody.put("userType", "1");
        requestBody.put("mobile", _mobile);

        JSONObject credential = new JSONObject();
        credential.put("identity", idNo);
        //B 港澳居民往来内地通行证
        //C 台湾居民来往大陆通行证
        //0 身份证(默认值)
        if (match(REGEX_HONGKONG_MACAO_MAINLAND_TRAVEL_PASS, idNo)) {
            credential.put("identityType", "B");
        } else if (match(REGEX_TAIWAN_TRAVEL_PASS, idNo)) {
            credential.put("identityType", "C");
        } else {
            credential.put("identityType", "0");
        }
        credential.put("contactMobile", _mobile);
        requestBody.put("credential", credential);
        //需申请证书则填写1
        requestBody.put("applyCert", "1");

        System.out.println("getBestSignUrlrequestBody:" + requestBody.toJSONString());
        // 返回结果解析
        JSONObject result = request(method, requestBody);
        System.out.println("personalReg-----result:" + result.toJSONString());
        // 返回errno为0，表示成功，其他表示失败
        if (result.getIntValue("errno") == 0) {
            JSONObject data = result.getJSONObject("data");
            if (data != null) {
                return data.getString("taskId");
            }
            return null;
        } else {
            log.info("personalReg-----异常：" + result.getIntValue("errno") + ":"
                    + result.getString("errmsg"));
            throw GrpcException.asRuntimeException(BEST_SIGN_ERR_PREFIX + result.getString("errno") + "上上签用户注册异常");
        }
    }

    public static String createContractPdf(String bAccount, String bestSignTid, JSONObject params, String title) {
        String method1 = "/template/createContractPdf/";

        // 组装请求参数，作为requestbody
        JSONObject requestBody1 = new JSONObject();
        requestBody1.put("account", bAccount);
        requestBody1.put("tid", bestSignTid);
        // 返回结果解析
//        JSONObject templateValues1 = JSONObject.parseObject(params);
//        JSONObject groupValues = templateValues1.getJSONArray("jsonVal").getJSONObject(0);
        requestBody1.put("groupValues", params);
//		requestBody1.put("templateValues", JSONObject.parseObject(JSON.toJSONString(groupValues, SerializerFeature.DisableCircularReferenceDetect)));
        log.info("createContractPdf-----requestBody1:" + requestBody1.toJSONString());
        JSONObject result1 = request(method1, requestBody1);
        log.info("createContractPdf-----result1:" + result1.toJSONString());
        String templateToken = "";
        // 返回errno为0，表示成功，其他表示失败
        if (result1.getIntValue("errno") == 0) {
            JSONObject data = result1.getJSONObject("data");
            if (data != null) {
                templateToken = data.getString("templateToken");
            }
        } else {
            log.info("createContractPdf异常：result1=" + result1.getIntValue("errno") + ":"
                    + result1.getString("errmsg"));
            throw GrpcException.asRuntimeException(BEST_SIGN_ERR_PREFIX + result1.getString("errno") + result1.getString("errmsg"));
        }

        String method2 = "/contract/createByTemplate/";
        // 组装请求参数，作为requestbody
        JSONObject requestBody2 = new JSONObject();
        requestBody2.put("account", bAccount);
        requestBody2.put("tid", bestSignTid);
        requestBody2.put("templateToken", templateToken);
        requestBody2.put("title", title);
        requestBody2.put("expireTime", String.valueOf(getDate(getDateStr(90) + "235959").getTime() / 1000));
        log.info("createContractPdf-----requestBody2:" + requestBody2.toJSONString());
        // 返回结果解析
        JSONObject result2 = request(method2, requestBody2);
        log.info("createContractPdf-----result2:" + result2.toJSONString());
        // 返回errno为0，表示成功，其他表示失败
        if (result2.getIntValue("errno") == 0) {
            JSONObject data = result2.getJSONObject("data");
            if (data != null) {
                return data.getString("contractId");
            }
            throw GrpcException.asRuntimeException(BEST_SIGN_ERR_PREFIX + result2.getString("errno") + result2.getString("errmsg"));
        } else {
            log.info("createContractPdf异常：result2=" + result2.getIntValue("errno") + ":"
                    + result2.getString("errmsg"));
            throw GrpcException.asRuntimeException(BEST_SIGN_ERR_PREFIX + result2.getString("errno") + result2.getString("errmsg"));
        }
    }


    public static Date getDate(String dateTime) {
        if (StringUtils.isBlank(dateTime)) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date date = dateFormat.parse(dateTime);
            return date;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getDateStr(int offday) {
        Calendar cal = Calendar.getInstance();
        cal.add(5, offday);
        String year = String.valueOf(cal.get(1));
        String month = String.valueOf(cal.get(2) + 1);
        if (month.length() == 1) {
            month = "0" + month;
        }

        String day = String.valueOf(cal.get(5));
        if (day.length() == 1) {
            day = "0" + day;
        }

        return year + month + day;
    }

    public static String sendByTemplate(String contractId, String cAccount, String bestSignTid, List<String> varNames, String signReturnUrl, String notifyUrl) {
        String method = "/contract/sendByTemplate/";

        // 组装请求参数，作为requestbody
        JSONObject requestBody = new JSONObject();
        requestBody.put("contractId", contractId);
        requestBody.put("tid", bestSignTid);
        requestBody.put("signer", cAccount);
        requestBody.put("isDrawSignatureImage", "2");
        requestBody.put("signatureImageName", "default");
        requestBody.put("pushUrl", notifyUrl);
        requestBody.put("returnUrl", signReturnUrl);

        if (CollectionUtils.isEmpty(varNames)) {
            requestBody.put("varNames", "sign");
        } else {
            requestBody.put("varNames", StringUtils.join(varNames, ","));
        }

        log.info("sendByTemplate-----requestBody:" + requestBody.toJSONString());
        // 返回结果解析
        JSONObject result = request(method, requestBody);
        log.info("sendByTemplate-----result:" + result.toJSONString());
        // 返回errno为0，表示成功，其他表示失败
        if (result.getIntValue("errno") == 0) {
            JSONObject data = result.getJSONObject("data");
            if (data != null) {
                return data.getString("longUrl");
            }
            return null;
        } else {
            log.info("sendByTemplate异常：" + result.getIntValue("errno") + ":"
                    + result.getString("errmsg"));
            throw GrpcException.asRuntimeException(BEST_SIGN_ERR_PREFIX + result.getString("errno") + result.getString("errmsg"));
        }
    }

    /**
     * 设置个人用户证件信息
     *
     * @param cAccount
     * @param name
     * @param idNo
     * @param mobile
     * @return
     * @author: liuyang
     * @link https://openapi.bestsign.cn/#/dev/doc/2385375964461793282
     * @Date: 2021/8/16 17:25
     */
    private static void setPersonalCredential(String cAccount, String name, String idNo, String mobile) {
        String method = "/user/setPersonalCredential/";

        // 组装请求参数，作为requestbody
        JSONObject requestBody = new JSONObject();
        requestBody.put("account", cAccount);
        requestBody.put("name", name);
        requestBody.put("identity", idNo);
        requestBody.put("contactMobile", mobile);

        //B 港澳居民往来内地通行证
        //C 台湾居民来往大陆通行证
        //0 身份证(默认值)
        if (match(REGEX_HONGKONG_MACAO_MAINLAND_TRAVEL_PASS, idNo)) {
            requestBody.put("identityType", "B");
        } else if (match(REGEX_TAIWAN_TRAVEL_PASS, idNo)) {
            requestBody.put("identityType", "C");
        } else {
            requestBody.put("identityType", "0");
        }

        log.info("setPersonalCredential-----result:" + requestBody.toJSONString());
        // 返回结果解析
        JSONObject result = request(method, requestBody);
        log.info("setPersonalCredential-----result:" + result.toJSONString());
        // 返回errno为0，表示成功，其他表示失败
        if (result.getIntValue("errno") == 0) {
            return;
        } else {
            log.info("setPersonalCredential异常：" + result.getIntValue("errno") + ":"
                    + result.getString("errmsg"));
            throw GrpcException.asRuntimeException(BEST_SIGN_ERR_PREFIX + result.getString("errno") + "上上签设置用户信息异常");
        }
    }

    public static Boolean match(String regex, String str) {
//        Pattern p = Pattern.compile(regex);
//        Matcher m = p.matcher(str);
//        return m.matches();
        switch (regex) {
            case REGEX_HONGKONG_MACAO_MAINLAND_TRAVEL_PASS:
                return CHECK_HONGKONG_MACAO_MAINLAND_TRAVEL_PASS.matcher(str).matches();
            case REGEX_TAIWAN_TRAVEL_PASS:
                return CHECK_TAIWAN_TRAVEL_PASS.matcher(str).matches();
            default:
                return false;
        }
    }

    /**
     * 查询证书编号
     *
     * @param account B端或C端账号
     * @return
     * @author: liuyang
     * @link https://openapi.bestsign.cn/#/dev/doc/2385375908677550087
     * @Date: 2021/8/16 17:19
     */
    private static Boolean getCert(String account) {
        String method = "/user/getCert/";

        // 组装请求参数，作为requestbody
        JSONObject requestBody = new JSONObject();
        requestBody.put("account", account);

        log.info("getCert-----requestBody:" + requestBody.toJSONString());
        // 返回结果解析
        JSONObject result = request(method, requestBody);
        log.info("getCert-----result:" + result.toJSONString());
        // 返回errno为0，表示成功，其他表示失败
        if (result.getIntValue("errno") == 0) {
            return true;
        } else {
            log.info("getCert-----异常：" + result.getIntValue("errno") + ":"
                    + result.getString("errmsg"));
            return false;
        }
    }

    /**
     * GET方法示例
     * 下载合同PDF文件
     *
     * @param contractId 合同编号
     * @return
     * @throws Exception
     */
    public static byte[] contractDownload(String contractId) {
        String method = "/storage/contract/download/";

        // 组装url参数
        String urlParams = "contractId=" + contractId;

        // 生成一个时间戳参数
        String rtick = RSAUtils.getRtick();
        // 计算参数签名
        String paramsSign = RSAUtils.calcRsaSign(developerId,
                privateKey, serverHost, method, rtick, urlParams, null);
        // 签名参数追加为url参数
        urlParams = String.format(URL_SIGN_PARAMS, developerId, rtick,
                paramsSign) + "&" + urlParams;
        // 发送请求
        byte[] responseBody = HttpClientSender.sendHttpGet(serverHost, method,
                urlParams);
        // 返回结果解析
        return responseBody;
    }
    public static void signContractByTemplate(String contractId, String bAccount, List<String> stampVars, String bestSignTid) {
        String method = "/contract/sign/template/";

        // 组装请求参数，作为requestbody
        JSONObject requestBody = new JSONObject();
        requestBody.put("contractId", contractId);
        requestBody.put("tid", bestSignTid);

        JSONObject vars = new JSONObject();
        JSONObject account = new JSONObject();
        account.put("account", bAccount);
        if (CollectionUtils.isEmpty(stampVars)) {
            vars.put("stamp", account);
        } else {
            for (String stampVar : stampVars) {
                vars.put(stampVar, account);
            }
        }
        requestBody.put("vars", vars);
        log.info("signContractByTemplate-----requestBody:" + requestBody.toJSONString());
        // 返回结果解析
        JSONObject result = request(method, requestBody);
        log.info("signContractByTemplate-----result:" + result.toJSONString());
        // 返回errno为0，表示成功，其他表示失败
        if (result.getIntValue("errno") == 0) {
            return;
        } else {
            log.info("signContractByTemplate异常：" + result.getIntValue("errno") + ":"
                    + result.getString("errmsg"));
            throw GrpcException.asRuntimeException(BEST_SIGN_ERR_PREFIX + result.getString("errno") + result.getString("errmsg"));
        }
    }


    public static void finishContract(String contractId) {
        String method = "/storage/contract/lock/";

        // 组装请求参数，作为requestbody
        JSONObject requestBody = new JSONObject();
        requestBody.put("contractId", contractId);
        log.info("finishContract-----requestBody:" + requestBody.toJSONString());
        // 返回结果解析
        JSONObject result = request(method, requestBody);
        log.info("finishContract-----result:" + result.toJSONString());
        // 返回errno为0，表示成功，其他表示失败
        if (result.getIntValue("errno") == 0) {
            return;
        } else {
            log.info("finishContract异常：" + result.getIntValue("errno") + ":"
                    + result.getString("errmsg"));
            throw GrpcException.asRuntimeException(BEST_SIGN_ERR_PREFIX + result.getString("errno") + result.getString("errmsg"));
        }
    }

    public static void sealAcrossPage(String contractId, String bAccount) {
        String method = "/storage/contract/cert/paging/seal/sign/";

        // 组装请求参数，作为requestbody
        JSONObject requestBody = new JSONObject();
        requestBody.put("contractId", contractId);
        requestBody.put("signer", bAccount);
        requestBody.put("signY", "0.45");
        log.info("sealAcrossPage-----requestBody:" + requestBody.toJSONString());
        // 返回结果解析
        JSONObject result = request(method, requestBody);
        log.info("sealAcrossPage-----result:" + result.toJSONString());
        // 返回errno为0，表示成功，其他表示失败
        if (result.getIntValue("errno") == 0) {
            return;
        } else {
            log.info("sealAcrossPage异常：" + result.getIntValue("errno") + ":"
                    + result.getString("errmsg"));
            throw GrpcException.asRuntimeException(BEST_SIGN_ERR_PREFIX + result.getString("errno") + result.getString("errmsg"));
        }
    }


    private static JSONObject request(String method, JSONObject requestBody) {
        // 生成一个时间戳参数
        String rtick = RSAUtils.getRtick();
        // 计算参数签名
        String paramsSign = RSAUtils.calcRsaSign(developerId,
                privateKey, serverHost, method, rtick, null,
                requestBody.toJSONString());
        // 签名参数追加为url参数
        String urlParams = String.format(URL_SIGN_PARAMS, developerId,
                rtick, paramsSign);
        // 发送请求
        String responseBody = HttpClientSender.sendHttpPost(serverHost, method,
                urlParams, requestBody.toJSONString());
        // 返回结果解析
        JSONObject result = JSON.parseObject(responseBody);
        return result;
    }


}
