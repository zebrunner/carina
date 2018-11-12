package com.qaprosoft.carina.core.foundation.report.testrail;

import java.util.ArrayList;
import java.util.List;

public interface ITestCases {
	final ThreadLocal<List<String>> casesIds = ThreadLocal.withInitial(ArrayList::new);

	default public List<String> getCases() {
		return casesIds.get();
	}

	default public void setCases(String... cases) {
		for (String _case : cases) {
			casesIds.get().add(_case);
		}
	}

	default public void clearCases() {
		casesIds.set(new ArrayList<String>());
	}

}
