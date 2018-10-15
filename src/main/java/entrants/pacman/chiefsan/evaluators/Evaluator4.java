package entrants.pacman.chiefsan.evaluators;

import entrants.pacman.chiefsan.MonteCarloTreeNode;
import entrants.pacman.chiefsan.MonteCarloTree;

public class Evaluator4 implements ITreeEvaluator {

    private static final int RULE1_DEFAULT_BONUS = 400;
    private static final int RULE2_DEFAULT_BONUS = 300;
    private int rule1Bonus, rule2Bonus;


    public Evaluator4(int rule1Bonus, int rule2Bonus) {
        this.rule1Bonus = rule1Bonus;
        this.rule2Bonus = rule2Bonus;
    }

    public Evaluator4() {
        this(RULE1_DEFAULT_BONUS, RULE2_DEFAULT_BONUS);
    }

    @Override
    public void evaluateTree(MonteCarloTree simulator) {
        Evaluator2 eval = new Evaluator2();

        for (MonteCarloTreeNode child: simulator.getPacManChildren()) {
            if (child.isMoveEatsPills()) {
                child.addScoreBonus(rule1Bonus);
            }
            else if (child.isEatPillsInFuture()) {
                child.addScoreBonus(rule2Bonus);
            }
            else {
                eval.evaluateTree(simulator);
            }
        }
    }
}
