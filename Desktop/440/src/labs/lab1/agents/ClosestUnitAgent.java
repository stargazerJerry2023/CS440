package src.labs.lab1.agents;

// SYSTEM IMPORTS
import edu.bu.labs.lab1.Direction;
import edu.bu.labs.lab1.State.StateView;
import edu.bu.labs.lab1.agents.Agent;
import edu.bu.labs.lab1.Coordinate;
import edu.bu.labs.lab1.Tile;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import edu.bu.labs.lab1.Unit.UnitView;

// JAVA PROJECT IMPORTS

public class ClosestUnitAgent
        extends Agent {

    // put your fields here! You will probably want to remember the following
    // information:
    // - all friendly unit ids (there may be more than one!)
    // - the location(s) of COIN(s) on the map

    private Set<Integer> myUnitIds;
    private Coordinate exitLocation;
    private Integer mainUnitId;
    private Coordinate mainUnitPosition;

    /**
     * The constructor for this type. Each agent has a unique ID that you will need
     * to use to request info from the
     * state about units it controls, etc.
     */
    public ClosestUnitAgent(final int agentId) {
        super(agentId); // make sure to call parent type (Agent)'s constructor!

        // initialize your fields here!
        this.myUnitIds = new HashSet<>();
        this.exitLocation = null;
        this.mainUnitId = null;
        this.mainUnitPosition = null;
        // helpful printout just to help debug
        System.out.println("Constructed ClosestUnitAgent");
    }

    /////////////////////////////// GETTERS AND SETTERS (this is Java after all)
    /////////////////////////////// ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public final Set<Integer> getMyUnitIds() {
        return this.myUnitIds;
    }

    private void setExitLocation(Coordinate c) {
        this.exitLocation = c;
    }

    public final Coordinate getExitLocation() {
        return this.exitLocation;
    }

    public final Integer getMainUnitId() {
        return this.mainUnitId;
    }

    public final Coordinate getMainUnitPosition() {
        return this.mainUnitPosition;
    }

    private void setMainUnitId(Integer i) {
        this.mainUnitId = i;
    }

    private void setMainUnitPosition(Coordinate c) {
        this.mainUnitPosition = c;
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
        // Collect all unit IDs
        for (Integer unitID : stateView.getUnitIds(this.getAgentId())) {
            this.myUnitIds.add(unitID);
        }

        if (this.myUnitIds.isEmpty()) {
            System.err.println("[ERROR] ClosestUnitAgent: No units found!");
            System.exit(-1);
        }

        // This will Find the exit location
        Coordinate exitLocation = null;
        for (int row = 0; row < stateView.getNumRows(); row++) {
            for (int col = 0; col < stateView.getNumCols(); col++) {
                Coordinate coordinate = new Coordinate(row, col);
                if (stateView.getTileState(coordinate) == Tile.State.FINISH) {
                    exitLocation = coordinate;
                    break;
                }
            }
            if (exitLocation != null)
                break;
        }

        if (exitLocation == null) {
            System.err.println("Failed to find the exit coordinate");
            System.exit(-1);
        }
        this.setExitLocation(exitLocation);

        Integer closestUnitId = null;
        Coordinate closestUnitPosition = null;
        double minDistance = Double.MAX_VALUE;

        for (UnitView unitView : stateView.getUnitViews(this.getAgentId())) {
            Coordinate unitPosition = unitView.currentPosition();
            int dx = unitPosition.col() - exitLocation.col();
            int dy = unitPosition.row() - exitLocation.row();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < minDistance) {
                minDistance = distance;
                closestUnitId = unitView.unitId();
                closestUnitPosition = unitPosition;
            }
        }

        this.setMainUnitId(closestUnitId);
        this.setMainUnitPosition(closestUnitPosition);

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

        if (this.getMainUnitId() == null || this.getExitLocation() == null) {
            return actions;
        }

        UnitView mainUnitView = state.getUnitView(this.getAgentId(), this.getMainUnitId());
        Coordinate currentPosition = mainUnitView.currentPosition();

        this.setMainUnitPosition(currentPosition);

        if (currentPosition.col() < this.getExitLocation().col()) {
            actions.put(mainUnitId, Direction.RIGHT);
        } else if (currentPosition.col() > this.getExitLocation().col()) {
            actions.put(mainUnitId, Direction.LEFT);
        } else if (currentPosition.row() < this.getExitLocation().row()) {
            actions.put(mainUnitId, Direction.DOWN);
        } else if (currentPosition.row() > this.getExitLocation().row()) {
            actions.put(mainUnitId, Direction.UP);
        }

        return actions;
    }

}
