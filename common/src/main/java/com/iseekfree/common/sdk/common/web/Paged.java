package com.iseekfree.common.sdk.common.web;

import java.util.List;

public class Paged<T> {

    private long total;
    private List<T> items;

    public Paged() {
    }

    public Paged(long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

    public static <T> Paged<T> of(long total, List<T> items) {
        return new Paged<>(total, items);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
