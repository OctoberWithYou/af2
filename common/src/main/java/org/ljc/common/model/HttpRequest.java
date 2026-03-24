package org.ljc.common.model;

import java.util.Map;

/**
 * HTTP请求封装实体
 * 用于在Agent与Server之间传输HTTP请求
 */
public class HttpRequest {
    /**
     * 请求方法: GET, POST, PUT, DELETE, etc.
     */
    private String method;

    /**
     * 完整URL
     */
    private String url;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 查询参数
     */
    private Map<String, String> queryParams;

    /**
     * 请求头
     */
    private Map<String, String> headers;

    /**
     * 请求体
     */
    private String body;

    /**
     * 请求编码
     */
    private String charset;

    /**
     * Content-Type
     */
    private String contentType;

    /**
     * 请求超时(毫秒)
     */
    private int timeout;

    // Getters and Setters

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
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

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}