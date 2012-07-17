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

	int[][] gameState = new int[3][3];
	int[] score = new int[2];

	boolean testCell(int cell)
	{
		if(cell==1)
		{
			score[0]++;
			return true;
		}
		else if(cell==-1)
		{
			score[1]++;
			return true;
		}
		return false;
	}

	boolean ended?()
	{
		for(int i=0;i<3;i++)
		{
			if(gameState[i][0]==gameState[i][1] && gameState[i][1]==gameState[i][2])
			{
				if(testCell(gameState[i][0])) return true;
			}

			if(gameState[0][i]==gameState[1][i] && gameState[1][i]==gameState[2][i])
			{
				if(testCell(gameState[0][i])) return true;
			}

		}
		if(gameState[0][0]==gameState[1][1] && gameState[1][1]==gameState[2][2])
		{
			if(testCell(gameState[0][0])) return true;
		}

		if(gameState[0][2]==gameState[1][1] && gameState[1][1]==gameState[2][0])
		{
			if(testCell(gameState[0][2])) return true;
		}
		
		return false;
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

		if(ended?())
		{
			for(int i=0;i<3;i++)
				for(int j=0;j<3;j++)
				{
					gameState[i][j]=0;
				}
		}

	}

	private void validateMark()
	{
		
	}

}

