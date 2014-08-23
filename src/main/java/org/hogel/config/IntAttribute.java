package org.hogel.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IntAttribute {
    String name();

    int defValue() default 0;
}
