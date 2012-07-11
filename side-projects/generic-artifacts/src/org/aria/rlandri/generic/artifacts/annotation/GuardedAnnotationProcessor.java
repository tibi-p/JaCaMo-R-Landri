package org.aria.rlandri.generic.artifacts.annotation;

import java.lang.reflect.Method;
import java.util.Collection;

import cartago.CartagoException;

public class GuardedAnnotationProcessor {

	private final Class<?> clazz;

	public GuardedAnnotationProcessor(Class<?> clazz) {
		this.clazz = clazz;
	}

	public void processAnnotations(Collection<GuardedAnnotation> annotations)
			throws CartagoException {
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			Method[] methods = c.getDeclaredMethods();
			for (Method method : methods) {
				for (GuardedAnnotation annotation : annotations)
					if (annotation.isMethodAnnotated(method))
						annotation.processMethod(method);
			}
		}
	}

}
