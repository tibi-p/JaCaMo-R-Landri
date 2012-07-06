package org.aria.rlandri.generic.artifacts;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.commons.collections.map.MultiValueMap;

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

	@OPERATION
	void startSubenv() {
		if (getOpUserName().equals("prime_agent_s_generic")) {
			signal("startSubenv");
			state = EnvStatus.RUNNING;
		}
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
		System.out.println("SA TE *UT IN HOTEL CISMIGIU");
	}

	private void registerGameOperations() throws CartagoException {
		for (Class<?> c = getClass(); c != null; c = c.getSuperclass()) {
			Method[] methods = c.getDeclaredMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(GAME_OPERATION.class))
					addGameOperation(method);
			}
		}
	}

	private void addGameOperation(Method method) throws CartagoException {
		GAME_OPERATION annotation = method.getAnnotation(GAME_OPERATION.class);
		System.out.println("-- " + method);
		System.out.println("-- " + annotation);
		String guard = annotation.guard();
		ArtifactGuardMethod guardBody = null;
		if (!"".equals(guard)) {
			Method guardMethod = getMethodInHierarchy(guard,
					method.getParameterTypes());
			if (guardMethod == null) {
				throw new CartagoException("invalid guard: " + guard);
			} else {
				guardBody = new ArtifactGuardMethod(this, guardMethod);
			}
		}
		IArtifactOp op = new GameArtifactOpMethod(this, method);
		defineOp(op, guardBody);
	}

	private Method getMethodInHierarchy(String name, Class<?>[] types) {
		Class<?> cl = getClass();
		do {
			try {
				return cl.getDeclaredMethod(name, types);
			} catch (Exception ex) {
				cl = cl.getSuperclass();
			}
		} while (cl != null);
		return null;
	}

	@Override
	@OPERATION
	void registerAgent(OpFeedbackParam<String> wsp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@OPERATION
	void finishSubenv() {
		// TODO Auto-generated method stub
		
	}

}
