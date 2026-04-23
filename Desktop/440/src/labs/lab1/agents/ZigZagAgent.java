package src.labs.lab1.agents;

// SYSTEM IMPORTS
import edu.bu.labs.lab1.Direction;
import edu.bu.labs.lab1.State.StateView;
import edu.bu.labs.lab1.agents.Agent;
import edu.bu.labs.lab1.Coordinate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import edu.bu.labs.lab1.Tile;

// JAVA PROJECT IMPORTS

public class ZigZagAgent
        extends Agent {

    // put your fields here! You will probably want to remember the following
    // information:
    // - all friendly unit ids (there may be more than one!)
    // - the location(s) of COIN(s) on the map

    private Integer turn; // TIS will keep track on the turn number
    private Integer myUnitId; // ID of the unit we control
    private Coordinate coinLocation; // tis is the location of the COIN just incase

    /**
     * The constructor for this type. Each agent has a unique ID that you will need
     * to use to request info from the
     * state about units it controls, etc.
     */
    public ZigZagAgent(final int agentId) {
        super(agentId); // make sure to call parent type (Agent)'s constructor!

        // initialize your fields here!
        this.turn = 0;
        this.myUnitId = null;
        this.coinLocation = null;
        // helpful printout just to help debug
        System.out.println("Constructed ZigZagAgent");
    }

    /////////////////////////////// GETTERS AND SETTERS (this is Java after all)
    /////////////////////////////// ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public final Integer getTurn() {
        return this.turn;
    }

    public final Integer getMyUnitId() {
        return this.myUnitId;
    }

    public final Coordinate getCoinLocation() {
        return this.coinLocation;
    }

    private void setTurn(Integer t) {
        this.turn = t;
    }

    private void setMyUnitId(Integer i) {
        this.myUnitId = i;
    }

    private void setCoinLocation(Coordinate c) {
        this.coinLocation = c;
    }

    /**
     * This method is called by our game engine once: before any moves are made. You
     * are provided with the state of
     * the game before any actions have been taken. This is in case you have some
     * fields you need to set but are
     * unable to in the constructor of this class (like keeping track of units on
     * the map, etc.).
     */
    @Override
    public void initializeFromState(final StateView stateView) {
        // TODO: identify units, set fields that couldn't be initialized in the
        // constructor because
        // of a lack of game data in the constructor.
        // discover my unitId
        Set<Integer> myUnitIds = new HashSet<>();
        for (Integer unitID : stateView.getUnitIds(this.getAgentId())) // for each unit on my team
        {
            myUnitIds.add(unitID);
        }

        if (myUnitIds.size() != 1) {
            System.err.println("[ERROR] ScriptedAgent.initialStep: I should control only 1 unit");
            System.exit(-1);
        }

        Coordinate coinLocation = null;
        for (int row = 0; row < stateView.getNumRows(); row++) {
            for (int col = 0; col < stateView.getNumCols(); col++) {
                Coordinate coordiate = new Coordinate(row, col);
                if (stateView.getTileState(coordiate) == Tile.State.COIN) {
                    coinLocation = coordiate;
                    break;
                }
            }
        }

        this.setMyUnitId(myUnitIds.iterator().next());
        this.setCoinLocation(coinLocation);
        this.setTurn(0);
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
     * will pick it up automatically (and the COIN will disappear from the map).
     */
    @Override
    public Map<Integer, Direction> assignActions(final StateView state) {
        Map<Integer, Direction> actions = new HashMap<>();

        // TODO: your code to give your unit(s) actions for this turn goes here!
        if (this.getMyUnitId() == null) {
            return actions;
        }

        if (this.getTurn() % 2 == 0) {
            actions.put(this.getMyUnitId(), Direction.RIGHT);
        } else {
            actions.put(this.getMyUnitId(), Direction.UP);
        }
        this.setTurn(this.getTurn() + 1);

        return actions;
    }

}
