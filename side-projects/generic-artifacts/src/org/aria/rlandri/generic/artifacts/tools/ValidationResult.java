package org.aria.rlandri.generic.artifacts.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class that contains data structures and methods which allow throwing detailed
 * types of failures during the validation of GAME_OPERATION type methods. This
 * is stored by a coordinator artifact and from there it is used by the agent to
 * recover from errors.
 * 
 * @author Mihai Poenaru - JaCaMo/R'Landri
 */
public class ValidationResult {

	private final Map<String, ValidationType> reasons = new HashMap<String, ValidationType>();
	private ValidationType defaultType = ValidationType.WARNING;
	private boolean success = true;
	private final String agent;

	/**
	 * Creates a new validation result with the default type of the failure
	 * reasons set to "WARNING"
	 * 
	 * @param agent
	 *            the agent that failed the validation
	 */
	public ValidationResult(String agent) {
		this.agent = agent;
	}

	/**
	 * @return The agent that generate this failure
	 */
	public String getAgent() {
		return agent;
	}

	/**
	 * Returns <tt>true</tt> if the validation was successful.
	 * 
	 * @return <tt>true</tt> if the validation was successful
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Adds a failure reason to this validation result with the type specified
	 * 
	 * @param reason
	 *            One of the reasons for which the validation failed
	 * @param type
	 *            The type of failure
	 * @return <tt>true</tt> if operation was successful and <tt>false</tt> if
	 *         undefined type was provided
	 */
	public void addReason(String reason, ValidationType type) {
		if (type.isSerious())
			success = false;
		reasons.put(reason, type);
	}

	/**
	 * Adds a failure reason to this validation result with the default type
	 * 
	 * @param reason
	 *            The reason to be added
	 */
	public void addReason(String reason) {
		if (defaultType.isSerious())
			success = false;
		reasons.put(reason, defaultType);
	}

	/**
	 * @return The default type associated with reasons if no type is manually
	 *         specified
	 */
	public ValidationType getDefaultType() {
		return defaultType;
	}

	/**
	 * Sets the default type associated with new failure reasons if no type is
	 * manually specified
	 * 
	 * @param defaultType
	 *            This validator result's new default type.
	 * @return <tt>true</tt> if operation is successful and <tt>false</tt> if
	 *         undefined type was provided
	 */
	public void setDefaultType(ValidationType defaultType) {
		this.defaultType = defaultType;
	}

	/**
	 * Get the associated type for the reason specified
	 * 
	 * @param reason
	 *            The reason that you want to find out the type of
	 * @return The type of the reason as a string
	 */
	public ValidationType getType(String reason) {
		return reasons.get(reason);
	}

	/**
	 * Get all the reasons why the validation failed
	 * 
	 * @return A <tt>Set</tt> of strings containing the reasons for failure
	 */
	public Set<String> getReasons() {
		return reasons.keySet();
	}

	/**
	 * Returns the failure reasons that are of the specified types
	 * 
	 * @param types
	 *            A <tt>list</tt> of types of failures that will filter out the
	 *            reasons being returned
	 * @return A <tt>list</tt> of reasons matching the types specified
	 */
	public List<String> getReasons(List<Integer> types) {
		ArrayList<String> result = new ArrayList<String>();

		for (String reason : reasons.keySet()) {
			if (types.contains(reasons.get(reason)))
				result.add(reason);
		}

		return result;
	}

	/**
	 * returns the failure reasons that are of the specified type
	 * 
	 * @param type
	 *            the type of failures that will filter out the reasons being
	 *            returned
	 * @return a <tt>list</tt> of reasons matching the type specified
	 */
	public List<String> getReasons(ValidationType type) {
		ArrayList<String> result = new ArrayList<String>();

		for (String reason : reasons.keySet()) {
			if (reasons.get(reason).equals(type))
				result.add(reason);
		}

		return result;
	}

	@Override
	/**
	 * a string representation of the reasons and their respective types
	 * @return a string representation of this object
	 */
	public String toString() {
		String result = reasons.keySet().isEmpty() ? "The operation failed for the following reasons: \n"
				: "The operation was successful";

		for (String reason : reasons.keySet()) {
			String type = "Durrr, this shouldn't be here";
			switch (reasons.get(reason)) {
			case WARNING:
				type = "WARNING";
				break;
			case ERROR:
				type = "ERROR";
				break;
			case FATAL:
				type = "FATAL";
				break;
			}

			result += "\t[" + type + "] " + reason + "\n";
		}

		return result;
	}

}
