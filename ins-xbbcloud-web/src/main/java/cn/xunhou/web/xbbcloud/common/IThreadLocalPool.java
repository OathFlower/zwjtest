package cn.xunhou.web.xbbcloud.common;

/**
 * 线程变量池
 *
 * @author sha.li
 * @since 2022/9/16
 */
public interface IThreadLocalPool {
    /**
     * 清空ThreadLocal中的数据
     */
    void remove();
}
