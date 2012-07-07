package org.aria.rlandri.generic.artifacts;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.map.MultiValueMap;
import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.GuardedAnnotation;
import org.aria.rlandri.generic.artifacts.annotation.GuardedAnnotationProcessor;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;
import org.aria.rlandri.generic.artifacts.util.ReflectionUtils;

import cartago.AgentId;
import cartago.ArtifactGuardMethod;
import cartago.CartagoException;
import cartago.IArtifactOp;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class SimultaneouslyExecutedCoordinator extends Coordinator {

	private MultiValueMap operationQueue = new MultiValueMap();

	void init() throws CartagoException {
		super.init();
		registerGameOperations();
	}

	public void addOpMethod(GameArtifactOpMethod op) {
		AgentId agentId = getOpUserId();
		operationQueue.put(agentId, op);
	}

	@PRIME_AGENT_OPERATION
	void startSubenv() {
		signal("startSubenv");
		state = EnvStatus.RUNNING;
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
				if (value instanceof GameArtifactOpMethod) {
					GameArtifactOpMethod op = (GameArtifactOpMethod) value;
					try {
						op.execSavedParameters();
					} catch (Exception e) {
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

	private void registerGameOperations() throws CartagoException {
		List<GuardedAnnotation> annotations = new ArrayList<GuardedAnnotation>();
		annotations.add(new GuardedAnnotation(GAME_OPERATION.class) {

			@Override
			public void processMethod(Method method) throws CartagoException {
				addCustomOperation(this, method);
			}

		});
		annotations.add(new GuardedAnnotation(PRIME_AGENT_OPERATION.class) {

			@Override
			public void processMethod(Method method) throws CartagoException {
				// TODO this is iffy, do it the right way
				if (!getOpUserName().startsWith("prime_agent_s_"))
					failed("Only the prime agent can execute a PRIME_AGENT_OPERATION");
				addCustomOperation(this, method);
			}

		});

		GuardedAnnotationProcessor processor = new GuardedAnnotationProcessor(
				getClass());
		processor.processAnnotations(annotations);
	}

	private void addCustomOperation(GuardedAnnotation guardedAnnotation,
			Method method) throws CartagoException {
		Annotation annotation = guardedAnnotation.getMethodAnnotation(method);
		System.out.println("-- " + method);
		System.out.println("-- " + annotation);
		String guard = guardedAnnotation.invokeGuardMethod(annotation);
		ArtifactGuardMethod guardBody = null;
		if (!"".equals(guard)) {
			Method guardMethod = ReflectionUtils.getMethodInHierarchy(
					getClass(), guard, method.getParameterTypes());
			if (guardMethod == null) {
				throw new CartagoException("invalid guard: " + guard);
			} else {
				guardBody = new ArtifactGuardMethod(this, guardMethod);
			}
		}
		IArtifactOp op = new GameArtifactOpMethod(this, method);
		defineOp(op, guardBody);
	}

	@Override
	@OPERATION
	void registerAgent(OpFeedbackParam<String> wsp) {
		// TODO Auto-generated method stub
		wsp.set("NA");
	}

	@Override
	@OPERATION
	void finishSubenv() {
		// TODO Auto-generated method stub

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
