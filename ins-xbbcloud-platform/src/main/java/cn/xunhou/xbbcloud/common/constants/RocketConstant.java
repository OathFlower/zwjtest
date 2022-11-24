package cn.xunhou.xbbcloud.common.constants;

public class RocketConstant {

    public static final String APPLICATION_NAME = "ins-xbbcloud-platform";

    /**
     * 发送结算考勤打卡账单
     */
    public static final String ATTENDANCE_BILL_TAG = "ATTENDANCE_BILL_TAG";
    public static final String ATTENDANCE_BILL_TAG_SHARDING_KEY = "ATTENDANCE_BILL_TAG_SHARDING_KEY";

    /**
     * 结算审核通过返回结果tag
     */
    public static final String SETTLEMENT_SAAS_TENANT_SALARY = "SETTLEMENT_SAAS_TENANT_SALARY";

    /**
     * 薪酬云-资金调度节点消息
     */
    public static final String XCY_FUND_DISPATCHING_MSG_TAG = "XCY_FUND_DISPATCHING_MSG_TAG";

    /**
     * 薪酬云-支付中心-招行流水
     */
    public static final String ASSET_CMB_BILL_FLOW_TAG = "ASSET_CMB_BILL_FLOW_TAG";

    public static final String WORKFLOW_PASS_TAG = "WORKFLOW_PASS_TAG";
    public static final String WORKFLOW_ADD_TAG = "WORKFLOW_ADD_TAG";
    public static final String WORKFLOW_REJECT_TAG = "WORKFLOW_REJECT_TAG";
    public static final String WORKFLOW_UPDATE_TAG = "WORKFLOW_UPDATE_TAG";

    public static final String WORKFLOW_SEND_MESSAGE_TAG = "WORKFLOW_SEND_MESSAGE_TAG";
    //轮询调度完成状态
    public static final String WAIT_FUND_DISPATCHING = "WAIT_FUND_DISPATCHING";
    //推送到余额
    public static final String SEND_TO_PAYROLL_TAG = "SEND_TO_PAYROLL_TAG";
    //提现完成
    public static final String XBB_WITHDRAWL_RESULT_TAG = "XBB_WITHDRAWL_RESULT_TAG";

    /**
     * 发薪撤回 -> XBB
     */
    public static final String XCY2XBB_BACK_DETAIL_TAG = "XCY2XBB_BACK_DETAIL_TAG";
}
