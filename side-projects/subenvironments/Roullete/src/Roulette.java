import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.aria.rlandri.generic.artifacts.*;
import org.aria.rlandri.generic.artifacts.annotation.*;

/**
 *      Artifact that implements the auction. 
 */
public class Roulette extends SimultaneouslyExecutedCoordinator {
    
	int [] numbers = new int[]{0,32,15,19,4,21,2,25,17,34,6,27,13,36,11,30,8,23,10,5,24,16,33,1,20,14,31,9,22,18,29,7,28,12,35,3,26};

	private final HashMap<String,Integer> payoffs = new HashMap<String,Integer>();

	{
		payoffs.put("0",35);
		payoffs.put("Single",35);
		payoffs.put("Split",17);
		payoffs.put("Street",11);
		payoffs.put("Corner",8);
		payoffs.put("Six",5);
		payoffs.put("Column1",2);
		payoffs.put("Column2",2);
		payoffs.put("Column3",2);
		payoffs.put("Dozen1",2);
		payoffs.put("Dozen2",2);
		payoffs.put("Dozen3",2);
	}
    
	HashMap<String,Bet> bets = new HashMap<String,Bet>();
	HashMap<String,Double> standings = new HashMap<String,Double>();
	
	String winningColor;
	int winningNumber;
	

	private double payoff(String betType,double betSum)
	{
		return betSum*payoffs.get(betType).intValue();
	}
	
	private void updateStandings(String player, double value)
	{
		if(standings.containsKey(player))
		{
			double oldValue = standings.get(player);
			standings.put(player,oldValue+value);
		}
		else
		{
			standings.put(player,value);
		}
	}

	@GAME_OPERATION(validator = "validateBet")
	void bet(String betName,double sum)
	{
		System.out.println("AJUNG AICI BITCH");
		this.bet(betName,null,sum);
	}

	void validateBet(String betName,double sum)
	{
		System.out.println("Validating BIATCH");
	}	

	@GAME_OPERATION(validator = "validateBet")
	void bet(String betName, Object betValues[], double sum)
	{

		String user = getOpUserName();
		
		System.out.println(user + " bets " + sum + " gold coins on " + betName);
		
		Bet bet = new Bet();
		System.out.println("MONEY: "+sum+"AGENT: "+getOpUserName());
		bet.sum = sum;
		bet.type = betName;
		bet.betValues = betValues;
		bets.put(user,bet);
		
	}

	void validateBet(String betName, Object betValues[], double sum)
	{
		System.out.println("Validating BIATCH!");
	}
	
    	@GAME_OPERATION(validator = "validateSpinWheel")
	public void spinWheel() {
	
		String user = getOpUserName();
		if(!user.equals("master"))
			return;
	
		int value = (int)(Math.random()*37);
		
		winningNumber = numbers[value];
		winningColor = "red";
		if(value%2==0)
		{
			if(value==0)
				winningColor="green";
			winningColor = "black";
		}
	
		System.out.println("Winning number: "+winningNumber + " and color: " +  winningColor);
    }
	
	void validateSpinWheel()
	{
		
	}
	
	
	//TODO: Validation is not done here (eg. users can win with ill formed bets)!!!!!!!!!!!!!!
	@GAME_OPERATION(validator = "validatePayout")	
	public void payout()
	{

		String user = getOpUserName();
		if(!user.equals("master"))
			return;
			
		Set<String> players = bets.keySet();
		for(Iterator it = players.iterator();it.hasNext();)
		{
			String player = (String)it.next();
			Bet bet = bets.get(player);

			
			
			String betType = bet.type;
			double betSum = bet.sum;
			Object[] values = bet.betValues;

			boolean won = false;
			
			if(betType.equals("0"))
			{
				if(winningNumber==0)
					won = true;
			}
			
			if(betType.equals("Single"))
			{
				int betValue = ((Number)values[0]).intValue();				
				if(winningNumber==betValue)
					won = true;
			}

			if(betType.equals("Split"))
			{
				
				for(int i=0;i<values.length;i++)
				{
					int value = ((Number)values[i]).intValue();
					if(winningNumber==value)
					{
						won = true;
						break;
					}
				}	
			}

			if(betType.equals("Street"))
			{
				
			}

			if(betType.equals("Corner"))
			{
				
			}

			if(betType.equals("Six"))
			{

			}

			if(betType.equals("Column1"))
			{
				
			}
			if(betType.equals("Column2"))
			{
				
			}

			if(betType.equals("Column3"))
			{
				
			}

			if(betType.equals("Dozen1"))
			{
				
			}

			if(betType.equals("Dozen2"))
			{
				
			}

			if(betType.equals("Dozen3"))
			{
				
			}

			if(betType.equals("Manque"))
			{
				if(winningNumber>=1 && winningNumber<=18)
					won = true;
			}
			
			if(betType.equals("Passe"))
			{
				if(winningNumber>=19 && winningNumber<=36)
					won = true;
			}
			
			if(betType.equals("Red"))
			{
				if(winningColor.equals("red"))
					won = true;
			}
			
			if(betType.equals("Black"))
			{
				if(winningColor.equals("black"))
					won = true;
			}
			
			if(betType.equals("Odd"))
			{
				if(winningNumber%2==1)
					won = true;
			}
			
			if(betType.equals("Even"))
			{
				if(winningNumber%2==0 && winningNumber>0)
					won = true;
			}

			if(won)
				updateStandings(player,payoff(betType,betSum));
			else
				updateStandings(player,-betSum);
		}
		bets = new HashMap<String,Bet>();
		System.out.println(standings);
	}

	void validatePayout()
	{
	
	}

}

