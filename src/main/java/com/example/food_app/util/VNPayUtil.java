package com.example.food_app.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class VNPayUtil {

    public static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA512"));
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hash error");
        }
    }

    public static String buildQuery(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder query = new StringBuilder();

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (value != null && !value.isEmpty()) {
                query.append(URLEncoder.encode(key, StandardCharsets.US_ASCII))
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));

                if (i < keys.size() - 1) {
                    query.append("&");
                }
            }
        }
        return query.toString();
    }
}