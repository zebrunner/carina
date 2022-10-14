package com.qaprosoft.carina.core.foundation.api.interceptor;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethod;

public interface ApiMethodInterceptor<M extends AbstractApiMethod> {

    void onInstantiation(M apiMethod);

    void onBeforeCall(M apiMethod);

}
