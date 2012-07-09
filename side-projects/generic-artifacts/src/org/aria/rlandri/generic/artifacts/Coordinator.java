package org.aria.rlandri.generic.artifacts;

import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;
import jason.mas2j.parser.mas2j;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.aria.rlandri.generic.artifacts.annotation.GuardedAnnotation;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;
import org.aria.rlandri.generic.artifacts.util.ReflectionUtils;

import cartago.AgentId;
import cartago.Artifact;
import cartago.ArtifactGuardMethod;
import cartago.CartagoException;
import cartago.IArtifactOp;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public abstract class Coordinator extends Artifact {
	HashMap<String, AgentId> agents;

	enum EnvStatus {
		PRIMORDIAL, INITIATED, RUNNING, EVALUATING, FINISHED
	};

	EnvStatus state = EnvStatus.PRIMORDIAL;
	public static final int realTimeSP = 0, realTimeNeg = 1,
			turnBasedSimultaneous = 2, turnBasedAlternative = 3;

	void init() throws CartagoException {
		try {
			agents = new HashMap<String, AgentId>();

			File mas2jFile = new File(".").listFiles(new FileFilter() {

				public boolean accept(File arg0) {
					return arg0.getAbsolutePath().endsWith("mas2j");
				}
			})[0];

			mas2j parser = new mas2j(new FileInputStream(mas2jFile));
			MAS2JProject project = parser.mas();
			for (AgentParameters ap : project.getAgents()) {
				if (!ap.getAgName().startsWith("prime_agent_s_"))
					if (ap.qty == 1) {
						agents.put(ap.getAgName(), null);
					} else
						for (int i = 1; i <= ap.qty; i++) {
							agents.put(ap.getAgName() + "_" + i, null);
						}
			}
			state = EnvStatus.INITIATED;

			registerCustomOperations();
		} catch (FileNotFoundException e) {
			throw new CartagoException("Could not find mas2j file");
		} catch (ParseException e) {
			throw new CartagoException("Parse exception for mas2j file");
		}
	}

	public void failIfNotRunning() {
		if (state != EnvStatus.RUNNING)
			failed("The coordinator is not in running mode");
	}

	public boolean isPrimeAgent() {
		return isPrimeAgent(getOpUserName());
	}

	public boolean isPrimeAgent(String agentName) {
		// TODO iffy prime agent check
		return agentName.startsWith("prime_agent_s_");
	}

	protected void addCustomOperation(GuardedAnnotation guardedAnnotation,
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
		Constructor<?> constructor = guardedAnnotation.getOpMethodConstructor();
		try {
			Object obj = constructor.newInstance(this, method);
			if (obj instanceof IArtifactOp) {
				IArtifactOp op = (IArtifactOp) obj;
				defineOp(op, guardBody);
			}
		} catch (IllegalArgumentException e) {
			throw new CartagoException(e.getMessage());
		} catch (InstantiationException e) {
			throw new CartagoException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new CartagoException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new CartagoException(e.getMessage());
		}
	}

	protected abstract void registerCustomOperations() throws CartagoException;

	protected abstract void updateRank();

	protected abstract void updateCurrency();

	protected abstract void saveState();

	@OPERATION
	void registerAgent(OpFeedbackParam<String> wsp) throws Exception {
		agents.put(getOpUserName(), getOpUserId());
	}


	@PRIME_AGENT_OPERATION
	void startSubenv() throws InterruptedException {
		signal("startSubenv");
		state = EnvStatus.RUNNING;
	}


	@OPERATION
	abstract void finishSubenv();

}
