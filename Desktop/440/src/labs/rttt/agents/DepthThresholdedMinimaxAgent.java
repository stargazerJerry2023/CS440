package src.labs.rttt.agents;

// SYSTEM IMPORTS
import edu.bu.labs.rttt.agents.SearchAgent;
import edu.bu.labs.rttt.game.PlayerType;
import edu.bu.labs.rttt.game.RecursiveTicTacToeGame.RecursiveTicTacToeGameView;
import edu.bu.labs.rttt.traversal.Node;

import java.util.List;

// JAVA PROJECT IMPORTS
import src.labs.rttt.heuristics.Heuristics;

public class DepthThresholdedMinimaxAgent
        extends SearchAgent {

    public static final int DEFAULT_MAX_DEPTH = 3;

    private int maxDepth;

    public DepthThresholdedMinimaxAgent(PlayerType myPlayerType) {
        super(myPlayerType);
        this.maxDepth = DEFAULT_MAX_DEPTH;
    }

    public final int getMaxDepth() {
        return this.maxDepth;
    }

    public void setMaxDepth(int i) {
        this.maxDepth = i;
    }

    public String getTabs(Node node) {
        StringBuilder b = new StringBuilder();
        for (int idx = 0; idx < node.getDepth(); ++idx) {
            b.append("\t");
        }
        return b.toString();
    }

    public Node minimax(Node node) {
        // uncomment if you want to see the tree being made
        // System.out.println(this.getTabs(node) + "Node(currentPlayer=" +
        // node.getCurrentPlayerType() +
        // " isTerminal=" + node.isTerminal() + " lastMove=" + node.getLastMove() +
        // ")");

        // search must return a child of the input node whose incoming edge
        // corresponds to the chosen move, so we use a helper that returns
        // utility values and select the best child here.

        // if this state is already terminal, there is no move to make; just
        // return the node itself (search is never called on terminal nodes in
        // normal play, but this is a safe fallback).
        if (node.isTerminal()) {
            return node;
        }

        boolean maximizing = node.getCurrentPlayerType() == this.getMyPlayerType();
        List<Node> children = node.getChildren();

        if (children == null || children.isEmpty()) {
            // no legal moves; again, should not happen in normal play
            return node;
        }

        Node bestChild = null;
        double bestValue = maximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (Node child : children) {
            double value = this.minimaxValue(child);

            // for non-terminal nodes we are responsible for setting the utility
            if (!child.isTerminal()) {
                child.setUtilityValue(value);
            }

            if (maximizing) {
                if (value > bestValue || bestChild == null) {
                    bestValue = value;
                    bestChild = child;
                }
            } else {
                if (value < bestValue || bestChild == null) {
                    bestValue = value;
                    bestChild = child;
                }
            }
        }

        return bestChild;
    }

    /**
     * Recursive depth-thresholded minimax that returns the utility value of the
     * given node while respecting the depth limit and heuristic bounds.
     */
    private double minimaxValue(Node node) {
        // terminal utilities are already set for us and are in {-100, 0, +100}
        if (node.isTerminal()) {
            return node.getUtilityValue();
        }

        // depth threshold: use heuristic strictly inside (-100, +100)
        if (node.getDepth() >= this.maxDepth) {
            double heuristicValue = Heuristics.calculateHeuristicValue(node);
            node.setUtilityValue(heuristicValue);
            return heuristicValue;
        }

        boolean maximizing = node.getCurrentPlayerType() == this.getMyPlayerType();
        List<Node> children = node.getChildren();

        // if no children, treat as a leaf and just return current utility (should
        // not happen in normal play).
        if (children == null || children.isEmpty()) {
            return node.getUtilityValue();
        }

        double bestValue = maximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (Node child : children) {
            double value = minimaxValue(child);
            if (maximizing) {
                if (value > bestValue) {
                    bestValue = value;
                }
            } else {
                if (value < bestValue) {
                    bestValue = value;
                }
            }
        }

        node.setUtilityValue(bestValue);
        return bestValue;
    }

    public Node search(Node node) {
        return this.minimax(node);
    }

    @Override
    public void afterGameEnds(final RecursiveTicTacToeGameView game) {
    }
}
