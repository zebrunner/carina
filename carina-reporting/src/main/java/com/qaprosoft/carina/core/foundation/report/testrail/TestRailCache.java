package com.qaprosoft.carina.core.foundation.report.testrail;

import com.qaprosoft.carina.core.foundation.report.testrail.dto.TestCaseInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yauheni_patotski on 5/13/15.
 */
public class TestRailCache {

   static Map<Integer, ArrayList<TestCaseInfo>> suiteCases = Collections.synchronizedMap(new HashMap<Integer, ArrayList<TestCaseInfo>>());




}
