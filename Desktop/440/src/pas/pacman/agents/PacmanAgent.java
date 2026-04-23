package src.pas.pacman.agents;

// SYSTEM IMPORTS
import edu.bu.pas.pacman.agents.Agent;
import edu.bu.pas.pacman.agents.SearchAgent;
import edu.bu.pas.pacman.game.Action;
import edu.bu.pas.pacman.game.Game.GameView;
import edu.bu.pas.pacman.graph.Path;
import edu.bu.pas.pacman.graph.PelletGraph.PelletVertex;
import edu.bu.pas.pacman.routing.BoardRouter;
import edu.bu.pas.pacman.routing.PelletRouter;
import edu.bu.pas.pacman.utils.Coordinate;
import edu.bu.pas.pacman.utils.Pair;

import java.util.Random;
import java.util.Set;
import java.util.Collection;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
// JAVA PROJECT IMPORTS
import src.pas.pacman.routing.ThriftyBoardRouter; // responsible for how to get somewhere
import src.pas.pacman.routing.ThriftyPelletRouter; // responsible for pellet order

public class PacmanAgent
        extends SearchAgent {

    private final Random random;
    private BoardRouter boardRouter;
    private PelletRouter pelletRouter;
    private Coordinate currentLocation;

    public PacmanAgent(int myUnitId,
            int pacmanId,
            int ghostChaseRadius) {
        super(myUnitId, pacmanId, ghostChaseRadius);
        this.random = new Random();

        this.boardRouter = new ThriftyBoardRouter(myUnitId, pacmanId, ghostChaseRadius);
        this.pelletRouter = new ThriftyPelletRouter(myUnitId, pacmanId, ghostChaseRadius);
    }

    public final Random getRandom() {
        return this.random;
    }

    public final BoardRouter getBoardRouter() {
        return this.boardRouter;
    }

    public final PelletRouter getPelletRouter() {
        return this.pelletRouter;
    }

    @Override
    public void makePlan(final GameView game) {
        this.currentLocation = game.getEntity(game.getPacmanId()).getCurrentCoordinate();

        Coordinate targetCoordinate = this.getTargetCoordinate();

        if (targetCoordinate != null) {
            // Tests or callers set an explicit target; plan from current location to that coordinate.
            Path<Coordinate> boardPath = this.getBoardRouter().graphSearch(this.currentLocation, targetCoordinate, game);
            if (boardPath == null) {
                this.setPlanToGetToTarget(new Stack<>());
                return;
            }
            List<Coordinate> vertices = new ArrayList<>();
            Path<Coordinate> p = boardPath;
            while (p != null) {
                vertices.add(p.getDestination());
                p = p.getParentPath();
            }
            Collections.reverse(vertices);
            Stack<Coordinate> plan = new Stack<>();
            for (int i = vertices.size() - 1; i >= 1; i--) {
                plan.push(vertices.get(i));
            }
            this.setPlanToGetToTarget(plan);
            return;
        }

        // No explicit target: use pellet router to choose next pellet.
        Path<PelletVertex> pelletPath = this.getPelletRouter().graphSearch(game);

        if (pelletPath == null) {
            this.setPlanToGetToTarget(new Stack<>());
            return;
        }

        // Path is reversed: head = goal, tail = start. Find the first step from start
        // (path node whose parent is the tail), without relying on PelletVertex.equals().
        Path<PelletVertex> step = pelletPath;
        while (step.getParentPath() != null && step.getParentPath().getParentPath() != null) {
            step = step.getParentPath();
        }
        if (step.getParentPath() == null) {
            this.setPlanToGetToTarget(new Stack<>());
            return;
        }

        PelletVertex firstTargetState = step.getDestination();
        targetCoordinate = firstTargetState.getPacmanCoordinate();

        Path<Coordinate> boardPath = this.getBoardRouter().graphSearch(this.currentLocation, targetCoordinate, game);

        if (boardPath == null) {
            this.setPlanToGetToTarget(new Stack<>());
            return;
        }

        List<Coordinate> vertices = new ArrayList<>();
        Path<Coordinate> p = boardPath;
        while (p != null) {
            vertices.add(p.getDestination());
            p = p.getParentPath();
        }
        Collections.reverse(vertices);

        Stack<Coordinate> plan = new Stack<>();
        for (int i = vertices.size() - 1; i >= 1; i--) {
            plan.push(vertices.get(i));
        }
        this.setPlanToGetToTarget(plan);
    }

    @Override
    public Action makeMove(final GameView game) {
        this.currentLocation = game.getEntity(game.getPacmanId()).getCurrentCoordinate();

        Stack<Coordinate> plan = this.getPlanToGetToTarget();
        if (plan == null || plan.isEmpty()) {
            this.makePlan(game);
            plan = this.getPlanToGetToTarget();
        }

        if (plan == null || plan.isEmpty()) {
            for (Action a : Action.values()) {
                if (!this.currentLocation.getNeighbor(a).equals(this.currentLocation)
                        && game.isLegalPacmanMove(this.currentLocation, a)) {
                    return a;
                }
            }
        }

        Coordinate next = plan.pop();
        try {
            return Action.inferFromCoordinates(this.currentLocation, next);
        } catch (Exception e) {
            for (Action a : Action.values()) {
                if (!this.currentLocation.getNeighbor(a).equals(this.currentLocation)
                        && game.isLegalPacmanMove(this.currentLocation, a)) {
                    return a;
                }
            }
        }
        return Action.values()[0];
    }

    @Override
    public void afterGameEnds(final GameView game) {
        // if you want to log stuff after a game ends implement me!
        System.out.println("afterGameEnds");
    }
}
