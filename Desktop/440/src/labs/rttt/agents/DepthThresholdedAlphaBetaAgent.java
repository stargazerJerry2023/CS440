package src.labs.rttt.agents;


// SYSTEM IMPORTS
import edu.bu.labs.rttt.agents.SearchAgent;
import edu.bu.labs.rttt.game.PlayerType;
import edu.bu.labs.rttt.game.RecursiveTicTacToeGame.RecursiveTicTacToeGameView;
import edu.bu.labs.rttt.traversal.Node;

import java.util.List;


// JAVA PROJECT IMPORTS
import src.labs.rttt.heuristics.Heuristics;
import src.labs.rttt.ordering.MoveOrderer;


public class DepthThresholdedAlphaBetaAgent
    extends SearchAgent
{

    public static final int DEFAULT_MAX_DEPTH = 3;

    private int maxDepth;

    public DepthThresholdedAlphaBetaAgent(PlayerType myPlayerType)
    {
        super(myPlayerType);
        this.maxDepth = DEFAULT_MAX_DEPTH;
    }

    public final int getMaxDepth() { return this.maxDepth; }
    public void setMaxDepth(int i) { this.maxDepth = i; }

    public String getTabs(Node node)
    {
        StringBuilder b = new StringBuilder();
        for(int idx = 0; idx < node.getDepth(); ++idx)
        {
            b.append("\t");
        }
        return b.toString();
    }

    /**
     * Alpha-beta search. Utility extrema must match game extrema: +100 (win), -100 (lose), 0 (tie).
     * Only terminal nodes may have these values; at depth limit use the heuristic, which must
     * stay strictly in (-100, +100) so we never assign extrema for non-terminal states.
     */
    public Node alphaBeta(Node node,
                          double alpha,
                          double beta)
    {
        // uncomment if you want to see the tree being made
        // System.out.println(this.getTabs(node) + "Node(currentPlayer=" + node.getCurrentPlayerType() +
        //      " isTerminal=" + node.isTerminal() + " lastMove=" + node.getLastMove() + ")");

        // if this state is already terminal, there is no move to make; just
        // return the node itself (search is never called on terminal nodes in
        // normal play, but this is a safe fallback).
        if (node.isTerminal())
        {
            return node;
        }

        boolean maximizing = node.getCurrentPlayerType() == this.getMyPlayerType();
        List<Node> children = MoveOrderer.orderChildren(node.getChildren());

        if (children == null || children.isEmpty())
        {
            return node;
        }

        Node bestChild = null;
        double bestValue = maximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (Node child : children)
        {
            double value = this.alphaBetaValue(child, alpha, beta);

            if (!child.isTerminal())
            {
                child.setUtilityValue(value);
            }

            if (maximizing)
            {
                if (value > bestValue || bestChild == null)
                {
                    bestValue = value;
                    bestChild = child;
                }
                alpha = Math.max(alpha, bestValue);
            }
            else
            {
                if (value < bestValue || bestChild == null)
                {
                    bestValue = value;
                    bestChild = child;
                }
                beta = Math.min(beta, bestValue);
            }

            if (beta <= alpha)
            {
                break;
            }
        }

        return bestChild;
    }

    public Node search(Node node)
    {
        return this.alphaBeta(node, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Override
    public void afterGameEnds(final RecursiveTicTacToeGameView game) {}

    /**
     * Recursive alpha-beta search that returns the utility value of the given
     * node while respecting the depth limit and heuristic bounds.
     */
    private double alphaBetaValue(Node node, double alpha, double beta)
    {
        if (node.isTerminal())
        {
            return node.getUtilityValue();
        }

        if (node.getDepth() >= this.maxDepth)
        {
            double heuristicValue = Heuristics.calculateHeuristicValue(node);
            node.setUtilityValue(heuristicValue);
            return heuristicValue;
        }

        boolean maximizing = node.getCurrentPlayerType() == this.getMyPlayerType();
        List<Node> children = MoveOrderer.orderChildren(node.getChildren());

        if (children == null || children.isEmpty())
        {
            return node.getUtilityValue();
        }

        double bestValue = maximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (Node child : children)
        {
            double value = alphaBetaValue(child, alpha, beta);

            if (maximizing)
            {
                if (value > bestValue)
                {
                    bestValue = value;
                }
                alpha = Math.max(alpha, bestValue);
            }
            else
            {
                if (value < bestValue)
                {
                    bestValue = value;
                }
                beta = Math.min(beta, bestValue);
            }

            if (beta <= alpha)
            {
                break;
            }
        }

        node.setUtilityValue(bestValue);
        return bestValue;
    }
}
