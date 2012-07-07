/**
 * 
 */
package org.aria.rlandri.generic.artifacts.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for a prime agent operation.
 * 
 * @author Tiberiu Popa
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PRIME_AGENT_OPERATION {
	String guard() default "";
}
