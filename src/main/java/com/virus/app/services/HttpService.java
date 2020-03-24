package com.virus.app.services;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import static com.virus.app.constants.ApplicationConstants.EMPTY_STRING;

@Service
public class HttpService {

    public String doGet(String url, HashMap<String, String> headers, HashMap<String, Object> queryParams) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String resp = EMPTY_STRING;
        try {
            StringBuffer params = new StringBuffer(EMPTY_STRING);
            if (Objects.nonNull(queryParams) && !queryParams.isEmpty()) {
                queryParams.forEach((k, v) -> params.append(k).append("=").append(v).append("&"));
            }
            if (!params.toString().equals(EMPTY_STRING)) {
                String queryParamsFinal = params.toString().substring(0, params.toString().length() - 1);
                url = url + "?" + queryParamsFinal;
            }
            HttpGet httpGet = new HttpGet(url);
            if (Objects.nonNull(headers) && !headers.isEmpty()) {
                headers.forEach(httpGet::addHeader);
            }
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Failed with HTTP error code: " + httpResponse.getStatusLine().getStatusCode());
            }
            resp = EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resp;
    }
}
