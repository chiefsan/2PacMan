package entrants.pacman.chiefsan.evaluators;

import entrants.pacman.chiefsan.MonteCarloTreeNode;
import entrants.pacman.chiefsan.MonteCarloTree;
import entrants.pacman.chiefsan.MyPacMan;
import pacman.controllers.MASController;
import pacman.controllers.examples.po.POCommGhosts;
import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Game;

import java.util.Random;

public class Evaluator2 implements ITreeEvaluator {
    private static final int DEFAULT_BONUS = 300;
    private int bonus;

    public Evaluator2(int bonus) {
        this.bonus = bonus;
    }

    public Evaluator2() {
        this(DEFAULT_BONUS);
    }


    @Override
    public void evaluateTree(MonteCarloTree tree) {
        Game game = tree.getGameState();
        double distance = getDistanceToNeartestPill(game);

        for (MonteCarloTreeNode child: tree.getPacManChildren()) {
            game = tree.pushGameState();

            MASController ghosts = new POCommGhosts(50);
            game.advanceGame(child.getMove(), ghosts.getMove(game, 0));

            while (!game.isJunction(game.getPacmanCurrentNodeIndex())) {
                game.advanceGame(MyPacMan.nonJunctionSim(game), ghosts.getMove(game, 40));
            }
            Constants.MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
            Random random = new Random();
            game.advanceGame(possibleMoves[random.nextInt(possibleMoves.length)],
                    ghosts.getMove(game.copy(), 40));

            double d = getDistanceToNeartestPill(game);

            if (d < distance)
                child.addScoreBonus(bonus);

            tree.popGameState();
        }
    }


    private double getDistanceToNeartestPill(Game game) {
        int[] pills = game.getActivePillsIndices();

        if (pills.length == 0)
            return Double.MAX_VALUE;

        int currentIndex = game.getPacmanCurrentNodeIndex();
        int closestPill = game.getClosestNodeIndexFromNodeIndex(currentIndex, pills, DM.MANHATTAN);

        return game.getDistance(currentIndex, closestPill, DM.MANHATTAN);
    }
}
