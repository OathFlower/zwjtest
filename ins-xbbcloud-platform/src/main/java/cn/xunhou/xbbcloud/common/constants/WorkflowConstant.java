package cn.xunhou.xbbcloud.common.constants;

/**
 * @Author: chenning
 * @Date: 2022/09/29/10:14
 * @Description:
 */
public class WorkflowConstant {

    private WorkflowConstant(){}

    public static final Integer APPROVER = 1;
    public static final Integer CC = 2;
    public static final Integer TRANSACTOR = 3;
    public static final Integer CONDITION = 4;

    public static final Integer WECHAT = 1;
    public static final Integer DINGDING = 0;
    public static final Integer SMS = 2;

    public static final Integer PERSON = 1;
    public static final Integer AUTO_PASS = 2;
    public static final Integer AUTO_REJECT = 3;


    public static final String  SMS_AGENT_JOIN_ADD_CODE = "T00049";
    public static final String  SMS_AGENT_JOIN_PASS_CODE = "T00050";
    public static final String  SMS_AGENT_JOIN_REJECT_CODE = "T00051";
    public static final String  SMS_ACCOUNT_ADD_CODE = "T00054";
    public static final String  SMS_ACCOUNT_MODIFY_CODE = "T00055";
    public static final String  SMS_ACCOUNT_PASS_CODE = "T00056";
    public static final String  SMS_ACCOUNT_REJECT_CODE = "T00057";
    public static final String  SMS_BANK_ACCOUNT_ADD_CODE = "T00058";
    public static final String  SMS_BANK_ACCOUNT_MODIFY_CODE = "T00059";
    public static final String  SMS_BANK_ACCOUNT_PASS_CODE = "T00060";
    public static final String  SMS_BANK_ACCOUNT_REJECT_CODE = "T00061";
    public static final String  SMS_PROJECT_ADD_CODE = "T00062";
    public static final String  SMS_PROJECT_REJECT_CODE = "T00063";
    public static final String  SMS_PROJECT_PASS_CODE = "T00064";

    public static final Integer PRODUCT_ID = 12001;
    // 权限码
    public static final int  ASSIGNEE_TYPE_CODE = 1;

    public static final long SYSTEM_HANDLE_ID = 1000000L;

}
