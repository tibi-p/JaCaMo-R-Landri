import jason.asSemantics.Agent;
import jason.asSemantics.InternalAction;
import jason.asSemantics.DefaultInternalAction;
import java.util.ArrayList;
import java.util.List;

public class RAgent extends Agent{

	List forbidden = new ArrayList();
	{
		forbidden.add(".create_agent");
		forbidden.add(".kill_agent");
		forbidden.add(".stopMAS");
	}
	
	@SuppressWarnings("unchecked")
    public InternalAction getIA(String iaName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if(!forbidden.contains(iaName))
			return super.getIA(iaName);
		return new DefaultInternalAction();
    }
	
}

