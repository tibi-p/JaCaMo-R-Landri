import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
/**
 *      Artifact that implements the auction. 
 */
public class Roulette extends Artifact {
    
	int [] numbers = new int[]{0,32,15,19,4,21,2,25,17,34,6,27,13,36,11,30,8,23,10,5,24,16,33,1,20,14,31,9,22,18,29,7,28,12,35,3,26};

	private final HashMap<String,Integer> payouts = new HashMap<String,Integer>();

	{
		payouts.put("0",35);
		payouts.put("Single",35);
		payouts.put("Split",17);
		payouts.put("Street",11);
		payouts.put("Corner",8);
		payouts.put("Six",5);
	}
    
	HashMap<String,Bet> bets = new HashMap<String,Bet>();
	HashMap<String,Double> standings = new HashMap<String,Double>();
	
	String winningColor;
	int winningNumber;
	
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
	
    	@OPERATION public void init() 
	{

    	}

	@OPERATION public void bet(String betName,double sum)
	{
		this.bet(betName,null,sum);
	}	

	@OPERATION public void bet(String betName, Object betValues[], double sum)
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
	
    @OPERATION public void spinWheel() {
	
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
	
	//TODO: Validation is not done here (eg. users can win with ill formed bets)!!!!!!!!!!!!!!
	@OPERATION public void payout()
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
			
			if(betType.equals("0"))
			{
				if(winningNumber==0)
				{
					updateStandings(player,35*betSum);
				}
				else
				{
					updateStandings(player,-betSum);
				}
			}
			
			if(betType.equals("Single"))
			{

				
				int betValue = ((Number)values[0]).intValue();				

				if(winningNumber==betValue)
				{
					updateStandings(player,35*betSum);
				}
				else
				{
					updateStandings(player,-betSum);
				}
			}

			if(betType.equals("Split"))
			{
				boolean won = false;
				for(int i=0;i<values.length;i++)
				{
					int value = ((Number)values[i]).intValue();
					if(winningNumber==value)
					{
						won = true;
						break;
					}
				}
				if(won)
					updateStandings(player,17*betSum);
				else
					updateStandings(player,-betSum);	
			}
			
			if(betType.equals("Manque"))
			{
				if(winningNumber>=1 && winningNumber<=18)
				{
					updateStandings(player,betSum);
				}
				else
				{
					updateStandings(player,-betSum);
				}
			}
			
			if(betType.equals("Passe"))
			{
				if(winningNumber>=19 && winningNumber<=36)
				{
					updateStandings(player,betSum);
				}
				else
				{
					updateStandings(player,-betSum);
				}
			}
			
			if(betType.equals("Red"))
			{
				if(winningColor.equals("red"))
				{
					updateStandings(player,betSum);
				}
				else
				{
					updateStandings(player,-betSum);
				}
			}
			
			if(betType.equals("Black"))
			{
				if(winningColor.equals("black"))
				{
					updateStandings(player,betSum);
				}
				else
				{
					updateStandings(player,-betSum);
				}
			}
			
			if(betType.equals("Odd"))
			{
				if(winningNumber%2==1)
				{
					updateStandings(player,betSum);
				}
				else
				{
					updateStandings(player,-betSum);
				}
			}
			
			if(betType.equals("Even"))
			{
				if(winningNumber%2==0 && winningNumber>0)
				{
					updateStandings(player,betSum);
				}
				else
				{
					updateStandings(player,-betSum);
				}
			}
		}
		bets = new HashMap<String,Bet>();
		System.out.println(standings);
	}
}

