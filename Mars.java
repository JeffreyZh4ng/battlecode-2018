import bc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Mars {

    public static MapLocation marsAttackTarget = null;

    public static HashMap<Integer, Rocket> marsRocketMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> marsWorkerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> marsAttackerMap = new HashMap<>();

    public static HashMap<Integer, UnitInstance> marsStagingWorkerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> marsStagingAttackerMap = new HashMap<>();

    public static void execute() {
        updateDeadUnits();
        lookForLandedRockets();

        runRocketMap();
        runUnitMap(marsWorkerMap);
        runUnitMap(marsAttackerMap);

        addStagingUnitsToMap();
    }

    /**
     * Method that will look for friendly rockets that have landed on Mars and will add them to the map
     */
    private static void lookForLandedRockets() {
        RocketLandingInfo landingInfo = Player.gc.rocketLandings();
        VecRocketLanding vecRocketLanding = landingInfo.landingsOn(Player.gc.round());
        for (int i = 0; i < vecRocketLanding.size(); i++) {
            int rocketId = vecRocketLanding.get(i).getRocket_id();
            Rocket landedRocket = new Rocket(rocketId, true);

            System.out.println("Added rocket " + rocketId + " To the rocket map!");
            marsRocketMap.put(rocketId, landedRocket);
        }
    }

    /**
     * Update and remove launched rocket. Needs to be specific to for rockets because of their unique functionality
     */
    private static void runRocketMap() {
        for (int rocketId: marsRocketMap.keySet()) {
            Rocket rocket = marsRocketMap.get(rocketId);
            rocket.run();
        }
    }

    /**
     * That that will run the execute() command for all the units in the given HashMap
     * @param searchMap The HashMap of units
     */
    private static void runUnitMap(HashMap<Integer, UnitInstance> searchMap) {
        for (int unitId: searchMap.keySet()) {
            searchMap.get(unitId).run();
        }
    }

    /**
     * Since the method has not yet been implemented in the API, we must manually check if any unit died last round
     */
    private static void updateDeadUnits() {
        HashSet<Integer> unitSet = new HashSet<>();
        VecUnit units = Player.gc.myUnits();
        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).location().isOnPlanet(Planet.Mars)) {
                unitSet.add(units.get(i).id());
            }
        }

        marsWorkerMap = findDeadUnits(unitSet, marsWorkerMap);
        marsAttackerMap = findDeadUnits(unitSet, marsAttackerMap);
    }

    /**
     * Helper method for the updateDeadUnits method. This method will compile an array all units in the specified
     * HashMap but not in the units list returned by Player.gameController.myUnits(). Will then remove all the
     * units specified by the array and remove them from the map
     * @param unitSet The set of units returned by the Game Controller
     * @param searchMap The current map you are purging
     * @return A new map without the dead units
     */
    private static HashMap<Integer, UnitInstance> findDeadUnits(HashSet<Integer> unitSet, HashMap<Integer, UnitInstance> searchMap) {
        ArrayList<Integer> deadUnits = new ArrayList<>();
        for (int unitId: searchMap.keySet()) {
            if (!unitSet.contains(unitId)) {
                deadUnits.add(unitId);

                // If the unit is dead, we must update the HashSets of the tasks it was part of.
                UnitInstance unit = searchMap.get(unitId);

            }
        }

        for (int unitId: deadUnits) {
            System.out.println("Removing unit: " + unitId);
            searchMap.remove(unitId);
        }

        return searchMap;
    }

    /**
     * Method that will add all the robots created this round to their indicated unit map
     */
    private static void addStagingUnitsToMap() {
        for (int unitId : marsStagingWorkerMap.keySet()) {
            marsWorkerMap.put(unitId, marsStagingWorkerMap.get(unitId));
            System.out.println("Added unit: " + unitId + " To the worker list");
        }
        marsStagingWorkerMap.clear();

        for (int unitId : marsStagingAttackerMap.keySet()) {
            marsAttackerMap.put(unitId, marsStagingAttackerMap.get(unitId));
            System.out.println("Added unit: " + unitId + " To the attacker list");
        }
        marsStagingAttackerMap.clear();
    }
}
