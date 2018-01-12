package planets;

import bc.VecUnit;
import commandsAndRequests.Globals;
import robots.Robot;
import commandsAndRequests.Task;
import structures.Blueprint;
import structures.Factory;
import structures.Rocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Earth {
    public static HashMap<Integer, Task> earthTaskMap = new HashMap<>();
    public static HashMap<Integer, Task> earthAttackTargetsMap = new HashMap<>();
    public static HashMap<Integer, Task> earthProduceRobotMap = new HashMap<>();

    public static HashMap<Integer, Blueprint> earthBlueprintMap = new HashMap<>();
    public static HashMap<Integer, Rocket> earthRocketMap = new HashMap<>();
    public static HashMap<Integer, Robot> earthWorkerMap = new HashMap<>();
    public static HashMap<Integer, Factory> earthFactoryMap = new HashMap<>();
    public static HashMap<Integer, Robot> earthAttackerMap = new HashMap<>();

    public void execute() {
        updateDeadUnits();

        // Execute blueprints, Rockets, Workers, Factories, Attackers
        for (int blueprintId: earthBlueprintMap.keySet()) {
            earthBlueprintMap.get(blueprintId).execute();
        }
        for (int rocketId: earthRocketMap.keySet()) {
            earthRocketMap.get(rocketId).execute();
        }
        for (int workerId: earthWorkerMap.keySet())
    }

    public boolean sendRequestsToWorkers() {

    }

    private <T> void runUnitMap(HashMap<Integer, T> searchMap) {
        for (int unitId: searchMap.keySet()) {
            searchMap.get(unitId);
        }
    }

    /**
     * Since the method has not yet been implemented in the API, we must manually check if any unit died
     * last round
     */
    private void updateDeadUnits() {
        HashSet<Integer> unitSet = new HashSet<>();
        VecUnit units = Globals.gameController.myUnits();
        for (int i = 0; i < units.size(); i++) {
            unitSet.add(units.get(i).id());
        }

        earthBlueprintMap = findDeadUnits(unitSet, earthBlueprintMap);
        earthRocketMap = findDeadUnits(unitSet, earthRocketMap);
        earthWorkerMap = findDeadUnits(unitSet, earthWorkerMap);
        earthFactoryMap = findDeadUnits(unitSet, earthFactoryMap);
        earthAttackerMap = findDeadUnits(unitSet, earthAttackerMap);
    }

    /**
     * Helper method for the updateDeadUnits method. This method will compile an array all units in the specified
     * HashMap but not in the units list returned by Globals.gameController.myUnits(). Will then remove all the
     * units specified by the array and remove them from the map
     * @param unitSet The set of units returned by the Game Controller
     * @param searchMap The current map you are purging
     * @param <T> The value of the map
     * @return A new map without the dead units
     */
    private <T> HashMap<Integer, T> findDeadUnits(HashSet<Integer> unitSet, HashMap<Integer, T> searchMap) {
        ArrayList<Integer> deadUnits = new ArrayList<>();
        for (int unitId: searchMap.keySet()) {
            if (!unitSet.contains(unitId)) {
                deadUnits.add(unitId);
            }
        }

        for (int unitId: deadUnits) {
            searchMap.remove(unitId);
        }

        return searchMap;
    }
}
