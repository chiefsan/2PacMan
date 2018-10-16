package entrants.pacman.chiefsan.evaluators;

import java.util.Collection;

import entrants.pacman.chiefsan.MonteCarloTreeNode;
import entrants.pacman.chiefsan.MonteCarloTree;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Evaluator1 implements ITreeEvaluator {

    private int ghostScore, pillScore;
    private static final int DEFAULT_GHOST_SCORE = 400;
    private static final int DEFAULT_PILL_SCORE = 200;

    public Evaluator1(int ghostScore, int pillScore) {
        this.ghostScore = ghostScore;
        this.pillScore = pillScore;
    }

    public Evaluator1() {
        this(DEFAULT_GHOST_SCORE, DEFAULT_PILL_SCORE);
    }

    @Override
    public void evaluateTree(MonteCarloTree simulator) {
        Collection<MonteCarloTreeNode> children = simulator.getPacManChildren();

        if (children.size() == 0)
            return;

        Game game = simulator.getGameState();

        MOVE ghostMove = getMoveTowardsEdibleGhost(game);

        if (ghostMove != MOVE.NEUTRAL) {
            addBonus(children, ghostMove, ghostScore);
        }

        MOVE pillMove = getMoveTowardsPill(game);
        addBonus(children, pillMove, pillScore);
    }


    private MOVE getMoveTowardsEdibleGhost(Game game) {

        int currentIndex = game.getPacmanCurrentNodeIndex();
        int min = Integer.MAX_VALUE;
        int closestGhostIndex = -1;
        int distance;
        int ghostIndex;

        for (GHOST ghost: GHOST.values()) {

            if (game.getGhostEdibleTime(ghost) > 0) {
                ghostIndex = game.getGhostCurrentNodeIndex(ghost);
                distance = game.getShortestPathDistance(currentIndex, ghostIndex);

                if (distance < min) {
                    min = distance;
                    closestGhostIndex = ghostIndex;
                }
            }
        }

        if (closestGhostIndex > -1) {
            return game.getNextMoveTowardsTarget(currentIndex, closestGhostIndex, DM.PATH);
        }
        else {
            return MOVE.NEUTRAL;
        }
    }


    private MOVE getMoveTowardsPill(Game game) {

        int currentIndex = game.getPacmanCurrentNodeIndex();
        int[] pills = game.getActivePillsIndices();
        int closestIndex = game.getClosestNodeIndexFromNodeIndex(currentIndex, pills, DM.PATH);

        return game.getNextMoveTowardsTarget(currentIndex, closestIndex, DM.PATH);
    }


    private void addBonus(Collection<MonteCarloTreeNode> children, MOVE move, int bonus) {

        for (MonteCarloTreeNode node: children) {
            if (node.getMove() == move) {
                node.addScoreBonus(bonus);
                return;
            }
        }
    }
}
