package com.qaprosoft.carina.core.foundation.spira;


public interface ISpiraInegrator {
	public void registerBuild(String build);
	public long startTestRun();
	public void registerTestStep();
	public long finishTestRun();
}
