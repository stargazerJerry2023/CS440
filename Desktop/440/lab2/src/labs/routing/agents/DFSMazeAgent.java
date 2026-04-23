package src.labs.routing.agents;

// SYSTEM IMPORTS
import edu.bu.labs.routing.Coordinate;
import edu.bu.labs.routing.Direction;
import edu.bu.labs.routing.Path;
import edu.bu.labs.routing.State.StateView;
import edu.bu.labs.routing.Tile;
import edu.bu.labs.routing.agents.MazeAgent;

import java.util.Collection;
import java.util.HashSet; // will need for dfs
import java.util.Stack; // will need for dfs
import java.util.Set; // will need for dfs

// JAVA PROJECT IMPORTS

public class DFSMazeAgent
        extends MazeAgent {

    public DFSMazeAgent(final int agentId) {
        super(agentId);
    }

    @Override
    public void initializeFromState(final StateView stateView) {
        // find the FINISH tile
        Coordinate finishCoord = null;
        for (int rowIdx = 0; rowIdx < stateView.getNumRows(); ++rowIdx) {
            for (int colIdx = 0; colIdx < stateView.getNumCols(); ++colIdx) {
                if (stateView.getTileState(new Coordinate(rowIdx, colIdx)) == Tile.State.FINISH) {
                    finishCoord = new Coordinate(rowIdx, colIdx);
                }
            }
        }
        this.setFinishCoordinate(finishCoord);

        // make sure to call the super-class' version!
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
        // DFS implementation using Stack
        Stack<Path<Coordinate>> stack = new Stack<>();
        Set<Coordinate> visited = new HashSet<>();
        
        // Start with the source coordinate
        Path<Coordinate> startPath = new Path<>(src);
        stack.push(startPath);
        visited.add(src);
        
        // Directions to explore (4 cardinal directions)
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        
        while (!stack.isEmpty()) {
            Path<Coordinate> currentPath = stack.pop();
            Coordinate currentCoord = currentPath.current();
            
            // Check if we've reached a coordinate adjacent to the goal
            // (The goal itself is occupied, so we need to stop at an adjacent coordinate)
            if (currentCoord.isAdjacentTo(goal)) {
                return currentPath;
            }
            
            // Explore neighbors
            for (Direction dir : directions) {
                Coordinate neighbor = currentCoord.getNeighbor(dir);
                
                // Check if neighbor is within bounds
                if (neighbor.row() < 0 || neighbor.row() >= stateView.getNumRows() ||
                    neighbor.col() < 0 || neighbor.col() >= stateView.getNumCols()) {
                    continue;
                }
                
                // Check if already visited
                if (visited.contains(neighbor)) {
                    continue;
                }
                
                // Check if the tile is valid (not a wall)
                Tile.State tileState = stateView.getTileState(neighbor);
                if (tileState == Tile.State.WALL) {
                    continue;
                }
                
                // Don't visit the goal itself (it's occupied)
                if (neighbor.equals(goal)) {
                    continue;
                }
                
                // Mark as visited and add to stack
                visited.add(neighbor);
                Path<Coordinate> newPath = new Path<>(currentPath, neighbor, 1.0);
                stack.push(newPath);
            }
        }
        
        // No path found
        return null;
    }

}
