package com.shizhefei.download.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DownloadJsonUtils {

    public static JSONObject toJsonObject(Map<String, List<String>> map) {
        return new JSONObject(map);
    }

    public static JSONObject toJsonObject2(Map<String, String> map) {
        return new JSONObject(map);
    }

    public static Map<String, List<String>> parse(JSONObject jsonObject) {
        Map<String, List<String>> map = new HashMap<>();
        try {
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONArray jsonArray = jsonObject.optJSONArray(key);
                int length = jsonArray.length();
                List<String> values = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    String value = jsonArray.getString(i);
                    values.add(value);
                }
                map.put(key, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static Map<String, String> parse2(JSONObject jsonObject) {
        Map<String, String> map = new HashMap<>();
        try {
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = jsonObject.optString(key);
                map.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
