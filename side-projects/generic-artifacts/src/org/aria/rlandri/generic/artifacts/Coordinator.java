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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aria.rlandri.generic.artifacts.annotation.GuardedAnnotation;
import org.aria.rlandri.generic.artifacts.annotation.GuardedAnnotationProcessor;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;
import org.aria.rlandri.generic.artifacts.util.ReflectionUtils;

import cartago.AgentId;
import cartago.Artifact;
import cartago.ArtifactGuardMethod;
import cartago.CartagoException;
import cartago.GUARD;
import cartago.IArtifactOp;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public abstract class Coordinator extends Artifact {

	public static final int realTimeSP = 0, realTimeNeg = 1,
			turnBasedSimultaneous = 2, turnBasedAlternative = 3;

	protected Map<String, AgentId> agents = new HashMap<String, AgentId>();
	protected Map<String, AgentId> masterAgents = new HashMap<String, AgentId>();
	private List<GuardedAnnotation> annotations = new ArrayList<GuardedAnnotation>();
	private EnvStatus state = EnvStatus.PRIMORDIAL;

	protected class CoordinatorAnnotation extends GuardedAnnotation {

		public CoordinatorAnnotation(
				Class<? extends Annotation> annotationClass,
				Class<? extends IArtifactOp> opMethodClass,
				boolean mandatoryValidator) throws CartagoException {
			super(annotationClass, opMethodClass, mandatoryValidator);
		}

		@Override
		public void processMethod(Method method) throws CartagoException {
			addCustomAnnotation(this, method);
		}

	}

	/**
	 * The basic initialisation of the coordinator. All subclasses that
	 * reimplement init must make a call to super.
	 * 
	 * @throws CartagoException
	 *             if the artifact could not be properly initialised
	 */
	protected void init() throws CartagoException {
		try {
			File mas2jFile = new File(".").listFiles(new FileFilter() {
				public boolean accept(File arg0) {
					return arg0.getAbsolutePath().endsWith("mas2j");
				}
			})[0];

			mas2j parser = new mas2j(new FileInputStream(mas2jFile));
			MAS2JProject project = parser.mas();
			for (AgentParameters ap : project.getAgents()) {
				String agentName = ap.getAgName();
				if (isParticipatingAgent(agentName)) {
					if (ap.qty == 1) {
						agents.put(agentName, null);
					} else {
						for (int i = 1; i <= ap.qty; i++) {
							agents.put(agentName + i, null);
						}
					}
				} else if (!isPrimeAgent(agentName)) {
					if (ap.qty == 1) {
						masterAgents.put(agentName, null);
					} else {
						for (int i = 1; i <= ap.qty; i++) {
							masterAgents.put(agentName + i, null);
						}
					}
				}
			}
			setState(EnvStatus.INITIATED);

			registerOperations();
		} catch (FileNotFoundException e) {
			throw new CartagoException("Could not find mas2j file");
		} catch (ParseException e) {
			throw new CartagoException("Parse exception for mas2j file");
		}
	}

	/**
	 * Fails and prints a formatted message.
	 */
	public void failWithMessage(String section, String message) {
		String failMsg = String.format("%s: %s", section, message);
		failed(failMsg);
	}

	/**
	 * Fails if the coordinator is not currently in the running state.
	 */
	public void failIfNotRunning() {
		if (isNotRunning())
			failed("The coordinator is not in running mode");
	}

	/**
	 * Returns <tt>true</tt> if the calling agent is the prime agent.
	 * 
	 * @return <tt>true</tt> if the calling agent is the prime agent
	 */
	public boolean isPrimeAgent() {
		return isPrimeAgent(getOpUserName());
	}

	/**
	 * Returns <tt>true</tt> if <tt>agentName</tt> is the prime agent.
	 * 
	 * @return <tt>true</tt> if <tt>agentName</tt> is the prime agent
	 */
	public boolean isPrimeAgent(String agentName) {
		// TODO iffy prime agent check
		return agentName.startsWith("prime_agent_s_");
	}

	public EnvStatus getState() {
		return state;
	}

	/**
	 * Sets the current state of coordinator to the specified one.
	 * 
	 * @param state
	 *            the new state of the coordinator
	 */
	public void setState(EnvStatus state) {
		System.err.println(String.format("changed state from %s to %s",
				this.state, state));
		this.state = state;
	}

	/**
	 * Appends the custom annotation to the list of operations.
	 * 
	 * @param annotation
	 *            the custom annotation to be added
	 */
	protected void addOperation(GuardedAnnotation annotation) {
		annotations.add(annotation);
	}

	/**
	 * Returns <tt>true</tt> if <tt>agentName</tt> is a participating agent.
	 * 
	 * @return <tt>true</tt> if <tt>agentName</tt> is a participating agent
	 */
	private boolean isParticipatingAgent(String agentName) {
		try {
			String[] tokens = agentName.split("_");
			if (tokens.length >= 2 && tokens.length <= 3) {
				if ("agent".equals(tokens[0])) {
					for (int i = 1; i < tokens.length; i++) {
						int id = Integer.parseInt(tokens[i]);
						if (id <= 0)
							return false;
					}
					return true;
				}
			}
		} catch (NumberFormatException e) {
		}
		return false;
	}

	/**
	 * Fills the list of operations with the desired custom annotations.
	 * 
	 * @throws CartagoException
	 *             if the custom annotations fail to be valid
	 */
	protected abstract void fillOperations() throws CartagoException;

	protected abstract void updateRank();

	protected abstract void updateCurrency();

	protected abstract void saveState();

	private void addCustomAnnotation(GuardedAnnotation guardedAnnotation,
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

		String validator = guardedAnnotation.invokeValidatorMethod(annotation);
		Method validatorMethod = null;
		if (!"".equals(validator)) {
			validatorMethod = ReflectionUtils.getMethodInHierarchy(getClass(),
					validator, method.getParameterTypes());
			if (validatorMethod == null) {
				throw new CartagoException("invalid validator: " + validator);
			}
		} else if (guardedAnnotation.isValidatorMandatory()) {
			String errMsg = "the operation does not specify a validator";
			throw new CartagoException(errMsg);
		}

		Constructor<?> constructor = guardedAnnotation.getOpMethodConstructor();
		try {
			Object obj = constructor.newInstance(this, method, validatorMethod);
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

	private void registerOperations() throws CartagoException {
		fillOperations();

		GuardedAnnotationProcessor processor = new GuardedAnnotationProcessor(
				getClass());
		processor.processAnnotations(annotations);
	}

	@OPERATION
	void registerAgent(OpFeedbackParam<String> wsp) {
		String agentName = getOpUserName();
		if (agents.containsKey(agentName)) {
			agents.put(agentName, getOpUserId());
		} else {
			String errFmt = "%s cannot register in this sub-environment";
			failed(String.format(errFmt, agentName));
		}
	}

	@OPERATION
	void registerMasterAgent(OpFeedbackParam<String> wsp) {
		String agentName = getOpUserName();
		if (masterAgents.containsKey(agentName)) {
			masterAgents.put(agentName, getOpUserId());
		} else {
			String errFmt = "%s cannot register as a master in this sub-environment";
			failed(String.format(errFmt, agentName));
		}
	}

	@PRIME_AGENT_OPERATION
	void startSubenv() {
		signal("startSubenv");
		setState(EnvStatus.RUNNING);
	}

	@OPERATION
	void finishSubenv() {
		updateRank();
		updateCurrency();
		saveState();
	}

	@GUARD
	boolean isNotRunning() {
		return state != EnvStatus.RUNNING;
	}

}
