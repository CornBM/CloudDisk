package com.train.cloudDisk.tool;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class QueryStringUtil {

    public static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = queryString.split("&"); // 分割字符串为键值对数组

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2); // 分割每个键值对
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8); // 解码URL编码的值
                params.put(key, value);
            }
        }
        return params;
    }
}
