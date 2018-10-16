package entrants.pacman.chiefsan;

/**
 * Computes UCB and selects the children
 */
public class UCBTunedSelectionPolicy {

    private static final double DEFAULT_BALANCE_PARAMETER = 10000;
    private double balanceParameter;

    /**
     * Default constructor
     */
    public UCBTunedSelectionPolicy() {
        this.balanceParameter = DEFAULT_BALANCE_PARAMETER;
    }

    /**
     * Selects the best child after performing UCB calculations
     * @param node node whose best child is to be found
     * @return the best child of the node
     */
    public MonteCarloTreeNode selectChild(MonteCarloTreeNode node)  {

        MonteCarloTreeNode selectedChild = null;
        double max = Double.NEGATIVE_INFINITY;
        double currentUcb;

        for (MonteCarloTreeNode child: node.getChildren()) {
            currentUcb = getUcbValue(child);
            if (currentUcb > max) {
                max = currentUcb;
                selectedChild = child;
            }
        }

        if (selectedChild == null)
            throw new IllegalStateException("Child cannot be selected in a leaf node!");

        return selectedChild;
    }

    public double getUcbValue(MonteCarloTreeNode node) {
//        return node.getAverageScore() + balanceParameter *
//                Math.sqrt(Math.log(node.getParent().getNumberOfVisits()) / node.getNumberOfVisits());
        return node.getAverageScore() + 1 *
                Math.sqrt(2*Math.log(node.getParent().getNumberOfVisits()) / node.getNumberOfVisits()*Math.min(0.25, node.getNumberOfVisits()));
    }
}