package com.qaprosoft.carina.core.foundation.skip;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.qaprosoft.carina.core.foundation.rule.IRule;

@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE, METHOD })
public @interface ExpectedSkip {
    
    Class<? extends IRule>[] rules();

}