package src.pas.pacman.routing;

// SYSTEM IMPORTS
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

// JAVA PROJECT IMPORTS
import edu.bu.pas.pacman.agents.Agent;
import edu.bu.pas.pacman.game.Action;
import edu.bu.pas.pacman.game.Game.GameView;
import edu.bu.pas.pacman.game.Tile;
import edu.bu.pas.pacman.graph.Path;
import edu.bu.pas.pacman.routing.BoardRouter;
import edu.bu.pas.pacman.routing.BoardRouter.ExtraParams;
import edu.bu.pas.pacman.utils.Coordinate;
import edu.bu.pas.pacman.utils.Pair;

// This class is responsible for calculating routes between two Coordinates on the Map.
// Use this in your PacmanAgent to calculate routes that (if followed) will lead
// Pacman from some Coordinate to some other Coordinate on the map.
public class ThriftyBoardRouter
        extends BoardRouter {

    // If you want to encode other information you think is useful for Coordinate
    // routing
    // besides Coordinates and data available in GameView you can do so here.
    public static class BoardExtraParams
            extends ExtraParams {

    }

    // feel free to add other fields here!

    public ThriftyBoardRouter(int myUnitId,
            int pacmanId,
            int ghostChaseRadius) {
        super(myUnitId, pacmanId, ghostChaseRadius);

        // if you add fields don't forget to initialize them here!
    }

    @Override
    public Collection<Coordinate> getOutgoingNeighbors(final Coordinate src,
            final GameView game,
            final ExtraParams params) {

        Collection<Coordinate> neighbors = new ArrayList<>();

        for (Action action : Action.values()) {
            Coordinate next = src.getNeighbor(action);
            if (next.equals(src)) {
                continue;
            }
            if (game.isLegalPacmanMove(src, action)) {
                neighbors.add(next);
            }
        }

        return neighbors;
    }

    @Override
    public Path<Coordinate> graphSearch(final Coordinate src,
            final Coordinate tgt,
            final GameView game) {

        if (src.equals(tgt)) {
            return new Path<>(src);
        }

        ExtraParams params = new BoardExtraParams();
        Queue<Coordinate> queue = new ArrayDeque<>();
        Set<Coordinate> visited = new HashSet<>();
        Map<Coordinate, Coordinate> parent = new HashMap<>();

        queue.add(src);
        visited.add(src);

        while (!queue.isEmpty()) {
            Coordinate u = queue.poll();
            if (u.equals(tgt)) {
                List<Coordinate> TgtToSrc = new ArrayList<>();
                for (Coordinate c = tgt; c != null; c = parent.get(c)) {
                    TgtToSrc.add(c);
                }
                Path<Coordinate> path = new Path<>(TgtToSrc.get(TgtToSrc.size() - 1));
                for (int i = TgtToSrc.size() - 2; i >= 0; i--) {
                    path = new Path<>(TgtToSrc.get(i), 1.0f, path);
                }
                return path;

            }
            for (Coordinate v : getOutgoingNeighbors(u, game, params)) {
                if (!visited.contains(v)) {
                    visited.add(v);
                    parent.put(v, u);
                    queue.add(v);
                }
            }

        }

        return null;
    }

}
