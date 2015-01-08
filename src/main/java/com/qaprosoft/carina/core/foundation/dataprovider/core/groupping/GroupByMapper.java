package com.qaprosoft.carina.core.foundation.dataprovider.core.groupping;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Yauheni_Patotski on 1/7/2015.
 */
public class GroupByMapper {

    private static Set<Integer> instanceInt;

    private static Set<String> instanceString;

    private static boolean hashMapped = false;


    public static boolean isHashMapped() {
        return hashMapped;
    }

    public static void setIsHashMapped(boolean isHashMapped) {
        GroupByMapper.hashMapped = isHashMapped;
    }


    public static Set<Integer> getInstanceInt(){
        if (instanceInt == null){
            instanceInt = Collections.synchronizedSet(new HashSet<Integer>());
        }
        return instanceInt;
    }

    public static Set<String> getInstanceStrings(){
        if (instanceString == null){
            instanceString = Collections.synchronizedSet(new HashSet<String>());
        }
        return instanceString;
    }
}