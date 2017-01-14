package com.qaprosoft.carina.core.foundation.performance;

import com.qaprosoft.carina.core.foundation.performance.Timer.IPerformanceOperation;

public class Operation {

	public enum OPERATIONS implements IPerformanceOperation {
		TEST("test"), TEST2("test2"), TEST3("test3"), TEST4("test4");

		private String key;

		private OPERATIONS(String key) {
			this.key = key;
		}

		@Override
		public String getKey() {
			return this.key;
		}

	}

}
