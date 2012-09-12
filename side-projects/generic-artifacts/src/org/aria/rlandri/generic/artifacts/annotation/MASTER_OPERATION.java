/**
 * 
 */
package org.aria.rlandri.generic.artifacts.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for a master operation.
 * 
 * @author Tiberiu Popa
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MASTER_OPERATION {
	String guard() default "";

	String validator() default "";
}
