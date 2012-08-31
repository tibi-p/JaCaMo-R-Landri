package org.aria.rlandri.generic.artifacts.tools;

public enum ValidationType {

	WARNING("warning"), ERROR("error"), FATAL("fatal");

	private final String name;

	ValidationType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isSerious() {
		return equals(ERROR) || equals(FATAL);
	}

}
