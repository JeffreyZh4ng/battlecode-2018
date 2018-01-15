package fufilGardenerRequest;

import bc.MapLocation;
import bc.UnitType;
import bc.VecUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Earth {
    public static HashMap<Integer, GlobalTask> earthTaskMap = new HashMap<>();

    // TODO: Possibly change these two two to queues?
    public static HashMap<Integer, GlobalTask> earthAttackTargetsMap = new HashMap<>();
    public static HashMap<Integer, GlobalTask> earthProduceRobotMap = new HashMap<>();

    public static HashMap<Integer, Unit> earthBlueprintMap = new HashMap<>();
    public static HashMap<Integer, Unit> earthRocketMap = new HashMap<>();
    public static HashMap<Integer, Unit> earthWorkerMap = new HashMap<>();
    public static HashMap<Integer, Unit> earthFactoryMap = new HashMap<>();
    public static HashMap<Integer, Unit> earthAttackerMap = new HashMap<>();

    public void execute() {



//        updateDeadUnits();
//
//        runUnitMap(earthBlueprintMap);
//        runUnitMap(earthRocketMap);
//        runUnitMap(earthWorkerMap);
//        runUnitMap(earthFactoryMap);
//        runUnitMap(earthAttackerMap);
    }

//    /**
//     * This method will be called when a factory or blueprint want to be constructed. This method will help
//     * choose the location of the structure and add it to the global task list
//     * @param command The command of the task that you want to be added to the global list
//     */
//    public void createGlobalTask(Command command) {
//        MapLocation globalTaskLocation;
//
//        switch (command) {
//            case CONSTRUCT_FACTORY:
//                globalTaskLocation = pickFactoryLocation();
//                break;
//            case CONSTRUCT_ROCKET:
//                globalTaskLocation = pickRocketLocation();
//                break;
//            default:
//                globalTaskLocation = bestRobotSpawnLocation();
//                break;
//        }
//
//        GlobalTask newGlobalTask = new GlobalTask(command, globalTaskLocation);
//        int globalTaskId = newGlobalTask.getTaskId();
//
//        earthTaskMap.put(globalTaskId, newGlobalTask);
//    }

//    private void sendTasksToRobot(GlobalTask globalTask, ArrayList<Integer> robotList) {
//        Command taskCommand = globalTask.getCommand();
//        int commandId = globalTask.getTaskId();
//        MapLocation taskLocation = globalTask.getTaskLocation();
//
//        switch (taskCommand) {
//            case CONSTRUCT_FACTORY:
//                for (int workerId : robotList) {
//                    RobotTask moveTask = new RobotTask(commandId, Command.MOVE, taskLocation);
//                    earthBlueprintMap.get(workerId).robotTaskQueue.add(moveTask);
//                    RobotTask blueprintTask = new RobotTask(commandId, Command.BLUEPRINT_ROCKET, taskLocation);
//                    earthBlueprintMap.get(workerId).robotTaskQueue.add(blueprintTask);
//                    RobotTask buildTask = new RobotTask(commandId, Command.BUILD, taskLocation);
//                    earthBlueprintMap.get(workerId).robotTaskQueue.add(buildTask);
//                }
//
//            case CONSTRUCT_ROCKET:
//                for (int workerId : robotList) {
//                    RobotTask moveTask = new RobotTask(commandId, Command.MOVE, taskLocation);
//                    earthBlueprintMap.get(workerId).robotTaskQueue.add(moveTask);
//                    RobotTask blueprintTask = new RobotTask(commandId, Command.BLUEPRINT_FACTORY, taskLocation);
//                    earthBlueprintMap.get(workerId).robotTaskQueue.add(blueprintTask);
//                    RobotTask buildTask = new RobotTask(commandId, Command.BUILD, taskLocation);
//                    earthBlueprintMap.get(workerId).robotTaskQueue.add(buildTask);
//                }
//        }
//    }

    public ArrayList<Integer> getNearestRobots(MapLocation mapLocation) {
        ArrayList<Integer> nearestRobots = new ArrayList<>();

        VecUnit vecUnit = Player.gc.senseNearbyUnitsByType(mapLocation, 50, UnitType.Worker);
        for (int i = 0; i < vecUnit.size(); i++) {
            bc.Unit unit = vecUnit.get(i);
            if (unit.team() == Player.gc.team()) {
                nearestRobots.add(unit.id());
            }
        }

        return nearestRobots;
    }

    // Possibly only need one of these parameters
    public void loadRocketRequest(MapLocation rocketLocation, int rocketId) {

    }

    /**
     * Runs through the earthTaskMap and will update progress on each task
     */
    private void updateTaskMap() {
        for (int globalTaskId: earthTaskMap.keySet()) {

            Command taskCommand = earthTaskMap.get(globalTaskId).getCommand();
            switch (taskCommand) {
                case CONSTRUCT_FACTORY:
                    manageConstruction(globalTaskId);
                    break;
                case CONSTRUCT_ROCKET:
                    break;
                case LOAD_ROCKET:
                    break;
                case BUILD:
            }
        }
    }

    private void manageConstruction(int globalTaskId) {
        GlobalTask globalTask = earthTaskMap.get(globalTaskId);
        Command taskCommand = globalTask.getCommand();

        // Stage 1 means its still trying to find workers to fulfil the task. Stage 2 is building the blueprint
        // Stage 3 is building the structure.
        switch (globalTask.getCompletionStage()) {
            case 1:
                // Make sure there is atleast one
                break;
            case 2:
                break;
            case 3:
                break;
        }
    }

    /**
     * That that will run the execute() command for all the units in the given HashMap
     * @param searchMap The HashMap of units
     */
    private void runUnitMap(HashMap<Integer, Unit> searchMap) {
        for (int unitId: searchMap.keySet()) {
            System.out.println("Earth running run(): " + unitId);
            searchMap.get(unitId).run();
        }
    }

    /**
     * Since the method has not yet been implemented in the API, we must manually check if any unit died
     * last round
     */
    private void updateDeadUnits() {
        HashSet<Integer> unitSet = new HashSet<>();
        VecUnit units = Player.gc.myUnits();
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
     * HashMap but not in the units list returned by Player.gameController.myUnits(). Will then remove all the
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
            System.out.println("Removing unit: " + unitId);
            searchMap.remove(unitId);
        }

        return searchMap;
    }


}
