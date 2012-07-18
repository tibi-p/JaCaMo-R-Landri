package org.aria.rlandri.generic.artifacts;

import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;
import jason.mas2j.parser.mas2j;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aria.rlandri.generic.artifacts.annotation.GuardedAnnotation;
import org.aria.rlandri.generic.artifacts.annotation.GuardedAnnotationProcessor;
import org.aria.rlandri.generic.artifacts.annotation.MASTER_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;
import org.aria.rlandri.generic.artifacts.opmethod.MasterArtifactOpMethod;
import org.aria.rlandri.generic.artifacts.tools.ValidationResult;
import org.aria.rlandri.generic.artifacts.util.ReflectionUtils;

import cartago.AgentId;
import cartago.Artifact;
import cartago.ArtifactGuardMethod;
import cartago.CartagoException;
import cartago.GUARD;
import cartago.IArtifactOp;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

/**
 * The main coordinator abstract class from which all particular coordinator
 * implementations stem from.
 * 
 * @author Andrei Geacar
 * @author Mihai Poenaru
 * @author Tiberiu Popa
 */
public abstract class Coordinator extends Artifact {

	protected final AgentRegistry regularAgents = new AgentRegistry();
	protected final AgentRegistry masterAgents = new AgentRegistry();
	protected final AgentRegistry primeAgents = new AgentRegistry();
	private final Map<String, ValidationResult> failures = new HashMap<String, ValidationResult>();
	private final List<GuardedAnnotation> annotations = new ArrayList<GuardedAnnotation>();
	private EnvStatus state = EnvStatus.PRIMORDIAL;
	private boolean typedFailSearch = false;
	private String environmentType;

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
			loadProperties();

			File mas2jFile = new File(".").listFiles(new FileFilter() {
				public boolean accept(File arg0) {
					return arg0.getAbsolutePath().endsWith("mas2j");
				}
			})[0];

			mas2j parser = new mas2j(new FileInputStream(mas2jFile));
			MAS2JProject project = parser.mas();
			for (AgentParameters ap : project.getAgents()) {
				String agentName = ap.getAgName();
				if (isPrimeAgent(agentName)) {
					addAgentToRegistry(primeAgents, agentName, ap.qty);
				} else if (isParticipatingAgent(agentName)) {
					addAgentToRegistry(regularAgents, agentName, ap.qty);
				} else {
					addAgentToRegistry(masterAgents, agentName, ap.qty);
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
	 * Fails if the coordinator is not currently in the initiated state.
	 */
	public void failIfNotInitiated() {
		if (state != EnvStatus.INITIATED)
			failed("The coordinator is not in running mode");
	}

	/**
	 * Fails if the coordinator is not currently in the running state.
	 */
	public void failIfNotRunning() {
		if (isNotRunning())
			failed("The coordinator is not in running mode");
	}

	/**
	 * Fails if the current agent is not a registered participating agent.
	 */
	public void failIfNotRegisteredParticipatingAgent() {
		if (!isRegisteredParticipatingAgent())
			failed("The current agent is not a registered participating agent");
	}

	/**
	 * Fails if the current agent is not a registered master agent.
	 */
	public void failIfNotRegisteredMasterAgent() {
		if (!isRegisteredMasterAgent())
			failed("The current agent is not a registered master agent");
	}

	/**
	 * Fails if the current agent is not a registered prime agent.
	 */
	public void failIfNotRegisteredPrimeAgent() {
		if (!isRegisteredPrimeAgent())
			failed("The current agent is not a registered prime agent");
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
		String primeName = String.format("prime_agent_s_%s", environmentType);
		return primeName.equals(agentName);
	}

	/**
	 * Returns <tt>true</tt> if the calling agent is a registered participating
	 * agent.
	 * 
	 * @return <tt>true</tt> if the calling agent is a registered participating
	 *         agent
	 */
	public boolean isRegisteredParticipatingAgent() {
		return isRegisteredParticipatingAgent(getOpUserId());
	}

	/**
	 * Returns <tt>true</tt> if <tt>agentId</tt> is a registered participating
	 * agent.
	 * 
	 * @return <tt>true</tt> if <tt>agentId</tt> is a registered participating
	 *         agent
	 */
	public boolean isRegisteredParticipatingAgent(AgentId agentId) {
		return regularAgents.isRegistered(agentId);
	}

	/**
	 * Returns <tt>true</tt> if the calling agent is a registered master agent.
	 * 
	 * @return <tt>true</tt> if the calling agent is a registered master agent
	 */
	public boolean isRegisteredMasterAgent() {
		return isRegisteredMasterAgent(getOpUserId());
	}

	/**
	 * Returns <tt>true</tt> if <tt>agentId</tt> is a registered master agent.
	 * 
	 * @return <tt>true</tt> if <tt>agentId</tt> is a registered master agent
	 */
	public boolean isRegisteredMasterAgent(AgentId agentId) {
		return masterAgents.isRegistered(agentId);
	}

	/**
	 * Returns <tt>true</tt> if the calling agent is a registered prime agent.
	 * 
	 * @return <tt>true</tt> if the calling agent is a registered prime agent
	 */
	public boolean isRegisteredPrimeAgent() {
		return isRegisteredPrimeAgent(getOpUserId());
	}

	/**
	 * Returns <tt>true</tt> if <tt>agentId</tt> is a registered prime agent.
	 * 
	 * @return <tt>true</tt> if <tt>agentId</tt> is a registered prime agent
	 */
	public boolean isRegisteredPrimeAgent(AgentId agentId) {
		return primeAgents.isRegistered(agentId);
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

	private void loadProperties() throws CartagoException {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("config.properties"));
			this.environmentType = prop.getProperty("environment_type");
		} catch (FileNotFoundException e) {
			throw new CartagoException(e.getMessage());
		} catch (IOException e) {
			throw new CartagoException(e.getMessage());
		}
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
		fillDefaultOperations();
		fillOperations();

		GuardedAnnotationProcessor processor = new GuardedAnnotationProcessor(
				getClass());
		processor.processAnnotations(annotations);
	}

	private void fillDefaultOperations() throws CartagoException {
		addOperation(new CoordinatorAnnotation(MASTER_OPERATION.class,
				MasterArtifactOpMethod.class, true));
	}

	@OPERATION
	void registerAgent(OpFeedbackParam<String> wsp) {
		failIfNotInitiated();
		AgentId agentId = getOpUserId();
		if (!regularAgents.registerAgent(agentId)) {
			String errFmt = "%s cannot register as a regular in this sub-environment";
			failed(String.format(errFmt, agentId));
		}
	}

	@OPERATION
	void registerMasterAgent(OpFeedbackParam<String> wsp) {
		failIfNotInitiated();
		AgentId agentId = getOpUserId();
		if (!masterAgents.registerAgent(agentId)) {
			String errFmt = "%s cannot register as a master in this sub-environment";
			failed(String.format(errFmt, agentId));
		}
	}

	@OPERATION
	void registerPrimeAgent() {
		failIfNotInitiated();
		AgentId agentId = getOpUserId();
		if (!primeAgents.registerAgent(agentId)) {
			String errFmt = "%s cannot register as a prime in this sub-environment";
			failed(String.format(errFmt, agentId));
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

	public void addValidationResult(ValidationResult vres) {
		failures.put(vres.getAgent(), vres);
	}

	@OPERATION
	void getNextFailureReason(OpFeedbackParam<String> reason,
			OpFeedbackParam<String> type) {
		ValidationResult vres = failures.get(getOpUserName());
		if (vres == null) {
			reason.set("NA");
			failures.remove(getOpUserName());
			return;
		}
		if (typedFailSearch) {
			vres.index = 0;
			typedFailSearch = false;
		}
		if (vres.index == vres.getReasons().size()) {
			failures.remove(getOpUserName());
			return;
		}
		reason.set(vres.getReasons().get(vres.index++));
		type.set(vres.getType(reason.get()));
	}

	@OPERATION
	void getNextFailureReason(OpFeedbackParam<String> reason,
			OpFeedbackParam<String> type, Object... types) {

		ValidationResult vres = failures.get(getOpUserName());
		if (vres == null) {
			reason.set("NA");
			failures.remove(getOpUserName());
			return;
		}
		if (!typedFailSearch) {
			vres.index = 0;
			typedFailSearch = true;
		}
		if (vres.index == vres.getReasons().size()) {
			failures.remove(getOpUserName());
			return;
		}
		ArrayList<Integer> typeFilter = new ArrayList<Integer>();
		for (Object t : types) {
			typeFilter.add(((Number) t).intValue());
		}

		reason.set(vres.getReasons(typeFilter).get(vres.index++));
		type.set(vres.getType(reason.get()));
	}

	private static final void addAgentToRegistry(AgentRegistry registry,
			String agentName, int qty) {
		if (qty > 1) {
			for (int i = 1; i <= qty; i++)
				registry.addAgentName(agentName + i);
		} else {
			registry.addAgentName(agentName);
		}
	}

}
