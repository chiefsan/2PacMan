package entrants.pacman.chiefsan;

import java.util.*;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Class for node in the Monte Carlo Tree.
 */
public class MonteCarloTreeNode {
    private MOVE move;
    private MonteCarloTreeNode parent;
    private int numberOfVisits;
    private int scoreBonus;
    private Map<Object, MonteCarloTreeNode> children;
    private double mean;
    private boolean moveEatsPowerPill;
    private boolean moveEatsPills;

    /**
     * Default Constructor
     */
    public MonteCarloTreeNode() {
        this.numberOfVisits = 0;
        this.move = MOVE.NEUTRAL;
        this.scoreBonus = 0;
        this.parent = null;
    }

    /**
     * Constructor for children
     * @param parent The parent of the node
     * @param move The move contained in the node
     */
    public MonteCarloTreeNode(MonteCarloTreeNode parent, MOVE move) {
        this.parent = parent;
        this.move = move;
    }

    /**
     * Updates the mean score as well as the number of visits to the node
     * @param score Score in the visit to be updated
     */
    public void updateScore(int score) {
        this.mean = (this.mean*this.numberOfVisits+score)/(this.numberOfVisits+1);
        this.numberOfVisits += 1;
    }

    /**
     * Adds bonus to the mean score
     * @param bonus Bonus given by the evaluators
     */
    public void addScoreBonus(int bonus) {
        this.scoreBonus += bonus;
    }

    /**
     * Expands the game from the current node based on the set of moves available
     * @param game Current game state
     */
    public void expand(Game game) {
        MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
        this.children = new HashMap<>(possibleMoves.length);

        for (MOVE move: possibleMoves) {
            this.children.put(move, new MonteCarloTreeNode(this, move));
        }
    }

    /**
     * Indicates whether this node is a leaf node
     * @return True if node is a leaf; False otherwise
     */
    public boolean isLeafNode() {
        return this.children==null;
    }

    /**
     * Gets the move contained in this node
     * @return move
     */
    public MOVE getMove() {
        return this.move;
    }

    /**
     * Gets the number of visits to this node
     * @return number of visits
     */
    public int getNumberOfVisits() {
        return this.numberOfVisits;
    }

    /**
     * Gets the children of this node
     * @return values of children, if any; null otherwise
     */
    public Collection<MonteCarloTreeNode> getChildren() {
        if (this.children==null)
            return null;
        return this.children.values();
    }

    /**
     * Gets the parent of this node
     * @return parent
     */
    public MonteCarloTreeNode getParent() {
        return this.parent;
    }

    /**
     * Gets the average score of the node
     * @return average score
     */
    public double getAverageScore() {
        if (this.numberOfVisits>0)
            return this.mean+this.scoreBonus;
        return this.scoreBonus;
    }

    /**
     * Indicates whether the current move eats any power pills
     * @return true if current move eats any power pills; false otherwise
     */
    public boolean isMoveEatsPowerPill() {
        return this.moveEatsPowerPill;
    }

    /**
     * Sets indicator variable to true if current move eats any power pill, false otherwise
     * @param moveEatsPowerPill
     */
    public void setMoveEatsPowerPill(boolean moveEatsPowerPill) {
        this.moveEatsPowerPill = moveEatsPowerPill;
    }

    /**
     * Indicates whether current move eats any pills
     * @return true if current move eats any pills; false otherwise
     */
    public boolean isMoveEatsPills() {
        return this.moveEatsPills;
    }

    /**
     * Sets indicator variable to true if current move eats any pill, false otherwise
     * @param moveEatsPills
     */
    public void setMoveEatsPills(boolean moveEatsPills) {
        this.moveEatsPills = moveEatsPills;
    }

    /**
     * Indicates whether any of the children has moves that eat pills
     * @return true if any of the children has any move that can eat pills, false otherwise
     */
    public boolean isEatPillsInFuture() {
        if (this.children==null)
            return false;
        for (MonteCarloTreeNode child: this.children.values()) {
            if (child.isMoveEatsPills())
                return true;
        }
        return false;
    }
}
