package src.pas.uno.agents;

// SYSTEM IMPORTS
import edu.bu.pas.uno.Card;
import edu.bu.pas.uno.Game;
import edu.bu.pas.uno.Game.GameView;
import edu.bu.pas.uno.Hand;
import edu.bu.pas.uno.agents.MCTSAgent;
import edu.bu.pas.uno.enums.Value;
import edu.bu.pas.uno.moves.Move;
import edu.bu.pas.uno.tree.Node;
import edu.bu.pas.uno.tree.Node.NodeState;
import edu.bu.pas.uno.tree.Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs;
import edu.bu.pas.uno.tree.Node.NoLegalMovesIdxDefaults.DrawUnresolvedCardsIdxs;

import java.util.ArrayList;
import java.util.List;

// JAVA PROJECT IMPORTS

public class ExpectedOutcomeAgent
        extends MCTSAgent {

    public static class MCTSNode
            extends Node {
        public MCTSNode(final GameView game,
                final int logicalPlayerIdx,
                final Node parent) {
            super(game, logicalPlayerIdx, parent);
        }

        @Override
        public Node getChild(final Move move) {
            Game game = new Game(this.getGameView());
            NodeState nodeState = this.getNodeState();
            if (nodeState == NodeState.HAS_LEGAL_MOVES) {
                game.resolveMove(move);
                return new MCTSNode(game.getOmniscientView(), this.getLogicalPlayerIdx(), this);
            } else if (nodeState == NodeState.NO_LEGAL_MOVES_UNRESOLVED_CARDS_PRESENT) {
                int DrawCards = game.getUnresolvedCards().total();
                game.drawTotal(game.getCurrentPlayerHand(), DrawCards);
                game.getUnresolvedCards().clear();
                game.resolveMove(null);
                return new MCTSNode(game.getOmniscientView(), this.getLogicalPlayerIdx(), this);
            } else if (nodeState == NodeState.NO_LEGAL_MOVES_MAY_PLAY_DRAWN_CARD) {
                game.resolveMove(move);
                return new MCTSNode(game.getOmniscientView(), this.getLogicalPlayerIdx(), this);
            }
            return null;
        }
    }

    public ExpectedOutcomeAgent(final int playerIdx,
            final long maxThinkingTimeInMS) {
        super(playerIdx, maxThinkingTimeInMS);
    }

    private float rolloutToTerminalUtility(final Game simulation) {
        int steps = 0;
        final int maxSteps = Game.DEFAULT_MAX_NUM_MOVES;
        while (!simulation.isOver() && steps < maxSteps) {
            this.simulationStep(simulation);
            steps++;
        }
        return this.utilityForSearch(simulation);
    }

    private void simulationStep(final Game g) {
        MCTSNode n = new MCTSNode(g.getOmniscientView(), this.getLogicalPlayerIdx(), null);
        NodeState s = n.getNodeState();

        if (s == NodeState.HAS_LEGAL_MOVES) {
            List<Integer> leg = n.getOrderedLegalMoves();
            if (leg.isEmpty()) {
                return;
            }
            int idx = leg.get(this.getRandom().nextInt(leg.size()));
            Card c = g.getCurrentPlayerHand().getCard(idx);
            Move move;
            if (c.value() == Value.WILD || c.value() == Value.WILD_DRAW_FOUR) {
                move = Move.createMove(g.getCurrentAgent(), idx, g.chooseRandomColor());
            } else {
                move = Move.createMove(g.getCurrentAgent(), idx);
            }
            g.resolveMove(move);
            return;
        }

        if (s == NodeState.NO_LEGAL_MOVES_UNRESOLVED_CARDS_PRESENT) {
            int toDraw = g.getUnresolvedCards().total();
            g.drawTotal(g.getCurrentPlayerHand(), toDraw);
            g.getUnresolvedCards().clear();
            g.resolveMove(null);
            return;
        }

        if (s == NodeState.NO_LEGAL_MOVES_MAY_PLAY_DRAWN_CARD) {
            Hand h = g.getCurrentPlayerHand();
            if (h.size() == 0) {
                g.resolveMove(null);
                return;
            }
            if (this.getRandom().nextBoolean()) {
                int idx = h.size() - 1;
                Card c = h.getCard(idx);
                Move mv;
                if (c.value() == Value.WILD || c.value() == Value.WILD_DRAW_FOUR) {
                    mv = Move.createMove(g.getCurrentAgent(), idx, g.chooseRandomColor());
                } else {
                    mv = Move.createMove(g.getCurrentAgent(), idx);
                }
                g.resolveMove(mv);
            } else {
                g.resolveMove(null);
            }
        }
    }

    private float utilityForSearch(final Game sim) {
        int me = this.getLogicalPlayerIdx();
        if (!sim.isOver()) {
            return Float.NaN;
        }
        for (int p = 0; p < sim.getNumPlayers(); p++) {
            if (sim.getHand(p).size() == 0) {
                float u = p == me ? 1f : 0f;
                return u < 0.5f ? 0f : u;
            }
        }
        return Float.NaN;
    }

    private Move moveForHandIndex(final GameView view, final int handIdx) {
        Game g = new Game(view);
        Card c = g.getCurrentPlayerHand().getCard(handIdx);
        if (c.value() == Value.WILD || c.value() == Value.WILD_DRAW_FOUR) {
            return Move.createMove(this, handIdx, g.chooseRandomColor());
        }
        return Move.createMove(this, handIdx);
    }

    private Move moveForCurrentPlayer(final GameView view, final int handIdx) {
        Game g = new Game(view);
        edu.bu.pas.uno.agents.Agent a = g.getCurrentAgent();
        Card c = g.getCurrentPlayerHand().getCard(handIdx);
        if (c.value() == Value.WILD || c.value() == Value.WILD_DRAW_FOUR) {
            return Move.createMove(a, handIdx, g.chooseRandomColor());
        }
        return Move.createMove(a, handIdx);
    }

    private void backupQ(final MCTSNode node, final int moveIdx, final float outcome) {
        long count = node.getQCount(moveIdx);
        float total = node.getQValueTotal(moveIdx);
        node.setQCount(moveIdx, count + 1);
        node.setQValueTotal(moveIdx, total + outcome);
    }

    private void expectedOutcomeIteration(final MCTSNode rootNode) {
        if (rootNode.isTerminal()) {
            return;
        }

        final ArrayList<MCTSNode> pathNodes = new ArrayList<>();
        final ArrayList<Integer> pathQIdx = new ArrayList<>();

        MCTSNode cur = rootNode;
        final int maxPath = 6;

        for (int depth = 0; depth < maxPath && !cur.isTerminal(); depth++) {
            NodeState st = cur.getNodeState();
            GameView view = cur.getGameView();

            if (st == NodeState.HAS_LEGAL_MOVES) {
                List<Integer> leg = cur.getOrderedLegalMoves();
                if (leg.isEmpty()) {
                    break;
                }
                int moveIdx = leg.get(this.getRandom().nextInt(leg.size()));
                Move mv = this.moveForCurrentPlayer(view, moveIdx);
                pathNodes.add(cur);
                pathQIdx.add(moveIdx);
                cur = (MCTSNode) cur.getChild(mv);
            } else if (st == NodeState.NO_LEGAL_MOVES_UNRESOLVED_CARDS_PRESENT) {
                pathNodes.add(cur);
                pathQIdx.add(DrawUnresolvedCardsIdxs.MOVE_IDX);
                cur = (MCTSNode) cur.getChild(null);
            } else if (st == NodeState.NO_LEGAL_MOVES_MAY_PLAY_DRAWN_CARD) {
                Game g = new Game(view);
                Hand h = g.getCurrentPlayerHand();
                if (h.size() == 0) {
                    break;
                }
                int drawnIdx = h.size() - 1;
                boolean play = this.getRandom().nextBoolean();
                Move mv;
                int qIdx;
                if (play) {
                    Card c = h.getCard(drawnIdx);
                    if (c.value() == Value.WILD || c.value() == Value.WILD_DRAW_FOUR) {
                        mv = Move.createMove(g.getCurrentAgent(), drawnIdx, g.chooseRandomColor());
                    } else {
                        mv = Move.createMove(g.getCurrentAgent(), drawnIdx);
                    }
                    qIdx = DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX;
                } else {
                    mv = null;
                    qIdx = DrawSingleCardIdxs.KEEP_CARD_MOVE_IDX;
                }
                pathNodes.add(cur);
                pathQIdx.add(qIdx);
                cur = (MCTSNode) cur.getChild(mv);
            } else {
                break;
            }
        }

        if (pathNodes.isEmpty()) {
            return;
        }

        Game sim = new Game(cur.getGameView());
        float outcome = this.rolloutToTerminalUtility(sim);
        if (Float.isNaN(outcome)) {
            return;
        }
        for (int i = 0; i < pathNodes.size(); i++) {
            this.backupQ(pathNodes.get(i), pathQIdx.get(i), outcome);
        }
    }

    @Override
    public Node search(final GameView game,
            final Integer drawnCardIdx) {
        MCTSNode rootNode = new MCTSNode(game, this.getLogicalPlayerIdx(), null);

        final long budgetNs = Math.max(0L, this.getMaxThinkingTimeInMS()) * 1_000_000L;
        final long deadlineNs = System.nanoTime() + budgetNs;
        int safety = 0;

        if (drawnCardIdx == null) {
            do {
                this.expectedOutcomeIteration(rootNode);
                safety++;
            } while (System.nanoTime() < deadlineNs && safety < 200_000);
        } else {
            do {
                NodeState nodeState = rootNode.getNodeState();
                if (nodeState != NodeState.NO_LEGAL_MOVES_MAY_PLAY_DRAWN_CARD) {
                    break;
                }
                boolean play = this.getRandom().nextBoolean();
                Move move;
                int qIdx;
                if (play) {
                    Game g = new Game(game);
                    Card c = g.getCurrentPlayerHand().getCard(drawnCardIdx);
                    if (c.value() == Value.WILD || c.value() == Value.WILD_DRAW_FOUR) {
                        move = Move.createMove(this, drawnCardIdx, g.chooseRandomColor());
                    } else {
                        move = Move.createMove(this, drawnCardIdx);
                    }
                    qIdx = DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX;
                } else {
                    move = null;
                    qIdx = DrawSingleCardIdxs.KEEP_CARD_MOVE_IDX;
                }
                MCTSNode childNode = (MCTSNode) rootNode.getChild(move);
                Game simulation = new Game(childNode.getGameView());
                float outcome = this.rolloutToTerminalUtility(simulation);
                if (!Float.isNaN(outcome)) {
                    this.backupQ(rootNode, qIdx, outcome);
                }
                safety++;
            } while (System.nanoTime() < deadlineNs && safety < 200_000);
        }

        return rootNode;
    }

    /**
     * A method to argmax the Q values inside a {@link Node}
     *
     * @param node The {@link Node} who has populated q-values
     * @return The {@link Move} corresponding to whichever {@link Move} has the
     *         largest q-value. Note
     *         that this can be <code>null</code> if you choose to not play the
     *         drawn card (you will
     *         have to detect whether or not you are in that scenario by examining
     *         the @{link Node}'s state).
     */
    @Override
    public Move argmaxQValues(final Node node) {
        NodeState nodeState = node.getNodeState();
        GameView view = node.getGameView();

        if (nodeState == NodeState.HAS_LEGAL_MOVES) {
            List<Integer> leg = node.getOrderedLegalMoves();
            if (leg.isEmpty()) {
                return null;
            }
            int bestIdx = leg.get(0);
            float bestQ = node.getQValue(bestIdx);
            for (int i = 1; i < leg.size(); i++) {
                int idx = leg.get(i);
                float q = node.getQValue(idx);
                if (q > bestQ) {
                    bestQ = q;
                    bestIdx = idx;
                }
            }
            return this.moveForHandIndex(view, bestIdx);
        }

        if (nodeState == NodeState.NO_LEGAL_MOVES_MAY_PLAY_DRAWN_CARD) {
            float qKeep = node.getQValue(DrawSingleCardIdxs.KEEP_CARD_MOVE_IDX);
            float qPlay = node.getQValue(DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX);
            if (qPlay > qKeep) {
                int idx = new Game(view).getCurrentPlayerHand().size() - 1;
                return this.moveForHandIndex(view, idx);
            }
            return null;
        }

        return null;
    }
}
