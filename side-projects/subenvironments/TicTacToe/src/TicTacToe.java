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
public class Roulette extends PlayerAlternatedCoordinator {

	// maximum number of games
	// the duration of all the games should be less than the permitted execution time for the subenv
	int maxGames;
	
	int[][] gameState = new int[3][3];
	int[] score = new int[2];

	int testCell(int cell)
	{
		if(cell==1)
		{
			return 1;
		}
		else if(cell==-1)
		{
			return -1;
		}
		return 0;
	}

	int ended?()
	{
		for(int i=0;i<3;i++)
		{
			if(gameState[i][0]==gameState[i][1] && gameState[i][1]==gameState[i][2])
			{
				int res = testCell(gameState[i][0]);
				if(res!=0) return res;
			}

			if(gameState[0][i]==gameState[1][i] && gameState[1][i]==gameState[2][i])
			{
				int res = testCell(gameState[0][i]);
				if(res!=0) return res;
			}

		}
		if(gameState[0][0]==gameState[1][1] && gameState[1][1]==gameState[2][2])
		{
			int res = testCell(gameState[0][0]);
			if(res!=0) return res;
		}

		if(gameState[0][2]==gameState[1][1] && gameState[1][1]==gameState[2][0])
		{
			int res = testCell(gameState[0][2]);
			if(res!=0) return res;
		}
		
		return 0;
	}	

	@GAME_OPERATION(validator = "validateMark")
	void mark(int i,int j)
	{
		if(currentAgent==0)
		{
			gameState[i][j]=1;
		}
		else
		{
			gameState[i][j]=-1;
		}

		int end = ended?();
		if(end!=0)
		{
			for(int i=0;i<3;i++)
				for(int j=0;j<3;j++)
				{
					gameState[i][j]=0;
				}
			
			if(end<0)
			{
				score[0]++;
				if(score[0]>=maxGames)
				{
					// GAME ENDS. PLAYER 0 WINS
				}
			}
			else
			{
				score[1]++;
				if(score[1]>=maxGames)
				{
					// GAME ENDS. PLAYER 1 WINS
				}
			}
		}

	}

	@GAME_OPERATION(validator = "validateGetState")
	int [] getState()
	{
		int[] result = new int[9];
		for(int i=0;i<3;i++)
			for(int j=0;j<3;j++)
			{
				result[3*i+j] = gameState[i][j];
			}
		return result;
	}

	private void validateMark()
	{
		
	}

	private void validateGetState()
	{
		
	}

}

