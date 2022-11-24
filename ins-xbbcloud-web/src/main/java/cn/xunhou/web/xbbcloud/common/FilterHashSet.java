package cn.xunhou.web.xbbcloud.common;

import cn.hutool.core.lang.Filter;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;

/**
 * 有过滤元素能力的HashSet
 *
 * @author sha.li
 * @since 2022-09-29 11:31
 */
public class FilterHashSet<T> extends HashSet<T> {

    @Setter
    private Filter<T> filter;

    public FilterHashSet() {
    }

    public FilterHashSet(Filter<T> filter) {
        this.filter = filter;
    }

    public FilterHashSet(Collection<? extends T> c) {
        super(c);
    }

    public FilterHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public FilterHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public boolean add(T t) {
        if (filter != null && !filter.accept(t)) {
            return false;
        }
        return super.add(t);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
