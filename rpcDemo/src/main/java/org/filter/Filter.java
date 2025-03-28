package org.filter;

public interface Filter<T> {

    FilterResponse doFilter(FilterData<T> filterData);
}
