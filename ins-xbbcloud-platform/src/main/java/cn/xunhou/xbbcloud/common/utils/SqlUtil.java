package cn.xunhou.xbbcloud.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.xbbcloud.common.constants.CommonConst;
import cn.xunhou.xbbcloud.config.XhThreadPoolConfiguration;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

@Slf4j
public class SqlUtil {
    /**
     * @param page     0开始
     * @param pageSize 页码大小
     * @return
     */
    public static String buildLimit(Integer page, Integer pageSize) {
        if (page == null || page < CommonConst.ZERO) {
            page = CommonConst.ZERO;
        }
        if (pageSize == null || pageSize < CommonConst.ONE) {
            pageSize = CommonConst.ONE;
        }
        return " limit " + ((NumberUtil.mul(page, pageSize).intValue()) + "," + pageSize);
    }

    /**
     * 作成count语句
     *
     * @param sql sql
     * @return count sql
     */
    public static String buildCount(String sql) {
        return "select count(*) countValue from (" + sql + ") count_value_table";
    }

    /**
     * 统计
     *
     * @param jdbcTemplate 查询模板
     * @param sql          sql
     * @param param        参数
     * @return 总数
     */
    public static Integer countForSql(NamedParameterJdbcTemplate jdbcTemplate, String sql, Object param) {
        SqlParameterSource sqlParameterSource = param == null ? new EmptySqlParameterSource() : new BeanPropertySqlParameterSource(param);
        Map<String, Object> countMap = jdbcTemplate.queryForMap(SqlUtil.buildCount(sql), sqlParameterSource);
        return MapUtil.getInt(countMap, "countValue");
    }


    /**
     * 分页查询
     * 当page或者pageSize其中一个为null时，则查所有
     *
     * @param jdbcTemplate 查询模板
     * @param sql          sql
     * @param param        参数
     * @param resultType   返回对象类型
     * @param page         页码  为 null 查所有
     * @param pageSize     单页大小 为 null 查所有
     * @param <T>          返回对象类型
     * @return 查询结果
     */
    @NonNull
    public static <T> PagePojoList<T> pagePojoList(NamedParameterJdbcTemplate jdbcTemplate, String sql, Object param, Class<T> resultType, Integer page, Integer pageSize) {
        PagePojoList.PagePojoListBuilder<T> builder = PagePojoList.<T>builder();
        boolean all = false;
        if (ObjectUtil.hasNull(page, pageSize) || (pageSize != null && pageSize == 0)) {
            all = true;
        }
        List<T> resultList;
        if (all) {
            resultList = queryList(jdbcTemplate, sql, param, resultType);
            builder.data(resultList);
            builder.total(resultList.size());
        } else {
            builder.total(countForSql(jdbcTemplate, sql, param));
            resultList = pageList(jdbcTemplate, sql, param, resultType, page, pageSize);
            builder.data(resultList);
        }
        return builder.build();
    }

    /**
     * 查询单页数据
     *
     * @param jdbcTemplate 查询模板
     * @param sql          sql
     * @param param        参数
     * @param resultType   返回对象类型
     * @param page         页码 为 null 查所有
     * @param pageSize     单页大小 为 null 查所有
     * @param <T>          返回对象类型
     * @return 返回结果
     */
    @NonNull
    public static <T> List<T> pageList(NamedParameterJdbcTemplate jdbcTemplate, String sql, Object param, Class<T> resultType, Integer page, Integer pageSize) {
        SqlParameterSource parameterSource = param == null ? new EmptySqlParameterSource() : new BeanPropertySqlParameterSource(param);
        RowMapper<T> rowMapper = new BeanPropertyRowMapper<>(resultType);
        sql = sql + buildLimit(page, pageSize);
        return jdbcTemplate.query(sql, parameterSource, rowMapper);
    }

    /**
     * 查询所有
     *
     * @param jdbcTemplate 查询模板
     * @param sql          sql
     * @param param        参数
     * @param resultType   返回对象类型
     * @param <T>          返回对象类型
     * @return 返回结果
     */
    public static <T> List<T> queryList(NamedParameterJdbcTemplate jdbcTemplate, String sql, Object param, Class<T> resultType) {
        SqlParameterSource parameterSource = null;
        if (param instanceof Map) {
            parameterSource = new MapSqlParameterSource((Map) param);
        } else {
            parameterSource = new BeanPropertySqlParameterSource(param);
        }
        RowMapper<T> rowMapper = new BeanPropertyRowMapper<>(resultType);
        return jdbcTemplate.query(sql, parameterSource, rowMapper);
    }


    /**
     * 分页查询
     *
     * @param jdbcTemplate 查询模板
     * @param sql          sql
     * @param param        参数
     * @param resultType   返回对象类型
     * @param <T>          返回对象类型
     * @return 查询结果
     */
    public static <T> PagePojoList<T> pagePojoList(NamedParameterJdbcTemplate jdbcTemplate, String sql, Object param, Class<T> resultType) {
        return pagePojoList(jdbcTemplate, sql, param, resultType, null, null);
    }

    /**
     * 通过id查询
     *
     * @param jdbcTemplate 查询模板
     * @param sql          sql
     * @param resultType   返回对象类型
     * @param ids          入参ids
     * @param filed        对应sql字段
     * @param <T>          返回对象类型
     * @return 查询结果
     */
    public static <T> List<T> findByIds(@NonNull NamedParameterJdbcTemplate jdbcTemplate, @NonNull String sql, @NonNull Class<T> resultType, Collection<?> ids, String filed) {
        Map<String, Object> param = Maps.newHashMap();
        param.put(filed, ids);
        return jdbcTemplate.query(sql, param, new BeanPropertyRowMapper<>(resultType));
    }

    /**
     * /**
     * 通过id查询
     *
     * @param jdbcTemplate 查询模板
     * @param sql          sql
     * @param resultType   返回对象类型
     * @param id           入参id
     * @param filed        对应sql字段
     * @param <T>          返回对象类型
     * @return 查询结果
     */
    public static <T> T findById(@NonNull NamedParameterJdbcTemplate jdbcTemplate, @NonNull String sql, @NonNull Class<T> resultType, Object id, String filed) {
        List<T> list = findByIds(jdbcTemplate, sql, resultType, Collections.singletonList(id), filed);
        if (list.size() > CommonConst.ONE) {
            throw new RuntimeException("id查询结果大于1");
        }
        return CollUtil.getFirst(list);
    }

    public static <T> List<T> queryThreadPage(@NonNull NamedParameterJdbcTemplate jdbcTemplate, @NonNull String sql, Object param, @NonNull Class<T> resultType, Integer pageSize) {
        ThreadPageQuery<T> pageQuery = new ThreadPageQuery<T>() {
            @Override
            public Executor getExecutorService() {
                return SpringContextUtil.getBean(XhThreadPoolConfiguration.LAZY_TRACE_EXECUTOR);
            }

            @Override
            public Integer total() {
                return SqlUtil.countForSql(jdbcTemplate, sql, param);
            }

            @Override
            public Integer pageSize() {
                return pageSize;
            }

            @Override
            public List<T> pageList(Integer page) {
                return SqlUtil.pageList(jdbcTemplate, sql, param, resultType, page, pageSize());
            }
        };
        return pageQuery.queryByThread();
    }

    public interface ThreadPageQuery<T> {
        /**
         * 获取线程池
         *
         * @return 返回线程池
         */
        Executor getExecutorService();

        /**
         * 查询总数
         *
         * @return
         */
        Integer total();

        /**
         * 每页条数
         */
        Integer pageSize();

        /**
         * 单页查询处理
         *
         * @param page 页码
         * @return 单页查询结果
         */
        List<T> pageList(Integer page);

        @SneakyThrows
        default List<T> queryByThread() {
            Integer pageSize = pageSize();
            Integer total = total();
            log.info("total = {}", total);
            if (total == null || total <= 0) {
                return Collections.emptyList();
            }
            Executor executor = getExecutorService();
            List<FutureTask<List<T>>> futures = new ArrayList<>();
            //总页数
            int totalPage = (total + pageSize - 1) / pageSize;
            for (int page = 0; page < totalPage; page++) {
                int finalPage = page;
                // 等凉菜
                Callable<List<T>> callable = () -> {
                    try {
                        //这里做处理信息的方法
                        List<T> list = pageList(finalPage);
                        //查询的结果如何保存下来，会不会存在覆盖的问题
                        log.info("每次查询的下标:" + finalPage + ",条数:" + pageSize + ", list =" + JSONUtil.toJsonStr(list));
                        return list;
                    } catch (Exception e) {
                        log.error("查询异常！", e);
                        return Collections.emptyList();
                    }
                };
                FutureTask<List<T>> future = new FutureTask<>(callable);
                executor.execute(future);
                futures.add(future);
            }
            List<T> ret = Lists.newArrayList();
            for (int i = 0; i < futures.size(); i++) {
                ret.addAll(futures.get(i).get());
            }
            // 执行完关闭线程池
//            executor.shutdown();
            return ret;
        }
    }
}
