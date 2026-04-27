package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Custom annotation — demonstrates how Spring BPP can detect and process custom annotations
// @Retention(RUNTIME) — annotation is available at runtime via reflection
//   Without RUNTIME retention, isAnnotationPresent() in BPP would always return false
// @Target(METHOD) — can be applied to methods (used on transfer() in TransferServiceImpl)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NpciAnnotation {

}
