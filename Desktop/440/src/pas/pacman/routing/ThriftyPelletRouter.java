package src.pas.pacman.routing;

// SYSTEM IMPORTS
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

// JAVA PROJECT IMPORTS
import edu.bu.pas.pacman.game.Game.GameView;
import edu.bu.pas.pacman.graph.Path;
import edu.bu.pas.pacman.graph.PelletGraph.PelletVertex;
import edu.bu.pas.pacman.routing.BoardRouter;
import edu.bu.pas.pacman.routing.PelletRouter;
import edu.bu.pas.pacman.utils.Coordinate;
import java.util.ArrayList;

public class ThriftyPelletRouter

        extends PelletRouter {

    // If you want to encode other information you think is useful for planning the
    // order
    // of pellets ot eat besides Coordinates and data available in GameView
    // you can do so here.
    public static class PelletExtraParams
            extends ExtraParams {

        private GameView game;

        public GameView getGame() {
            return game;
        }

        public void setGame(GameView game) {
            this.game = game;
        }
    }

    private final BoardRouter boardRouter;

    public ThriftyPelletRouter(int myUnitId,
            int pacmanId,
            int ghostChaseRadius) {
        super(myUnitId, pacmanId, ghostChaseRadius);
        this.boardRouter = new ThriftyBoardRouter(myUnitId, pacmanId, ghostChaseRadius);
    }

    @Override
    public Collection<PelletVertex> getOutgoingNeighbors(final PelletVertex src,
            final GameView game,
            final ExtraParams params) {
        // TODO: implement me!

        Collection<PelletVertex> neighbors = new ArrayList<>();
        Set<Coordinate> remainingPellets = src.getRemainingPelletCoordinates();

        if (remainingPellets.isEmpty()) {
            return neighbors;
        }

        for (Coordinate pellet : remainingPellets) {
            neighbors.add(src.removePellet(pellet));
        }

        return neighbors;
    }

    @Override
    public float getEdgeWeight(final PelletVertex src,
            final PelletVertex dst,
            final ExtraParams params) {

        Coordinate from = src.getPacmanCoordinate();
        Coordinate to = dst.getPacmanCoordinate();

        if (params instanceof PelletExtraParams) {
            GameView game = ((PelletExtraParams) params).getGame();
            if (game != null && boardRouter != null) {
                Path<Coordinate> path = boardRouter.graphSearch(from, to, game);
                if (path != null) {
                    float cost = path.getTrueCost();
                    return cost >= 0f ? cost : (float) getDistance(from, to);
                }
            }
        }

        return (float) getDistance(from, to);
    }

    @Override
    public float getHeuristic(final PelletVertex src,
            final GameView game,
            final ExtraParams params) {
        // TODO: implement me!

        Set<Coordinate> remainingPellets = src.getRemainingPelletCoordinates();
        Coordinate CurrentLocation = src.getPacmanCoordinate();
        int minDistance = Integer.MAX_VALUE;
        if (remainingPellets.isEmpty()) {
            return 0f;
        }

        for (Coordinate pellet : remainingPellets) {
            int distance = getDistance(CurrentLocation, pellet);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }

        List<Coordinate> remainingPelletsList = new ArrayList<>(remainingPellets);
        Set<Integer> distances = new HashSet<>();
        distances.add(0);
        int distanceCost = 0;

        while (distances.size() < remainingPelletsList.size()) {
            int minEdge = Integer.MAX_VALUE;
            int j = -1;
            for (int i : distances) {
                for (int k = 0; k < remainingPelletsList.size(); k++) {
                    if (distances.contains(k))
                        continue;
                    int w = getDistance(remainingPelletsList.get(i), remainingPelletsList.get(k));
                    if (w < minEdge) {
                        minEdge = w;
                        j = k;
                    }
                }

            }
            if (j == -1)
                break;
            distances.add(j);
            distanceCost += minEdge;
        }
        return (float) (minDistance + distanceCost);

    }

    @Override
    public Path<PelletVertex> graphSearch(final GameView game) {
        PelletVertex start = new PelletVertex(game);

        if (start.getRemainingPelletCoordinates().isEmpty()) {
            return new Path<>(start);
        }

        PelletExtraParams params = new PelletExtraParams();
        params.setGame(game);

        Comparator<Path<PelletVertex>> byF = Comparator
                .comparingDouble(p -> p.getTrueCost() + p.getEstimatedPathCostToGoal());
        PriorityQueue<Path<PelletVertex>> open = new PriorityQueue<>(byF);
        Set<PelletVertex> closed = new HashSet<>();

        Path<PelletVertex> startPath = new Path<>(start);
        startPath.setEstimatedPathCostToGoal(getHeuristic(start, game, params));
        open.add(startPath);

        while (!open.isEmpty()) {
            Path<PelletVertex> current = open.poll();
            PelletVertex dest = current.getDestination();

            if (closed.contains(dest)) {
                continue;
            }
            if (dest.getRemainingPelletCoordinates().isEmpty()) {
                return current;
            }
            closed.add(dest);

            for (PelletVertex neighbor : getOutgoingNeighbors(dest, game, params)) {
                if (closed.contains(neighbor)) {
                    continue;
                }
                float edgeCost = getEdgeWeight(dest, neighbor, params);
                float h = getHeuristic(neighbor, game, params);
                Path<PelletVertex> nextPath = new Path<>(neighbor, edgeCost, h, current);
                open.add(nextPath);
            }
        }

        return null;
    }

    public int getDistance(final Coordinate src,
            final Coordinate tgt) {
        return Math.abs(src.x() - tgt.x()) + Math.abs(src.y() - tgt.y());

    }

}
