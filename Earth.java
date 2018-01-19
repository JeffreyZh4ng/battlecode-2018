import bc.*;

import java.util.*;

public class Earth extends PlanetInstance {

    public static HashMap<Integer, GlobalTask> earthTaskMap = new HashMap<>();
    public static Queue<GlobalTask> earthTaskQueue = new LinkedList<>();

    public static HashMap<Integer, UnitInstance> earthRocketMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthWorkerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthAttackerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthFactoryMap = new HashMap<>();

    public static HashSet<Integer> earthFinishedTasks = new HashSet<>();

    public static HashMap<Integer, UnitInstance> earthStagingWorkerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthStagingAttackerMap = new HashMap<>();

    public static HashSet<String> planedStructureLocations = new HashSet<>();

    public static void execute() {

        updateDeadUnits();

        updateTaskQueue();

        runUnitMap(earthRocketMap);
        runUnitMap(earthWorkerMap);
        runUnitMap(earthAttackerMap);
        runUnitMap(earthFactoryMap);

        earthTaskMap = removeFinishedTasks(earthTaskMap, earthFinishedTasks);
        earthFinishedTasks = new HashSet<>();

        earthWorkerMap = addStagingUnitsToMap(earthWorkerMap, earthStagingWorkerMap);
        earthStagingWorkerMap = new HashMap<>();
        earthAttackerMap = addStagingUnitsToMap(earthAttackerMap, earthStagingAttackerMap);
        earthStagingAttackerMap = new HashMap<>();
    }

    /**
     * Will update and assign tasks to workers if there are idle workers. Loops through a list of idle workers.
     * If a sufficient number of workers have been assigned, pop off the task.
     */
    private static void updateTaskQueue() {
        if (earthTaskQueue.size() == 0) {
            System.out.println("Queue size is zero!");
            return;
        }

        while (earthTaskQueue.peek().getWorkersOnTask().size() >= earthTaskQueue.peek().getMinimumWorkers()) {
            earthTaskQueue.poll();
        }

        for (int workerId: earthWorkerMap.keySet()) {
            if (earthWorkerMap.get(workerId).isIdle()) {

                GlobalTask globalTask = earthTaskQueue.peek();
                int taskId = globalTask.getTaskId();

                if (!earthTaskMap.containsKey(globalTask.getTaskId())) {
                    globalTask.addWorkerToList(workerId);

                    System.out.println("Workers on task: " + globalTask.getTaskId() + " is " + globalTask.getWorkersOnTask().size());

                    earthTaskMap.put(taskId, globalTask);
                } else {
                    earthTaskMap.get(taskId);
                    globalTask.addWorkerToList(workerId);
                    System.out.println("Added worker: " + workerId + " to task: " + taskId);
                    System.out.println("Current workers on task: " + globalTask.getWorkersOnTask().size());
                }

                if (globalTask.getMinimumWorkers() == earthTaskMap.get(taskId).getWorkersOnTask().size()) {
                    earthTaskQueue.poll();
                    System.out.println("Task has enough workers! Polling: " + taskId);
                    if (earthTaskQueue.size() == 0) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * This method will be called when a factory or blueprint want to be constructed. This method will help
     * choose the location of the structure and add it to the global task list
     * @param command The command of the task that you want to be added to the global list
     */
    public static void createGlobalTask(Command command) {
        int minimumWorkers;
        MapLocation globalTaskLocation;

        switch (command) {
            //TODO: crashes if no location is found
            case CONSTRUCT_FACTORY:
                minimumWorkers = 4;
                globalTaskLocation = pickStructureLocation();
                planedStructureLocations.add(globalTaskLocation.toString());
                break;
            case CONSTRUCT_ROCKET:
                minimumWorkers = 6;
                globalTaskLocation = pickStructureLocation();
                planedStructureLocations.add(globalTaskLocation.toString());
                break;
            default:
                minimumWorkers = 4;
                globalTaskLocation = pickStructureLocation();
                planedStructureLocations.add(globalTaskLocation.toString());
                break;
        }

        earthTaskQueue.add(new GlobalTask(minimumWorkers, command, globalTaskLocation));
    }

    /**
     * Method that will pick the best MapLocation to build a structure
     * @return The MapLocation of the best place to build a structure or null if no locations exist or no availble workers exist
     */
    private static MapLocation pickStructureLocation() {
        ArrayList<MapLocation> clearLocations = new ArrayList<>();

        for (int x = 1; x < Player.gc.startingMap(Player.gc.planet()).getWidth() - 1; x++) {
            for (int y = 1; y < Player.gc.startingMap(Player.gc.planet()).getHeight() - 1; y++) {

                //location to test is the center location
                MapLocation locationToTest = new MapLocation(Player.gc.planet(), x, y);
                if (planedStructureLocations.contains(locationToTest.toString())) ;
                Boolean isClear = true;
                for (Direction direction : Direction.values()) {

                    //is already a planed location or is not passable terrain
                    if (planedStructureLocations.contains(locationToTest.add(direction).toString()) || Player.gc.startingMap(Player.gc.planet()).isPassableTerrainAt(locationToTest.add(direction)) == 0) {
                        isClear = false;
                        break;
                    }
                    //if has structure
                    if (Player.gc.canSenseLocation(locationToTest.add(direction)) && Player.gc.hasUnitAtLocation(locationToTest.add(direction)) && (Player.gc.senseUnitAtLocation(locationToTest.add(direction)).unitType() == UnitType.Factory || Player.gc.senseUnitAtLocation(locationToTest.add(direction)).unitType() == UnitType.Rocket)) {
                        isClear = false;
                        break;
                    }
                }
                if (isClear) {
                    clearLocations.add(locationToTest);
                }
            }
        }

        MapLocation closestLocation = null;
        long shortestDistance = 100000;

        //choose best location from list
        for (MapLocation location : clearLocations) {
            for (int workerId : earthWorkerMap.keySet()) {

                MapLocation workerLocation = Player.gc.unit(workerId).location().mapLocation();

                if (closestLocation == null) {
                    closestLocation = location;
                    shortestDistance = location.distanceSquaredTo(workerLocation);

                } else if (location.distanceSquaredTo(workerLocation) < shortestDistance) {
                    closestLocation = location;
                    shortestDistance = location.distanceSquaredTo(workerLocation);
                }
            }

        }
        return closestLocation;
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
            unitSet.add(units.get(i).id());
        }

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
    private static HashMap<Integer, UnitInstance> findDeadUnits(HashSet<Integer> unitSet, HashMap<Integer, UnitInstance> searchMap) {
        ArrayList<Integer> deadUnits = new ArrayList<>();
        for (int unitId: searchMap.keySet()) {
            if (!unitSet.contains(unitId)) {
                deadUnits.add(unitId);

                // If the unit is dead, we must update the HashSets of the tasks it was part of.
                UnitInstance unit = searchMap.get(unitId);
                if (unit.getCurrentTask() != null) {
                    int globalTaskId = unit.getCurrentTask().getTaskId();
                    earthTaskMap.get(globalTaskId).removeWorkerFromList(unitId);
                }
            }
        }

        for (int unitId: deadUnits) {
            System.out.println("Removing unit: " + unitId);
            searchMap.remove(unitId);
        }

        return searchMap;
    }

    /**
     * Method that will remove the completed tasks from the global current earth tasks
     * @param tasks The current tasks
     * @param finishedTasks The completed tasks
     * @return A new HashMap of current tasks
     */
    private static HashMap<Integer, GlobalTask> removeFinishedTasks(HashMap<Integer, GlobalTask> tasks, HashSet<Integer> finishedTasks) {
        for (int taskId: finishedTasks) {
            tasks.remove(taskId);
            System.out.println("Deleting task " + taskId);
        }

        return tasks;
    }

    /**
     * Method that will add all the robots created this round to their indicated unit map
     * @param unitMap The unit map you want to add robots to
     * @param stagingMap The map you are pulling the units from
     */
    private static HashMap<Integer, UnitInstance> addStagingUnitsToMap(HashMap<Integer, UnitInstance> unitMap, HashMap<Integer, UnitInstance> stagingMap) {
        for (int unitId: stagingMap.keySet()) {
            unitMap.put(unitId, stagingMap.get(unitId));
            System.out.println("Added unit: " + unitId + " To the current list");
        }

        return unitMap;
    }
}
