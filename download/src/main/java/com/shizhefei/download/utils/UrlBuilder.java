package com.shizhefei.download.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class UrlBuilder {
    private String url;
    private LinkedList<String> paths = new LinkedList<>();
    private LinkedHashMap<String, List<?>> params = new LinkedHashMap<>();

    public UrlBuilder(String url) {
        super();
        this.url = url;
    }

    public UrlBuilder sp(Object path) {
        paths.add(String.valueOf(path));
        return this;
    }

    public UrlBuilder param(String param, String value) {
        return paramImp(param, value);
    }

    public UrlBuilder param(String param, int value) {
        return paramImp(param, value);
    }

    public UrlBuilder param(String param, boolean value) {
        return paramImp(param, value);
    }

    public UrlBuilder param(String param, double value) {
        return paramImp(param, value);
    }

    public UrlBuilder param(String param, float value) {
        return paramImp(param, value);
    }

    public UrlBuilder param(String param, byte value) {
        return paramImp(param, value);
    }

    public UrlBuilder param(String param, long value) {
        return paramImp(param, value);
    }

    public UrlBuilder paramImp(String param, Object value) {
        List<Object> values = (List<Object>) params.get(param);
        if (values == null) {
            values = new ArrayList<>();
            params.put(param, values);
        }
        values.add(value);
        return this;
    }

    public UrlBuilder params(Map<String, List<?>> p) {
        params.putAll(p);
        return this;
    }


    public UrlBuilder param(String key, List<?> values) {
        params.put(key, values);
        return this;
    }

    public String build() {
        StringBuilder builder = new StringBuilder(url);
        if (builder.length() > 0 && builder.charAt(builder.length() - 1) == '/') {
            builder.deleteCharAt(builder.length() - 1);
        }
        for (String path : paths) {
            builder.append("/").append(path);
        }
        if (!params.isEmpty()) {
            if (builder.indexOf("?") < 0)
                builder.append('?');
            for (Entry<String, List<?>> entry : params.entrySet()) {
                // url.append(String.valueOf(entry.getValue()));
                // 不做URLEncoder处理
                List<?> values = entry.getValue();
                for (Object value : values) {
                    builder.append('&');
                    builder.append(entry.getKey());
                    builder.append('=');
                    try {
                        builder.append(URLEncoder.encode(String.valueOf(value), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return builder.toString().replace("?&", "?");
    }
}
