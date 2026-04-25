package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Custom annotation — demonstrates how Spring BPP can detect and process custom annotations
// @Retention(RUNTIME) — annotation is available at runtime via reflection
// @Target — can be applied to classes and methods
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NpciAnnotation {

}
