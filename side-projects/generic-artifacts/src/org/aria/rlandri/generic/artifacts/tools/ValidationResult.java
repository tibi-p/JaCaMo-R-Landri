package org.aria.rlandri.generic.artifacts.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ValidationResult {
	public final static int WARNING = 0, ERROR = 1, FATAL = 2, limit = 2;
	private HashMap<String, Integer> reasons;
	private int defaultType;
	private String agent;
	public int index;

	/**
	 * Creates a new validation result with the default type of the failure reasons set to "WARNING"
	 * @param agent the agent that failed the validation
	 */
	public ValidationResult(String agent) {
		reasons = new HashMap<String, Integer>();
		defaultType = WARNING;
		this.agent = agent;
		index = 0;
	}
	
	/**
	 * @return The agent that generate this failure
	 */
	public String getAgent() {
		return agent;
	}
	
	/**
	 * Adds a failure reason to this validation result with the type specified
	 * @param reason One of the reasons for which the validation failed
	 * @param type The type of failure
	 * @return <tt>true</tt> if operation was successful and <tt>false</tt> if undefined type was provided
	 */
	
	public boolean addReason(String reason, int type){
		if(type > limit) return false;
		reasons.put(reason, type);
		return true;
	}
	/**
	 * Adds a failure reason to this validation result with the default type
	 * @param reason The reason to be added
	 */
	public void addReason(String reason){
		reasons.put(reason, defaultType);
	}
	/**
	 * @return The default type associated with reasons if no type is manually specified
	 */
	public int getDefaultType() {
		return defaultType;
	}

	/**
	 * Sets the default type associated with new failure reasons if no type is manually specified
	 * @param defaultType This validator result's new default type.
	 * @return <tt>true</tt> if operation is successful and <tt>false</tt> if undefined type was provided
	 */
	public boolean setDefaultType(int defaultType) {
		if(defaultType > limit) return false;
		this.defaultType = defaultType;
		return true;
	}
	/**
	 * Get the associated type for the reason specified
	 * @param reason The reason that you want to find out the type of
	 * @return The type of the reason as a string
	 */
	public String getType(String reason){
		String type = "Durrr, this shouldn't be here";
		switch(reasons.get(reason)){
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
		
		return type;
	}
	
	/**
	 * Get all the reasons why the validation failed
	 * @return An <tt>ArrayList</tt> of strings containing the reasons for failure
	 */
	public List<String> getReasons(){
		return new ArrayList<String>(reasons.keySet());
	}
	
	/**
	 * Returns the failure reasons that are of the specified types
	 * @param types A <tt>list</tt> of types of failures that will filter out the reasons being returned
	 * @return A <tt>list</tt> of reasons matching the types specified
	 */
	public List<String> getReasons(List<Integer> types){
		ArrayList<String> result = new ArrayList<String>();
		
		for(String reason : reasons.keySet()){
			if(types.contains(reasons.get(reason))) result.add(reason);
		}
		
		return result;
	}
	
	/**
	 * returns the failure reasons that are of the specified type
	 * @param type the type of failures that will filter out the reasons being returned
	 * @return a <tt>list</tt> of reasons matching the type specified
	 */
	public List<String> getReasons(int type){
		ArrayList<String> result = new ArrayList<String>();
		
		for(String reason : reasons.keySet()){
			if(reasons.get(reason) == type) result.add(reason);
		}
		
		return result;
	}
	@Override
	/**
	 * a string representation of the reasons and their respective types
	 * @return a string representation of this object
	 */
	public String toString(){
		String result = reasons.keySet().isEmpty() ? "The operation failed for the following reasons: \n" 
													: "The operation was successful"; 
		
		for(String reason : reasons.keySet()){
			String type = "Durrr, this shouldn't be here";
			switch(reasons.get(reason)){
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
