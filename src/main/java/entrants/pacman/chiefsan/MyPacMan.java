package entrants.pacman.chiefsan;

import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import entrants.pacman.chiefsan.evaluators.*;
import pacman.game.Constants;

import java.util.ArrayList;
import java.util.Random;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getMove() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., entrants.pacman.chiefsan).
 */
public class MyPacMan extends PacmanController {

    public MOVE getMove(Game game, long timeDue) {

        int myNodeIndex = game.getPacmanCurrentNodeIndex();

        MOVE move = checkNearByGhost(game);
        if(move != MOVE.NEUTRAL) {
            return move;
        }

        int minDistance = Integer.MAX_VALUE;
        Constants.GHOST minGhost = null;
        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            if (game.getGhostEdibleTime(ghost) > 0) {
                int distance = game.getShortestPathDistance(myNodeIndex, game.getGhostCurrentNodeIndex(ghost));

                if (distance < minDistance) {
                    minDistance = distance;
                    minGhost = ghost;
                }
            }
        }

        if (minGhost != null) {
            return game.getNextMoveTowardsTarget(
                    myNodeIndex,
                    game.getGhostCurrentNodeIndex(minGhost),
                    Constants.DM.PATH);
        }

        if (game.getCurrentMaze().graph[myNodeIndex].numNeighbouringNodes > 2) return mcts(game);
        else return nonJunctionSim(game);
    }

    /**
     * Checks for presence of ghosts around pac-man
     * @param game current game state
     * @return NEUTRAL if no ghosts found nearby else a move to escape from the ghost
     */
    public static MOVE checkNearByGhost(Game game) {

        int pacManNodeIndex = game.getPacmanCurrentNodeIndex();
        int DEFAULT_MINIMUM_DISTANCE = 20;
        int ghostLocation = -1;

        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (game.getShortestPathDistance(pacManNodeIndex, ghostLocation) <= DEFAULT_MINIMUM_DISTANCE) {
                    return game.getNextMoveAwayFromTarget(pacManNodeIndex, ghostLocation, Constants.DM.PATH);
                }
            }
        }

        return MOVE.NEUTRAL;
    }

    /**
     * Computes the next move for pac-man if it is present in a non-junction node (degree < 3)
     * @param game current game state
     */
    public static MOVE nonJunctionSim(Game game){

        int myNodeIndex = game.getPacmanCurrentNodeIndex();

        int[] powerPills = game.getPowerPillIndices();
        int[] pills = game.getPillIndices();
        ArrayList<Integer> allPills = new ArrayList<>();


        for (int i = 0; i < powerPills.length; i++) {
            Boolean checkPowerPill = game.isPowerPillStillAvailable(i);
            if (checkPowerPill != null) { // Avoids NPE

                if (checkPowerPill == true){
                    allPills.add(powerPills[i]);
                }
            }
        }

        for (int i = 0; i < pills.length; i++) {
            Boolean checkPill = game.isPillStillAvailable(i);
            if (checkPill != null) { // Avoids NPE

                if (checkPill == true){
                    allPills.add(pills[i]);
                }
            }
        }



        if (!allPills.isEmpty() && myNodeIndex != -1) {
            int[] targetPath = new int[allPills.size()];
            for(int i = 0; i < targetPath.length; i++) {
                targetPath[i] = allPills.get(i);
            }

            return game.getNextMoveTowardsTarget(
                    myNodeIndex,
                    game.getClosestNodeIndexFromNodeIndex(myNodeIndex, targetPath, Constants.DM.PATH),
                    Constants.DM.PATH);
        }

        MOVE[] moves = game.getPossibleMoves(myNodeIndex, game.getPacmanLastMoveMade());
        if (moves.length > 0) {
            Random random = new Random();
            return moves[random.nextInt(moves.length)];
        }
        return game.getPacmanLastMoveMade().opposite();
    }

    /**
     * Computes the next move for pac-man if it is present in a junction node (degree > 2)
     * @param game current game state
     */
    public static MOVE mcts(Game game) {

        MonteCarloTree tree = new MonteCarloTree(game);
        MOVE move = MOVE.NEUTRAL;

        for (int i = 0; i < 10; i++) {
            tree.simulate();
        }

        ITreeEvaluator[] additionalEvaluators = new ITreeEvaluator[] {
                new Evaluator1(),
                new Evaluator3(),
                new Evaluator4()};

        for (ITreeEvaluator evaluator: additionalEvaluators) {
            evaluator.evaluateTree(tree);
        }

        MonteCarloTreeNode node = tree.bestNode();
        if (node != null) {
            move = node.getMove();
        } else {
            move = MOVE.NEUTRAL;
        }
        tree.setRootNode(node);

        return move;
    }
}