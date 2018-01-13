package planets;

import bc.VecUnit;
import commandsAndRequests.Globals;
import units.Robot;
import commandsAndRequests.GlobalTask;
import units.Unit;
import units.structures.Blueprint;
import units.structures.Factory;
import units.structures.Rocket;
import units.Structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Earth {
    public static HashMap<Integer, GlobalTask> earthTaskMap = new HashMap<>();
    public static HashMap<Integer, GlobalTask> earthAttackTargetsMap = new HashMap<>();
    public static HashMap<Integer, GlobalTask> earthProduceRobotMap = new HashMap<>();

    public static HashMap<Integer, Unit> earthBlueprintMap = new HashMap<>();
    public static HashMap<Integer, Unit> earthRocketMap = new HashMap<>();
    public static HashMap<Integer, Unit> earthWorkerMap = new HashMap<>();
    public static HashMap<Integer, Unit> earthFactoryMap = new HashMap<>();
    public static HashMap<Integer, Unit> earthAttackerMap = new HashMap<>();

    public void execute() {
        updateDeadUnits();
        Robot.moveWorkers();

        runUnitMap(earthBlueprintMap);
    }

    public boolean sendRequestsToWorkers() {
        return true;
    }

    private void runUnitMap(HashMap<Integer, Unit> searchMap) {
        for (int unitId: searchMap.keySet()) {
            searchMap.get(unitId).execute();
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
     * @return A new map without the dead units
     */
    private HashMap<Integer, Unit> findDeadUnits(HashSet<Integer> unitSet, HashMap<Integer, Unit> searchMap) {
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
