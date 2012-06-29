import jason.asSyntax.*;
import jason.environment.TimeSteppedEnvironment;
import java.util.logging.Logger;
import java.util.List;


public class Env extends TimeSteppedEnvironment {

    private Logger logger = Logger.getLogger("env.mas2j."+Env.class.getName());
	private DBWrapper db;
	int maxSteps;
    
    @Override
    public void init(String[] args) {
		super.init(new String[] { args[1]} ); 
		addPercept(Literal.parseLiteral("a"));
		
		maxSteps = Integer.parseInt(args[0]);
		
		db = new DBWrapper();
		
        updateAgsPercept();
    }
	
	
    
    @Override
    public boolean executeAction(String ag, Structure action) {
			
			String actId = action.getFunctor();
			List <Term> terms =  action.getTerms() ;
			if (actId.equals("bet")) 
			{
				logger.info("Agent "+ag+" placed bet "+terms);
				//logger.info("terms : " + terms);
				return true;
			}
			
			return false;
    }
	
	public void stepStarted(int step)
	{

	}
	
	public void stepFinished(int step, long elapsedTime, boolean byTimeout)
	{
		//in the first step we reduce the number of agents with one because of the master agent
		if(step==0)
		{
			setNbAgs(getNbAgs()-1);
		}
		logger.info("Done step "+step+ " " + (maxSteps - 1));
		if(step==maxSteps-1)
		{
		
			try
			{
			
				int rank = db.getRank(1);
				logger.info("Old rank : "+rank);
				db.updateRank(1,110);
				rank = db.getRank(1);
				logger.info("New rank: "+rank);
				
				int economy = db.getEconomy(1);
				logger.info("Old economy: "+economy);
				db.updateEconomy(1,170);
				economy = db.getEconomy(1);
				logger.info("New economy: "+economy);
			
			}
			catch(Exception e)
			{
				logger.info(e.getMessage());
			}
			
			addPercept(Literal.parseLiteral("stop"));
			logger.info("Reached final step");
			
			
		}
	}
    
    void updatePercepts() {
        
    }

    
}
