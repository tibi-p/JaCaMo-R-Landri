package org.aria.rlandri.generic.artifacts;

import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Structure;
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

import ora4mas.nopl.JasonTermWrapper;

import org.aria.rlandri.generic.artifacts.annotation.GuardedAnnotation;
import org.aria.rlandri.generic.artifacts.annotation.GuardedAnnotationProcessor;
import org.aria.rlandri.generic.artifacts.annotation.MASTER_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;
import org.aria.rlandri.generic.artifacts.opmethod.MasterArtifactOpMethod;
import org.aria.rlandri.generic.artifacts.tools.ValidationResult;
import org.aria.rlandri.generic.artifacts.tools.ValidationType;
import org.aria.rlandri.generic.artifacts.util.ReflectionUtils;

import cartago.AgentId;
import cartago.Artifact;
import cartago.ArtifactGuardMethod;
import cartago.CartagoException;
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

	public static final String VALIDATION_FUNCTOR = "op_error";
	public static final String TURN_FUNCTOR = "turn_error";
	public static final String GAME_FUNCTOR = "game_error";

	protected final Configuration configuration = Configuration.getInstance();

	/**
	 * The registry of regular agents.
	 */
	protected final AgentRegistry regularAgents = new AgentRegistry();
	/**
	 * The registry of master agents.
	 */
	protected final AgentRegistry masterAgents = new AgentRegistry();
	/**
	 * The registry of prime agents.
	 */
	protected final AgentRegistry primeAgents = new AgentRegistry();

	// TODO check if this is RLY necessary
	private final Map<String, ValidationResult> failures = new HashMap<String, ValidationResult>();
	/**
	 * The list of guarded annotations used by the coordinator.
	 */
	private final List<GuardedAnnotation> annotations = new ArrayList<GuardedAnnotation>();
	/**
	 * The current state of the coordinator.
	 */
	private EnvStatus state = EnvStatus.PRIMORDIAL;
	/**
	 * The type of sub-environment.
	 */
	private String environmentType;

	/**
	 * Implementation of guarded annotations for the coordinator.
	 * 
	 * @author Tiberiu Popa
	 */
	protected class CoordinatorAnnotation extends GuardedAnnotation {

		/**
		 * Constructs a new <tt>CoordinatorAnnotation</tt>.
		 * 
		 * @param annotationClass
		 *            the class of the annotation
		 * @param opMethodClass
		 *            the class of the artifact operation
		 * @param mandatoryValidator
		 *            whether the validator element is mandatory in the
		 *            annotation
		 * @throws CartagoException
		 *             if a security manager is present and somehow gets upset
		 *             in the process or if <tt>annotationClass</tt> does not
		 *             possess the required methods
		 */
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
			doBaseInit();
		} catch (FileNotFoundException e) {
			throw new CartagoException("Could not find mas2j file");
		} catch (IOException e) {
			throw new CartagoException(e.getMessage());
		} catch (ParseException e) {
			throw new CartagoException("Parse exception for mas2j file");
		}
	}

	/**
	 * Fails as a result of a validation error.
	 */
	public void failValidation() {
		ValidationResult vres = failures.get(getOpUserName());
		ListTerm validationErrorList = new ListTermImpl();
		for (String reason : vres.getReasons()) {
			Structure term = new Structure(vres.getType(reason).getName());
			term.addTerm(new Atom(reason));
			validationErrorList.add(term);
		}
		failed("validation", VALIDATION_FUNCTOR, new JasonTermWrapper(
				validationErrorList));
	}

	/**
	 * Fails as a result of a turn error.
	 * 
	 * @param reason
	 *            the reason for the error
	 * @param type
	 *            the type of the error
	 */
	public void failTurn(String reason, ValidationType type) {
		ListTerm validationErrorList = new ListTermImpl();
		Structure term = new Structure(type.getName());
		term.addTerm(new Atom(reason));
		validationErrorList.add(term);
		failed("validation", TURN_FUNCTOR, new JasonTermWrapper(
				validationErrorList));
	}

	/**
	 * Fails if the coordinator is not currently in the initiated state.
	 */
	public void failIfNotInitiated() {
		if (state != EnvStatus.INITIATED)
			failTurn("coordinator_is_not_initiated", ValidationType.ERROR);
	}

	/**
	 * Fails if the coordinator is not currently in the running state.
	 */
	public void failIfNotRunning() {
		if (state != EnvStatus.RUNNING)
			failTurn("coordinator_is_not_running", ValidationType.ERROR);
	}

	/**
	 * Fails if the current agent is not a registered participating agent.
	 */
	public void failIfNotRegisteredParticipatingAgent() {
		if (!isRegisteredParticipatingAgent())
			failTurn("not_registered_participating_agent", ValidationType.ERROR);
	}

	/**
	 * Fails if the current agent is not a registered master agent.
	 */
	public void failIfNotRegisteredMasterAgent() {
		if (!isRegisteredMasterAgent())
			failTurn("not_registered_master_agent", ValidationType.ERROR);
	}

	/**
	 * Fails if the current agent is not a registered prime agent.
	 */
	public void failIfNotRegisteredPrimeAgent() {
		if (!isRegisteredPrimeAgent())
			failTurn("not_registered_prime_agent", ValidationType.ERROR);
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

	/**
	 * Returns the current state of the coordinator.
	 * 
	 * @return the current state of the coordinator
	 */
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
	 * Sends a signal to all master agents.
	 * 
	 * @param type
	 *            the type of the signal
	 * @param objs
	 *            the signal's arguments
	 */
	protected void signalMasterAgents(String type, Object... objs) {
		for (AgentId agentId : masterAgents.getAgentIds())
			signal(agentId, type, objs);
	}

	/**
	 * Sends a signal to all prime agents.
	 * 
	 * @param type
	 *            the type of the signal
	 * @param objs
	 *            the signal's arguments
	 */
	protected void signalPrimeAgents(String type, Object... objs) {
		for (AgentId agentId : primeAgents.getAgentIds())
			signal(agentId, type, objs);
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

	/**
	 * The basic initialisation of the coordinator with no exceptions wrapped.
	 * 
	 * @throws FileNotFoundException
	 *             if the configuration properties file could not be found
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws ParseException
	 *             if the mas2j file could not be parsed
	 * @throws CartagoException
	 *             if the custom operations fail to register
	 */
	private void doBaseInit() throws FileNotFoundException, IOException,
			ParseException, CartagoException {
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
				primeAgents.addAgentName(agentName, ap.qty);
			} else if (isParticipatingAgent(agentName)) {
				regularAgents.addAgentName(agentName, ap.qty);
			} else {
				masterAgents.addAgentName(agentName, ap.qty);
			}
		}
		setState(EnvStatus.INITIATED);
		registerOperations();
	}

	/**
	 * Adds a Cartago operation backed by a <tt>Method</tt> that is associated
	 * with a <tt>GuardedAnnotation</tt>.
	 * 
	 * @param guardedAnnotation
	 *            the method's guarded annotation
	 * @param method
	 *            the method underlying to the Cartago operation
	 * @throws CartagoException
	 *             if the Cartago operation could not be constructed
	 */
	private void addCustomAnnotation(GuardedAnnotation guardedAnnotation,
			Method method) throws CartagoException {
		Annotation annotation = guardedAnnotation.getMethodAnnotation(method);
		String guard = guardedAnnotation.invokeGuardMethod(annotation);
		ArtifactGuardMethod guardBody = null;
		if (guard != null && !"".equals(guard)) {
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
		if (validator != null && !"".equals(validator)) {
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

	/**
	 * Loads the configuration properties.
	 * 
	 * @throws FileNotFoundException
	 *             if the configuration properties file could not be found
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private void loadProperties() throws FileNotFoundException, IOException {
		this.environmentType = configuration.getProperty("environment_type");
	}

	/**
	 * Populate the list of operations with custom annotations and process them.
	 * 
	 * @throws CartagoException
	 *             if the custom annotations fail to be valid
	 */
	private void registerOperations() throws CartagoException {
		fillDefaultOperations();
		fillOperations();

		GuardedAnnotationProcessor processor = new GuardedAnnotationProcessor(
				getClass());
		processor.processAnnotations(annotations);
	}

	/**
	 * Fills the list of operations with the default custom annotations.
	 * 
	 * @throws CartagoException
	 *             if the custom annotations fail to be valid
	 */
	private void fillDefaultOperations() throws CartagoException {
		addOperation(new CoordinatorAnnotation(MASTER_OPERATION.class,
				MasterArtifactOpMethod.class, true));
	}

	@PRIME_AGENT_OPERATION
	protected void startSubenv() {
		signal("startSubenv");
	}

	@OPERATION
	protected void registerAgent(OpFeedbackParam<String> wsp) {
		failIfNotInitiated();
		AgentId agentId = getOpUserId();
		if (!regularAgents.registerAgent(agentId)) {
			failTurn("cannot_register_as_participating_agent",
					ValidationType.ERROR);
		}
	}

	@OPERATION
	protected void registerMasterAgent(OpFeedbackParam<String> wsp) {
		failIfNotInitiated();
		AgentId agentId = getOpUserId();
		if (!masterAgents.registerAgent(agentId)) {
			failTurn("cannot_register_as_master_agent", ValidationType.ERROR);
		}
	}

	@OPERATION
	protected void registerPrimeAgent() {
		failIfNotInitiated();
		AgentId agentId = getOpUserId();
		if (!primeAgents.registerAgent(agentId)) {
			failTurn("cannot_register_as_prime_agent", ValidationType.ERROR);
		}
	}

	@OPERATION
	protected void finishSubenv() {
		updateRank();
		updateCurrency();
		saveState();
	}

	public void addValidationResult(ValidationResult vres) {
		failures.put(vres.getAgent(), vres);
	}

}
