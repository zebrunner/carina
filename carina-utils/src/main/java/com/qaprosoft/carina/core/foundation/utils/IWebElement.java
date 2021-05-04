package com.qaprosoft.carina.core.foundation.utils;

public interface IWebElement {
    String getText();
    String getName();
    
    boolean isElementPresent(long timeout);
}
