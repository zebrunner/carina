package com.qaprosoft.carina.core.foundation.grid;

import java.util.ArrayList;
import java.util.List;

public class GridRequest
{
	private String gridSessionId;
	private String testId;
	private String serial;
	private List<String> models = new ArrayList<>();
	private Operation operation;
	
	public GridRequest(String gridSessionId, String testId, List<String> models, Operation operation)
	{
		this.gridSessionId = gridSessionId;
		this.testId = testId;
		this.models = models;
		this.operation = operation;
	}
	

	public GridRequest(String gridSessionId, String testId, String serial, Operation operation)
	{
		this.gridSessionId = gridSessionId;
		this.testId = testId;
		this.serial = serial;
		this.operation = operation;
	}
	
	public String getGridSessionId()
	{
		return gridSessionId;
	}

	public void setGridSessionId(String gridSessionId)
	{
		this.gridSessionId = gridSessionId;
	}

	public String getTestId()
	{
		return testId;
	}

	public void setTestId(String testId)
	{
		this.testId = testId;
	}

	public List<String> getModels()
	{
		return models;
	}

	public void setModels(List<String> models)
	{
		this.models = models;
	}
	
	public Operation getOperation()
	{
		return operation;
	}

	public void setOperation(Operation operation)
	{
		this.operation = operation;
	}

	public String getSerial()
	{
		return serial;
	}

	public void setSerial(String serial)
	{
		this.serial = serial;
	}

	public enum Operation {CONNECT, DISCONNECT};
}