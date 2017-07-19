package com.qaprosoft.carina.core.foundation.api.ssl;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethodV2;

public class PutDocMethod extends AbstractApiMethodV2
{

	public PutDocMethod()
	{
		super(null, null);
		replaceUrlPlaceholder("base_url", "https://demo-default.uw2.document-server-nprd.lendingcloud.us:8003");
		request.header("Content-Type", "text/plain");
		request.header("LCDOC_TYPE", "test");
		request.body("Test Core Client");
	}

}
