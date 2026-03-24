package org.ljc.common.model;

import java.util.Map;

/**
 * HTTP响应封装实体
 * 用于在Agent与Server之间传输HTTP响应
 */
public class HttpResponse {
    /**
     * HTTP状态码
     */
    private int statusCode;

    /**
     * 状态描述
     */
    private String statusMessage;

    /**
     * 响应头
     */
    private Map<String, String> headers;

    /**
     * 响应体
     */
    private String body;

    /**
     * Content-Type
     */
    private String contentType;

    /**
     * 响应编码
     */
    private String charset;

    /**
     * 响应时间(毫秒)
     */
    private long responseTime;

    /**
     * 默认构造函数
     */
    public HttpResponse() {
    }

    // Getters and Setters

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    /**
     * 判断是否成功响应 (2xx)
     */
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
}