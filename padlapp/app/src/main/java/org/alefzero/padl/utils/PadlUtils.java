package org.alefzero.padl.utils;

import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class PadlUtils {


    public synchronized static Map<String,String> split(Collection<String> data) {
        return PadlUtils.split(data, false);
    }

    public synchronized static Map<String,String> split(Collection<String> data, boolean invertKeys) {
        Map<String,String> items = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
        if (null != data) {
            for (String attribute : data) {
                StringTokenizer stEqual = new StringTokenizer(attribute, "=");
                String key = stEqual.nextToken().trim();
                String value = stEqual.countTokens() == 0 ? key : stEqual.nextToken().trim();
                if (! invertKeys) {
                    items.put(key, value);
                } else {
                    items.put(value, key);
                }
            }
        }
        return items;
    }

    public static String safeToLower(String string) {
        String data = string != null ? string.toLowerCase() : null;
        return data;
    }
}
