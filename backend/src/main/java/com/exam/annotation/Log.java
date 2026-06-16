package com.exam.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    String module() default "";
    String operation() default "";
    int operationType() default 9;
    String targetType() default "";
    boolean recordState() default false;
}
