package org.aria.rlandri.generic.artifacts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.map.MultiValueMap;
import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.GuardedAnnotation;
import org.aria.rlandri.generic.artifacts.annotation.GuardedAnnotationProcessor;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;

import cartago.AgentId;
import cartago.CartagoException;
import cartago.IArtifactOp;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class SimultaneouslyExecutedCoordinator extends Coordinator {

	private MultiValueMap operationQueue = new MultiValueMap();

	void init() throws CartagoException {
		super.init();
	}

	public void addOpMethod(IArtifactOp op, Object[] params) {
		AgentId agentId = getOpUserId();
		operationQueue.put(agentId, new ParameterizedOperation(op, params));
	}

	@OPERATION
	void registerAgent() {

	}

	@OPERATION
	void runQueuedOperations() {
		System.out.println("SPARTAAAAAA!");
		for (Object key : operationQueue.keySet()) {
			Collection<?> coll = operationQueue.getCollection(key);
			for (Object value : coll) {
				if (value instanceof ParameterizedOperation) {
					ParameterizedOperation entry = (ParameterizedOperation) value;
					try {
						SETBGameArtifactOpMethod op = (SETBGameArtifactOpMethod) entry
								.getOp();
						op.execSavedParameters(entry.getParams());
					} catch (Exception e) {
						// TODO log it or something
						e.printStackTrace();
					}
				}
			}
		}
	}

	@GAME_OPERATION
	void hotelCismigiu() {
		System.out.println("SA MA MUT IN HOTEL CISMIGIU");
	}

	@Override
	protected void registerCustomOperations() throws CartagoException {
		List<GuardedAnnotation> annotations = new ArrayList<GuardedAnnotation>();
		annotations.add(new CoordinatorAnnotation(GAME_OPERATION.class,
				SETBGameArtifactOpMethod.class));
		annotations.add(new CoordinatorAnnotation(PRIME_AGENT_OPERATION.class,
				PrimeAgentArtifactOpMethod.class));

		GuardedAnnotationProcessor processor = new GuardedAnnotationProcessor(
				getClass());
		processor.processAnnotations(annotations);
	}

	@Override
	@OPERATION
	void registerAgent(OpFeedbackParam<String> wsp) {
		// TODO Auto-generated method stub
		wsp.set("NA");
	}

	@Override
	protected void updateRank() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void updateCurrency() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void saveState() {
		// TODO Auto-generated method stub
	}

}
