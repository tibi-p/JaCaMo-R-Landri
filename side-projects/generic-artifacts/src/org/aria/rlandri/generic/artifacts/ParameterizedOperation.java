package org.aria.rlandri.generic.artifacts;

import cartago.IArtifactOp;

public class ParameterizedOperation {

	private IArtifactOp op;
	private Object[] params;

	public ParameterizedOperation(IArtifactOp op, Object[] params) {
		this.op = op;
		this.params = params;
	}

	public IArtifactOp getOp() {
		return op;
	}

	public Object[] getParams() {
		return params;
	}

}
