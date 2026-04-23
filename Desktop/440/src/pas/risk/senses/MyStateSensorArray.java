package pas.risk.senses;

// SYSTEM IMPORTS
import edu.bu.jmat.Matrix;

import edu.bu.pas.risk.GameView;
import edu.bu.pas.risk.TerritoryOwnerView;
import edu.bu.pas.risk.agent.senses.StateSensorArray;
import edu.bu.pas.risk.territory.Board;
import edu.bu.pas.risk.territory.Continent;
import edu.bu.pas.risk.territory.Territory;

import java.util.List;

// JAVA PROJECT IMPORTS

/**
 * A suite of sensors to convert a {@link GameView} into a feature vector (must
 * be a row-vector)
 */
public class MyStateSensorArray
        extends StateSensorArray {
    public static final int NUM_FEATURES = 8;
    private static final double TOTAL_TERRITORIES_NORM = 42.0;
    private static final double TOTAL_CONTINENTS_NORM = 6.0;
    private static final double MAX_SINGLE_CONTINENT_BONUS_NORM = 7.0;

    public MyStateSensorArray(final int agentId) {
        super(agentId);
    }

    public Matrix getSensorValues(final GameView state) {
        final int myId = this.getAgentId();
        final Board board = state.getBoard();

        final List<Territory> myTerritories = state.getTerritoriesOwnedBy(myId);
        final List<Continent> myContinents = state.getContinentsOwnedBy(myId);

        int myArmies = 0;
        int totalArmies = 0;
        int maxOpponentTerritories = 0;
        int minLivingOpponentTerritories = Integer.MAX_VALUE;

        // tHIS is to calcutlate the total armies on the board as well as my army
        for (Territory territory : board.territories()) {
            final TerritoryOwnerView ownerView = state.getTerritoryOwners().get(territory);
            final int ownerId = ownerView.getOwner();
            final int armies = ownerView.getArmies();
            totalArmies += armies;

            if (ownerId == myId) {
                myArmies += armies;
            }
        }

        final int numAgents = state.getNumAgents();
        for (int agentId = 0; agentId < numAgents; ++agentId) {
            if (agentId == myId) {
                continue;
            }

            final int opponentTerritoryCount = state.getTerritoriesOwnedBy(agentId).size();
            if (opponentTerritoryCount > maxOpponentTerritories) {
                maxOpponentTerritories = opponentTerritoryCount;
            }
            if (opponentTerritoryCount > 0 && opponentTerritoryCount < minLivingOpponentTerritories) {
                minLivingOpponentTerritories = opponentTerritoryCount;
            }
        }

        int borderTerritoryCount = 0;
        for (Territory myTerritory : myTerritories) {
            boolean hasEnemyNeighbor = false;
            for (Territory neighbor : myTerritory.adjacentTerritories()) {
                final int neighborOwnerId = state.getTerritoryOwners().get(neighbor).getOwner();
                if (neighborOwnerId != myId) {
                    hasEnemyNeighbor = true;
                    break;
                }
            }
            if (hasEnemyNeighbor) {
                borderTerritoryCount += 1;
            }
        }

        double nearestContinentProgress = 0.0;
        for (Continent continent : board.continents()) {
            final int continentSize = continent.territories().size();
            if (continentSize <= 0) {
                continue;
            }

            int ownedInContinent = 0;
            for (Territory territory : continent.territories()) {
                if (state.getTerritoryOwners().get(territory).getOwner() == myId) {
                    ownedInContinent += 1;
                }
            }

            final double progress = Divider(ownedInContinent, continentSize);
            if (progress > nearestContinentProgress) {
                nearestContinentProgress = progress;
            }
        }

        int continentBonusArmies = 0;
        for (Continent continent : myContinents) {
            continentBonusArmies += continent.armiesPerTurn();
        }

        final double eliminationPressure = (minLivingOpponentTerritories == Integer.MAX_VALUE)
                ? 0.0
                : Divider(minLivingOpponentTerritories, TOTAL_TERRITORIES_NORM);

        final Matrix features = Matrix.zeros(1, NUM_FEATURES);
        features.set(0, 0, Divider(myTerritories.size(), TOTAL_TERRITORIES_NORM));
        features.set(0, 1, Divider(myArmies, totalArmies));
        features.set(0, 2, Divider(myContinents.size(), TOTAL_CONTINENTS_NORM));
        features.set(0, 3, Divider(maxOpponentTerritories, TOTAL_TERRITORIES_NORM));
        features.set(0, 4, Divider(borderTerritoryCount, myTerritories.size()));
        features.set(0, 5, nearestContinentProgress);
        features.set(0, 6, Divider(continentBonusArmies, MAX_SINGLE_CONTINENT_BONUS_NORM));
        features.set(0, 7, eliminationPressure);
        return features;
    }

    private static double Divider(final double num,
            final double den) {
        if (den <= 0.0) {
            return 0.0;
        }
        final double value = num / den;
        if (value < 0.0) {
            return 0.0;
        }
        return Math.min(1.0, value);
    }

}
