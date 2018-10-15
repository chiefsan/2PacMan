package entrants.pacman.chiefsan;

import pacman.controllers.MASController;
import pacman.controllers.examples.StarterPacMan;
import pacman.controllers.examples.po.POCommGhosts;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.info.GameInfo;
import pacman.game.internal.Ghost;

import java.util.*;

/**
 * Class for Monte Carlo Tree
 */
public class MonteCarloTree {
    private Game game;
    private MASController ghosts;
    private Stack<Game> gameStates;
    private MonteCarloTreeNode rootNode;
    public static Random random = new Random();
    private Set<Integer> activePowerPills;

    /**
     * Constructs an MCT based on the given game state
     * @param game game state
     */
    public MonteCarloTree(Game game) {
        Game coGame;
        GameInfo info = game.getPopulatedGameInfo();
        info.fixGhosts((ghost) -> new Ghost(
                ghost,
                game.getCurrentMaze().lairNodeIndex,
                -1,
                -1,
                MOVE.NEUTRAL
        ));
        coGame = game.getGameFromInfo(info);
        this.game = coGame;
        this.gameStates = new Stack<>();
        this.rootNode = new MonteCarloTreeNode(); // this is the root node

        this.ghosts = new POCommGhosts(50);

        this.activePowerPills = new HashSet<Integer>();
        updateActivePowerPills(game.getActivePowerPillsIndices());
    }

    /**
     *
     * @param indices
     */
    private void updateActivePowerPills(int[] indices) {
        for (int index: indices) {
            activePowerPills.add(index);
        }
    }

    public Game pushGameState() {
        gameStates.push(game);
        return game.copy();
    }

    public void popGameState() {
        game = gameStates.pop();
    }

    public Game getGameState() {
        return game;
    }

    public void setRootNode(MonteCarloTreeNode node) {
        rootNode = node;
    }

    /**
     * Performs a Monte Carlo Simulation from current game state
     */
    public void simulate() {
        List<MonteCarloTreeNode> visitedNodes = new ArrayList<>();
        int lives = this.game.getPacmanNumberOfLivesRemaining();
        pushGameState();
        try {
            MonteCarloTreeNode node = rootNode;
            visitedNodes.add(node);

            while (!node.isLeafNode()) {
                node = new UCB1SelectionPolicy().selectChild(node);

                if (node == null)
                    return;

                visitedNodes.add(node);

                game.advanceGame(node.getMove(), ghosts.getMove(game, 0));
                int[] indices = game.getActivePowerPillsIndices();
                if (indices.length != activePowerPills.size()) {
                    updateActivePowerPills(indices);
                }

                while (!game.isJunction(game.getPacmanCurrentNodeIndex())) {
                    game.advanceGame(MyPacMan.nonJunctionSim(game), ghosts.getMove(game, 10));
                }
                MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
                game.advanceGame(possibleMoves[random.nextInt(possibleMoves.length)],
                        ghosts.getMove(game.copy(), 10));
            }

            if (node.getNumberOfVisits() >= 20 || node == rootNode) { // always expand the rootnode
                node.expand(game);

                for (MonteCarloTreeNode child : node.getChildren()) {

                    int powerPillCount = game.getNumberOfActivePowerPills();
                    int pillCount = game.getNumberOfActivePills();
                    int level = game.getCurrentLevel(); // get the current level
                    MOVE move = child.getMove(); // get the childs move

                    pushGameState();

                    game.advanceGame(move, ghosts.getMove(game, 0));
                    int[] indices = game.getActivePowerPillsIndices();
                    if (indices.length != activePowerPills.size()) {
                        updateActivePowerPills(indices);
                    }

                    while (!game.isJunction(game.getPacmanCurrentNodeIndex())) {
                        game.advanceGame(MyPacMan.nonJunctionSim(game), ghosts.getMove(game, 10));
                    }
                    MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
                    game.advanceGame(possibleMoves[random.nextInt(possibleMoves.length)],
                            ghosts.getMove(game.copy(), 10));

                    if (game.getNumberOfActivePowerPills() < powerPillCount) {
                        child.setMoveEatsPowerPill(true);
                    }

                    if (game.getNumberOfActivePills() < pillCount) {
                        child.setMoveEatsPills(true);
                    }

                    int score = 0;

                    if (game.getCurrentLevel() > level)
                        score += 10000;

                    score += runSimulation(visitedNodes, lives);
                    child.updateScore(score);

                    popGameState();
                }

                node = new UCB1SelectionPolicy().selectChild(node);
                if (node == null)
                    return;

                visitedNodes.add(node);

                game.advanceGame(node.getMove(), ghosts.getMove(game, 0));
                int[] indices = game.getActivePowerPillsIndices();
                if (indices.length < activePowerPills.size()) {
                    updateActivePowerPills(indices);
                }
            }
            runSimulation(visitedNodes, lives);

        } finally {
            popGameState();
        }
    }

    /**
     * Runs the simulation for a node and updates its score based on the perfomance
     * @param visitedNodes set of all nodes visited by simulating from a node
     * @param lives number of lives initially remaining for pac-man
     * @return the score of the simulation from that node
     */
    private int runSimulation(List<MonteCarloTreeNode> visitedNodes, int lives) {
        int score = 0;

        if (game.getPacmanNumberOfLivesRemaining() < lives) {
            score -= 10000; // death penalty
        }

        score += exhaust();

        for (MonteCarloTreeNode n: visitedNodes) {
            n.updateScore(score);
        }

        return score;
    }

    /**
     * Gets the collection of children of states for the current game state
     * @return children
     */
    public Collection<MonteCarloTreeNode> getPacManChildren() {

        Collection<MonteCarloTreeNode> children;
        children = rootNode.getChildren();

        if (children != null)
            return children;
        else
            return new ArrayList<>();
    }

    /**
     * Returns the best node in the MCT by checking the scores
     * @return the best node
     */
    public MonteCarloTreeNode bestNode() {

        double currentScore, max = Double.NEGATIVE_INFINITY;
        MonteCarloTreeNode bestNode = null;
        MonteCarloTreeNode searchNode;

        searchNode = rootNode;

        if (searchNode.getChildren() != null) {
            for (MonteCarloTreeNode node: searchNode.getChildren()) {
                currentScore = node.getAverageScore();

                if (currentScore > max) {
                    max = currentScore;
                    bestNode = node;
                }
            }
        }

        return bestNode;
    }


    /**
     * Plays a game using a random pac-man/ghosts model till the end of the game
     * @return score at the end of the game
     */
    private int exhaust() {

        int level = game.getCurrentLevel();
        int i = 0;

        while (i++ < 10000
                && !game.gameOver()
                && game.getCurrentLevel() == level) {
            System.out.println(i);
            game.advanceGame(new StarterPacMan().getMove(game, 0), ghosts.getMove(game, 0));
        }

        int score = game.getScore();

        return score;
    }


}
