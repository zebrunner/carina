package com.qaprosoft.carina.core.foundation.utils.tag;


import java.lang.annotation.*;

@Repeatable(TestTag.List.class)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestTag {
    String name();

    String value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @interface List {

        TestTag[] value();
    }
}
