package com.qaprosoft.carina.core.foundation.dataprovider.annotations;


/**
 * Created by Patotsky on 29.12.2014.
 */

public @interface GroupBy {

    int parameterNumber () default 0;

    String collumnName() default "";
}
