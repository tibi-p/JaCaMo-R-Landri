/**
 * 
 */
package org.aria.rlandri.generic.artifacts.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for a game operation.
 * 
 * @author Tiberiu Popa
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GAME_OPERATION {
	String guard() default "";
}
