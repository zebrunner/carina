package com.qaprosoft.carina.core.foundation.utils.metadata;

import com.qaprosoft.carina.core.foundation.utils.metadata.model.ElementsInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yauhenipatotski on 4/14/17.
 */
public class MetadataCollector {

    private static Map<String, ElementsInfo> allCollectedData = new HashMap<>();


    public static void putPageInfo(String pageName, ElementsInfo elementsInfo) {
        allCollectedData.put(pageName, elementsInfo);
    }

    public static Map<String, ElementsInfo> getAllCollectedData() {
        return allCollectedData;
    }

}
