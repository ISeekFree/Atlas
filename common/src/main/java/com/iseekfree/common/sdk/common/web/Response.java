package com.iseekfree.common.sdk.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The unified {@code {code,msg,data}} envelope. {@code null} members are omitted
 * from the serialized JSON (a failure carries no {@code data}, a success never
 * needs a {@code null} {@code msg}), so clients never see {@code "data":null}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

    private int code;
    private String msg;
    private T data;

    public Response() {
    }

    public Response(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(0, "success", data);
    }

    public static <T> Response<T> failure(int code, String msg) {
        return new Response<>(code, msg, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
