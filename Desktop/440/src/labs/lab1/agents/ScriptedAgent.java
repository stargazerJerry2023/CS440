package src.labs.lab1.agents;

// SYSTEM IMPORTS
import edu.bu.labs.lab1.Coordinate;
import edu.bu.labs.lab1.Direction;
import edu.bu.labs.lab1.Tile;
import edu.bu.labs.lab1.State.StateView;
import edu.bu.labs.lab1.agents.Agent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// JAVA PROJECT IMPORTS

public class ScriptedAgent
        extends Agent {
    private int dx;
    private int dy;
    private Integer myUnitId; // id of the unit we control (used to lookop UnitView from state)
    private Coordinate coinLocation; // Coordinate of the COIN (only one) on the map
    private int Turn;

    /**
     * The constructor for this type. Each agent has a unique ID that you will need
     * to use to request info from the
     * state about units it controls, etc.
     */
    public ScriptedAgent(final int agentId) {
        super(agentId); // make sure to call parent type (Agent)'s constructor!

        // even though we have fields for my unit's ID and the COIN's location
        // we don't have access to that information in the constructor. So, we are
        // forced to rely on the
        // initializeFromState method (where we have access to the state of the game) to
        // set these fields.
        // You should think of the method initializeFromState as a "secondary
        // constructor"
        this.myUnitId = null;
        this.coinLocation = null;
        this.dx = 0;
        this.dy = 0;
        this.Turn = 0;
        // helpful printout just to help debug
        System.out.println("Constructed ScriptedAgent");
    }

    /////////////////////////////// GETTERS AND SETTERS (this is Java after all)
    /////////////////////////////// ///////////////////////////////
    public final Integer getMyUnitId() {
        return this.myUnitId;
    }

    public final Coordinate getCoinLocation() {
        return this.coinLocation;
    }

    private void setMyUnitId(Integer i) {
        this.myUnitId = i;
    }

    private void setCoinLocation(Coordinate c) {
        this.coinLocation = c;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Agents in Sepia have five abstract methods that we must override. The first
     * three are the most important:
     * - initialStep
     * - middleStep
     * - terminalStep
     * When a new game is started, the Agent objects are created using their
     * constructors. However, the game itself
     * has not been started yet, so if an Agent wishes to keep track of
     * units/resources in the game, that info is
     * not available to it yet. The first turn of the game, each agent's initialStep
     * method is called by sepia
     * automatically and provided the *initial* state of the game. So, this method
     * is often used as a
     * secondary constructor to "discover" the ids of units we control, the number
     * of players in the game, etc.
     *
     * This method produces a mapping from the ids of units we control to the
     * actions those units will take this turn.
     * Note we are only allowed to map units that we *control* (their ids
     * technically) to actions, we are not allowed
     * to try to control units that aren't on our team!
     */
    @Override
    public void initializeFromState(final StateView stateView) {
        coinLocation = null;

        // discover friendly units
        Set<Integer> myUnitIds = new HashSet<>();
        for (Integer unitID : stateView.getUnitIds(this.getAgentId())) // for each unit on my team
        {
            myUnitIds.add(unitID);
        }

        // check that we only have a single unit
        if (myUnitIds.size() != 1) {
            System.err.println("[ERROR] ScriptedAgent.initialStep: I should control only 1 unit");
            System.exit(-1);
        }

        // TODO: discover the location of the coin. See the documentation!
        for (int row = 0; row < stateView.getNumRows(); row++) {
            for (int col = 0; col < stateView.getNumCols(); col++) {
                Coordinate coordiate = new Coordinate(row, col);
                if (stateView.getTileState(coordiate) == Tile.State.COIN) {
                    coinLocation = coordiate;
                    break;
                }
            }
        }

        // set our fields
        this.setMyUnitId(myUnitIds.iterator().next());
        this.setCoinLocation(coinLocation);
    }

    /**
     * This method is called every turn (or "frame") of the game. Your agent is
     * responsible for assigning
     * actions to each of the unit(s) your agent controls. The return type of this
     * method is a mapping
     * from unit ID (that your agent controls) to the Direction you want that unit
     * to move in.
     *
     * If you are trying to collect COIN(s), you do so by walking into the same
     * square as a COIN. Your agent
     * will pick it up automatically (and the COIN will dissapear from the map).
     */
    @Override
    public Map<Integer, Direction> assignActions(final StateView state) {
        Map<Integer, Direction> actions = new HashMap<>();

        // TODO: your code to give your unit(s) actions for this turn goes here!
        if (this.getMyUnitId() == null) {
            return actions;
        }

        /*
         * Using the coin location coordinates, we can determine where the
         * unit should move based on the step numbers
         */

        if (this.coinLocation != null) {
            int x = this.coinLocation.row();
            int y = this.coinLocation.col();

            if (this.dx < x - 1) {
                actions.put(this.getMyUnitId(), Direction.UP);
                this.dx = this.dx + 1;
            } else if (this.dy < y - 1) {
                actions.put(this.getMyUnitId(), Direction.RIGHT);
                this.dy = this.dy + 1;
            }
            /*
             * After we reached the coin, we need to move up and right
             * to now exist the map based on the coin
             * coordinate location
             */
            else if (this.dx < x + 1) {
                actions.put(this.getMyUnitId(), Direction.UP);
                this.dx = this.dx + 1;
            } else if (this.dy < y + 2) {
                actions.put(this.getMyUnitId(), Direction.RIGHT);
                this.dy = this.dy + 1;
            }

        } else {
            if (this.Turn < 5) {
                actions.put(this.getMyUnitId(), Direction.UP);
                this.Turn = this.Turn + 1;
            } else {
                actions.put(this.getMyUnitId(), Direction.RIGHT);
                this.Turn = this.Turn + 1;
            }

        }
        return actions;
    }

}
