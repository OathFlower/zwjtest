package cn.xunhou.xbbcloud.common.bean;


import cn.xunhou.grpc.proto.subject.SubjectServiceProto;
import cn.xunhou.xbbcloud.common.annotation.TemplateParam;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.ToString;

import java.lang.reflect.Field;
import java.util.List;

@ToString
@Data
public class TemplateBean {

    /**
     * 甲方签署日期
     */

    private String firstPartySignDate;
    /**
     * 乙方签署日期
     */

    private String secondPartySignDate;
    /**
     * 签署日期
     */

    private String lastSignDate;


    /**
     * 甲方
     */

    private String firstParty;
    /**
     * 甲方地址
     */

    private String firstPartyAddress;
    /**
     * 乙方
     */

    private String secondParty;
    /**
     * 性别
     */

    private String sex;
    /**
     * 籍贯
     */

    private String nativePlace;
    /**
     * 身份证号码
     */

    private String idCardNo;
    /**
     * 乙方住址
     */

    private String secondPartyAddress;
    /**
     * 联系电话
     */

    private String mobile;

    /**
     * 紧急联系人姓名
     */

    private String emergencyLinkmanName;
    /**
     * 紧急联系人电话
     */

    private String emergencyLinkmanMobile;
    /**
     * 工资卡开户行
     */

    private String payrollCardBank;
    /**
     * 工资卡开户城市
     */

    private String payrollCardCity;
    /**
     * 工资卡账号
     */

    private String payrollCardNo;
    /**
     * 劳动合同开始时间
     */

    private String contractStartTime;
    /**
     * 劳动合同结束时间
     */

    private String contractEndTime;
    /**
     * 试用期
     */

    private String probationPeriod;
    /**
     * 工作岗位
     */

    private String workPost;
    /**
     * 薪资
     */

    private String salary;
    /**
     * 发薪日
     */

    private String salaryDay;
    /**
     * 试用期开始时间
     */

    private String probationStartTime;
    /**
     * 试用期结束时间
     */

    private String probationEndTime;
    /**
     * 工作城市
     */

    private String workPlaceCity;
    /**
     * 试用期薪资
     */

    private String probationSalary;

    /**
     * 薪资补充
     */

    private String salarySupply;

    /**
     * 薪资描述
     */

    private String salaryDesc;
    /**
     * 法人代表
     */

    private String firstPartyLegalRepresentative;

    /**
     * 工时制度
     */

    private String workingHourSystem;

    /**
     * 甲方联系方式
     */

    private String firstPartyContactPhone;

    /**
     * 乙方公司名称
     */

    private String secondPartyCompanyName;

    /**
     * 学校
     */

    private String school;

    /**
     * 专业
     */

    private String profession;


    private String companyName;


    private String other;

    public JSONObject toParamsMap(List<String> paramList) {
        JSONObject jsonObject = new JSONObject();
        Field[] fields = TemplateBean.class.getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                TemplateParam templateParam = field.getAnnotation(TemplateParam.class);
                if (templateParam == null) {
                    continue;
                }
                if (paramList.contains(field.getName())) {
                    jsonObject.put(field.getName(), field.get(this) != null ? field.get(this).toString() : null);
                }
            }
        } catch (Exception e) {
            throw GrpcException.asRuntimeException("TemplateBean转换异常" + e.getMessage());
        }
        return jsonObject;
    }

    public void setDefaultSecondPartyInfo(String realName, String tel, String idCardNo) {

        this.setSecondParty(realName);
        this.setMobile(tel);
        this.setIdCardNo(idCardNo);

    }

    public void setDefaultFirstPartyInfo(SubjectServiceProto.SubjectDetailBeResponse subjectDetailBeResponse) {
        if (null != subjectDetailBeResponse) {
            this.setFirstParty(subjectDetailBeResponse.getSubjectName());//设置甲方
            this.setFirstPartyAddress(subjectDetailBeResponse.getSubjectAddress());//设置甲方地址
        }
    }

}
