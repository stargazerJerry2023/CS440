package src.labs.routing.agents;

// SYSTEM IMPORTS
import edu.bu.labs.routing.Coordinate;
import edu.bu.labs.routing.Direction;
import edu.bu.labs.routing.Path;
import edu.bu.labs.routing.State.StateView;
import edu.bu.labs.routing.Tile;
import edu.bu.labs.routing.agents.MazeAgent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;

// JAVA PROJECT IMPORTS

public class BFSMazeAgent
        extends MazeAgent {

    public BFSMazeAgent(final int agentId) {
        super(agentId);
    }

    @Override
    public void initializeFromState(final StateView stateView) {
        Coordinate finishCoord = null;
        for (int rowIdx = 0; rowIdx < stateView.getNumRows(); ++rowIdx) {
            for (int colIdx = 0; colIdx < stateView.getNumCols(); ++colIdx) {
                if (stateView.getTileState(new Coordinate(rowIdx, colIdx)) == Tile.State.FINISH) {
                    finishCoord = new Coordinate(rowIdx, colIdx);
                }
            }
        }
        this.setFinishCoordinate(finishCoord);

        super.initializeFromState(stateView);
    }

    @Override
    public boolean shouldReplacePlan(final StateView stateView) {
        return false;
    }

    @Override
    public Path<Coordinate> search(final Coordinate src,
            final Coordinate goal,
            final StateView stateView) {
        Queue<Path<Coordinate>> queue = new LinkedList<>();
        Set<Coordinate> visited = new HashSet<>();

        Path<Coordinate> startPath = new Path<>(src);
        queue.add(startPath);
        visited.add(src);

        Direction[] directions = Direction.getCardinalDirections();

        while (!queue.isEmpty()) {
            Path<Coordinate> currentPath = queue.poll();
            Coordinate currentCoord = currentPath.current();

            if (currentCoord.equals(goal)) {
                return currentPath;
            }

            for (Direction dir : directions) {
                Coordinate neighbor = currentCoord.getNeighbor(dir);

                if (neighbor.row() < 0 || neighbor.row() >= stateView.getNumRows() ||
                        neighbor.col() < 0 || neighbor.col() >= stateView.getNumCols()) {
                    continue;
                }
                Tile.State tileState = stateView.getTileState(neighbor);
                if (tileState == Tile.State.WALL) {
                    continue;
                }
                // Return immediately when we discover the goal (shortest path by BFS)
                if (neighbor.equals(goal)) {
                    return new Path<>(currentPath, neighbor, 1d);
                }
                if (visited.contains(neighbor)) {
                    continue;
                }
                visited.add(neighbor);
                queue.add(new Path<>(currentPath, neighbor, 1d));
            }
        }

        return null;
    }

}
