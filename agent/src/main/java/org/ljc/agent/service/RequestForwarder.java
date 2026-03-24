package org.ljc.agent.service;

import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.ljc.agent.config.AgentConfig;
import org.ljc.common.model.HttpRequest;
import org.ljc.common.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 请求转发服务
 * 负责将收到的请求转发到内网AI模型
 */
public class RequestForwarder {
    private static final Logger logger = LoggerFactory.getLogger(RequestForwarder.class);

    private final AgentConfig agentConfig;
    private final CloseableHttpClient httpClient;

    public RequestForwarder(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;

        // 创建HTTP客户端
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(Timeout.of(agentConfig.getConnectTimeout(), TimeUnit.MILLISECONDS))
            .setResponseTimeout(Timeout.of(60, TimeUnit.SECONDS))
            .build();

        this.httpClient = HttpClients.custom()
            .setDefaultRequestConfig(config)
            .build();
    }

    /**
     * 转发HTTP请求到内网AI模型
     *
     * @param request HTTP请求
     * @return HTTP响应
     */
    public HttpResponse forward(HttpRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 创建HTTP方法
            HttpUriRequestBase httpRequest = createHttpRequest(request);

            // 添加请求头
            if (request.getHeaders() != null) {
                for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                    httpRequest.addHeader(entry.getKey(), entry.getValue());
                }
            }

            // 添加请求体
            if (request.getBody() != null && !request.getBody().isEmpty()) {
                String charset = request.getCharset() != null ? request.getCharset() : "UTF-8";
                StringEntity entity = new StringEntity(request.getBody(),
                    ContentType.create(request.getContentType() != null ? request.getContentType() : "application/json", charset));
                httpRequest.setEntity(entity);
            }

            // 发送请求
            try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
                return buildResponse(response, System.currentTimeMillis() - startTime);
            }

        } catch (Exception e) {
            logger.error("Failed to forward request to: {}", request.getUrl(), e);
            return createErrorResponse(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 创建HTTP请求对象
     */
    private HttpUriRequestBase createHttpRequest(HttpRequest request) {
        String method = request.getMethod().toUpperCase();

        switch (method) {
            case "GET":
                return new HttpGet(request.getUrl());
            case "POST":
                return new HttpPost(request.getUrl());
            case "PUT":
                return new HttpPut(request.getUrl());
            case "DELETE":
                return new HttpDelete(request.getUrl());
            case "PATCH":
                return new HttpPatch(request.getUrl());
            case "HEAD":
                return new HttpHead(request.getUrl());
            case "OPTIONS":
                return new HttpOptions(request.getUrl());
            default:
                return new HttpPost(request.getUrl());
        }
    }

    /**
     * 构建响应对象
     */
    private HttpResponse buildResponse(CloseableHttpResponse response, long responseTime)
        throws Exception {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(response.getCode());
        httpResponse.setStatusMessage(response.getReasonPhrase() != null ?
            response.getReasonPhrase() : "");
        httpResponse.setResponseTime(responseTime);

        // 响应头
        Map<String, String> headers = new HashMap<>();
        Header[] responseHeaders = response.getHeaders();
        for (Header header : responseHeaders) {
            headers.put(header.getName(), header.getValue());
        }
        httpResponse.setHeaders(headers);

        // Content-Type
        Header contentType = response.getFirstHeader("Content-Type");
        if (contentType != null) {
            httpResponse.setContentType(contentType.getValue());
            // 提取charset
            String ct = contentType.getValue();
            if (ct.contains("charset=")) {
                httpResponse.setCharset(ct.substring(ct.indexOf("charset=") + 8));
            }
        }

        // 响应体
        if (response.getEntity() != null) {
            String body = EntityUtils.toString(response.getEntity());
            httpResponse.setBody(body);
        }

        return httpResponse;
    }

    /**
     * 创建错误响应
     */
    private HttpResponse createErrorResponse(String errorMessage, long responseTime) {
        HttpResponse response = new HttpResponse();
        response.setStatusCode(500);
        response.setStatusMessage("Internal Server Error");
        response.setBody(errorMessage);
        response.setResponseTime(responseTime);
        return response;
    }

    /**
     * 关闭HTTP客户端
     */
    public void close() {
        try {
            httpClient.close();
        } catch (Exception e) {
            logger.error("Failed to close HTTP client", e);
        }
    }
}