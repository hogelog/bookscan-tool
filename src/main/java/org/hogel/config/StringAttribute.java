package org.hogel.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface StringAttribute {
    String name();

    String defValue() default "";
}
