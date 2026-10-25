package com.smt.refactor.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * HTTP 工具（翻译自老系统 com.smt.utils.HttpUtils）
 * 仅保留用户管理/坐席签入校验用到的 httpFormRequest
 */
@Slf4j
public class HttpUtils {

    public static byte[] httpFormRequest(String url, Map<String, String> reqParams, boolean isPostMethod,
                                         int contentLength, int connectTimeout, int readTimeout) {
        StringBuilder sign = new StringBuilder();
        if (reqParams != null) {
            List<String> keys = new ArrayList<>(reqParams.keySet());
            Collections.sort(keys);
            int i = 0;
            for (String key : keys) {
                if (i++ != 0) {
                    sign.append("&");
                }
                sign.append(key).append("=");
                try {
                    sign.append(URLEncoder.encode(reqParams.get(key), "UTF-8"));
                } catch (Exception ignored) {
                }
            }
        }

        HttpURLConnection conn = null;
        InputStream ins = null;
        byte[] fileContent = null;
        try {
            if (isPostMethod) {
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setUseCaches(false);
                conn.setConnectTimeout(connectTimeout);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                if (contentLength != -1) {
                    conn.setFixedLengthStreamingMode(contentLength);
                }
                conn.getOutputStream().write(sign.toString().getBytes());
            } else {
                conn = (HttpURLConnection) new URL(url + "?" + sign).openConnection();
                conn.setUseCaches(false);
                conn.setConnectTimeout(connectTimeout);
                conn.setRequestMethod("GET");
            }
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                conn.disconnect();
                throw new Exception(url + " responseCode:" + conn.getResponseCode());
            }
            int readed;
            int hasWrite = 0;
            int contentLength2 = conn.getContentLength();
            ins = conn.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 14];
            while ((readed = ins.read(buffer, 0, buffer.length)) > 0) {
                bos.write(buffer, 0, readed);
                hasWrite += readed;
            }
            fileContent = bos.toByteArray();
            bos.close();
            if (0 == hasWrite) {
                log.info("httpFormRequest has_write == 0 !");
            }
        } catch (Exception e) {
            if (ins != null) {
                try {
                    ins.close();
                } catch (Exception ignored) {
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
            log.info(e.getMessage());
        }
        return fileContent;
    }
}
