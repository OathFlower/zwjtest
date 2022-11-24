package cn.xunhou.xbbcloud.common.utils;

import cn.xunhou.common.tools.util.ZKClientHelper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 读取lietou库sys_param表数据
 *
 * @author yuanxl
 * @changelog 改为赖加载
 * @changelog 改为从zk加载数据库私钥
 * @date 2015-6-26 下午03:51:20
 */
public class ParamUtil {

    private static ParamUtil instance = new ParamUtil();

    // 数据来源zk
    private volatile ConcurrentMap<String, String> desKeyInZk = new ConcurrentHashMap<String, String>();

    private static final String ZK_PATH = "/security/db";

    private ParamUtil() {

    }

    public static ParamUtil getInstance() {
        return instance;
    }

    private synchronized void loadInZk() {
        Map<String, Object> data = ZKClientHelper.getData4Map("/config/public" + ZK_PATH);
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                desKeyInZk.put(entry.getKey(), (String) entry.getValue());
            }
        }
    }

    /**
     * 从zk里加载私钥数据
     *
     * @param key
     * @return
     */
    public String getStringValue(String key) {
        // 先从zk读取
        if (desKeyInZk.isEmpty()) {
            synchronized (this) {
                if (desKeyInZk.isEmpty()) {
                    loadInZk();
                }
            }
        }
        String value = desKeyInZk.get(key);

        if (value == null) {
            throw new RuntimeException("security db key=" + key + " is not in the ZK");
        }

        return value;
    }


}
