package com.mitek.tree.util;

import com.mitek.tree.config.Constants;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpConnectionClient {
    private static final Logger logger = LoggerFactory.getLogger(HttpConnectionClient.class);

    public CloseableHttpClient getHttpClient() {
        return buildDefaultClient();
    }

    public CloseableHttpClient buildDefaultClient() {
        logger.debug("requesting http client connection client open");
        Integer timeout = Constants.REQUEST_TIMEOUT;
        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000).setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        return clientBuilder.setDefaultRequestConfig(config).build();
    }

    public HttpPost createPostRequest(String url) {
        return new HttpPost(url);
    }
}
