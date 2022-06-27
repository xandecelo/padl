package org.alefzero.padl.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class PadlUtils {


    public synchronized static Map<String,String> split(Collection<String> data) {
        return PadlUtils.split(data, false);
    }

    public synchronized static Map<String,String> split(Collection<String> data, boolean invertKeys) {
        Map<String,String> items = new HashMap<String,String>();
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
}
