import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;
import cartago.OpFeedbackParam;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.aria.rlandri.generic.artifacts.*;
import org.aria.rlandri.generic.artifacts.annotation.*;

/**
 *  
 */
public class TicTacToe extends PlayerAlternatedCoordinator {

	// maximum number of games
	// the duration of all the games should be less than the permitted execution
	// time for the subenv
	int maxGames;

	int[][] gameState = new int[3][3];
	int[] score = new int[2];

	int ended() {
		for (int i = 0; i < 3; i++) {
			if (gameState[i][0] == gameState[i][1]
					&& gameState[i][1] == gameState[i][2]) {
				int cell = gameState[i][0];
				if (cell != 0)
					return cell;
			}

			if (gameState[0][i] == gameState[1][i]
					&& gameState[1][i] == gameState[2][i]) {
				int cell = gameState[0][i];
				if (cell != 0)
					return cell;
			}

		}
		if (gameState[0][0] == gameState[1][1]
				&& gameState[1][1] == gameState[2][2]) {
			int cell = gameState[0][0];
			if (cell != 0)
				return cell;
		}

		if (gameState[0][2] == gameState[1][1]
				&& gameState[1][1] == gameState[2][0]) {
			int cell = gameState[0][2];
			if (cell != 0)
				return cell;
		}

		return 0;
	}

	@GAME_OPERATION(validator = "validateMark")
	void mark(int i, int j) {
		if (currentAgent == 0) {
			gameState[i][j] = 1;
		} else {
			gameState[i][j] = -1;
		}

		int end = ended();
		if (end != 0) {
			for (int k = 0; k < 3; k++)
				for (int m = 0; m < 3; m++) {
					gameState[i][j] = 0;
				}

			if (end < 0) {
				score[0]++;
				if (score[0] >= maxGames) {
					// GAME ENDS. PLAYER 0 WINS
				}
			} else {
				score[1]++;
				if (score[1] >= maxGames) {
					// GAME ENDS. PLAYER 1 WINS
				}
			}
		}

	}
	//

	@OPERATION
	void getGameState(OpFeedbackParam<Integer[]> result) {
		Integer[] res = new Integer[9];
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				res[3 * i + j] = gameState[i][j];
			}
		result.set(res);
	}

	private void validateMark(int i, int j) {

	}


}
