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

	@OPERATION public void bet(String betName,int betValue,double sum)
	{
		String user = getOpUserName();
		
		System.out.println(user + " bets " + sum + " gold coins on " + betName);
		
		Bet bet = new Bet();
		bet.sum = sum;
		bet.type = betName;
		bet.value = betValue;	
		
		bets.put(user,bet);
	}
	
	@OPERATION public void bet(String betName,double sum)
	{
		String user = getOpUserName();
		
		System.out.println(user + " bets " + sum + " gold coins on " + betName);
		
		Bet bet = new Bet();
		bet.sum = sum;
		bet.type = betName;
		
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
			int betValue = bet.value;
			double betSum = bet.sum;
			
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
			
			if(betType.equals("Number"))
			{
				if(winningNumber==betValue)
				{
					updateStandings(player,35*betSum);
				}
				else
				{
					updateStandings(player,-betSum);
				}
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

