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
import java.util.Stack;
import java.util.Set;

public class DFSMazeAgent
        extends MazeAgent {

    public DFSMazeAgent(final int agentId) {
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
        Stack<Path<Coordinate>> stack = new Stack<>();
        Set<Coordinate> visited = new HashSet<>();

        Path<Coordinate> startPath = new Path<>(src);
        stack.push(startPath);
        visited.add(src);

        Direction[] directions = { Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT };

        while (!stack.isEmpty()) {
            Path<Coordinate> currentPath = stack.pop();
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
                if (visited.contains(neighbor)) {
                    continue;
                }
                Tile.State tileState = stateView.getTileState(neighbor);
                if (tileState == Tile.State.WALL) {
                    continue;
                }

                visited.add(neighbor);
                Path<Coordinate> newPath = new Path<>(currentPath, neighbor, 1d);
                stack.push(newPath);
            }
        }

        // No path found
        return null;
    }

}
