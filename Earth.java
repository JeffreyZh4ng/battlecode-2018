import bc.*;

import java.util.*;

public class Earth {
    public static HashMap<Integer, GlobalTask> earthTaskMap = new HashMap<>();

    // TODO: Possibly change these two two to queues?
    public static HashMap<Integer, AttackTarget> earthAttackTargetsMap = new HashMap<>();
    // public static Queue<AttackTarget> earthProduceRobotMap = new LinkedList<>();

    public static HashMap<Integer, UnitInstance> earthRocketMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthWorkerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthAttackerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthFactoryMap = new HashMap<>();

    public static HashMap<Integer, GlobalTask> earthFinishedTasks = new HashMap<>();
    public static Queue<AttackTarget> eliminatedTargets = new LinkedList<>();

    public static HashMap<Integer, UnitInstance> earthStagingWorkerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthStagingAttackerMap = new HashMap<>();

    public static HashSet<String> planedStructureLocations = new HashSet<>();

    public void execute() {

        updateDeadUnits();

        updateTaskMap();

        runUnitMap(earthRocketMap);
        runUnitMap(earthWorkerMap);
        runUnitMap(earthAttackerMap);
        runUnitMap(earthFactoryMap);

        earthTaskMap = removeFinishedTasks(earthTaskMap, earthFinishedTasks);
        earthFinishedTasks = new HashMap<>();

        earthWorkerMap = addStagingUnitsToMap(earthWorkerMap, earthStagingWorkerMap);
        earthStagingWorkerMap = new HashMap<>();
        earthAttackerMap = addStagingUnitsToMap(earthAttackerMap, earthStagingAttackerMap);
        earthStagingAttackerMap = new HashMap<>();
    }

    /**
     * This method will be called when a factory or blueprint want to be constructed. This method will help
     * choose the location of the structure and add it to the global task list
     * @param command The command of the task that you want to be added to the global list
     */
    public void createGlobalTask(Command command) {
        MapLocation globalTaskLocation;

        switch (command) {
            //TODO: crashes if no location is found
            case CONSTRUCT_FACTORY:
                globalTaskLocation = pickStructureLocation();
                planedStructureLocations.add(globalTaskLocation.toString());
                break;
            case CONSTRUCT_ROCKET:
                globalTaskLocation = pickStructureLocation();
                planedStructureLocations.add(globalTaskLocation.toString());
                break;
            default:
                globalTaskLocation = pickStructureLocation();
                planedStructureLocations.add(globalTaskLocation.toString());
                break;
        }

        GlobalTask newGlobalTask = new GlobalTask(command, globalTaskLocation);
        int globalTaskId = newGlobalTask.getTaskId();

        earthTaskMap.put(globalTaskId, newGlobalTask);
    }

    /**
     * When a robot sees a target, send a attackTarget to the attack target map.
     * @param targetLocation The location of the target
     * @param targetId The id of the target(if applicable)
     */
    public void createAttackTarget(MapLocation targetLocation, int targetId) {

    }

    /**
     * Method that will pick the best MapLocation to build a structure
     * @return The MapLocation of the best place to build a structure or null if no locations exist or no avalible workers exist
     */
    private MapLocation pickStructureLocation() {

        ArrayList<MapLocation> clearLocations = new ArrayList<>();
        System.out.println("planned locs: "+planedStructureLocations);
        //iterate over all positions that could be surrounded by empty spaces
        for (int x = 1; x < Player.gc.startingMap(Player.gc.planet()).getWidth()-1; x++) {
            for (int y = 1; y < Player.gc.startingMap(Player.gc.planet()).getHeight()-1; y++) {

                //location to test is the center location
                MapLocation locationToTest = new MapLocation(Player.gc.planet(),x,y);
                if (planedStructureLocations.contains(locationToTest.toString()));
                Boolean isClear = true;
                for(Direction direction : Direction.values()) {

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
        System.out.println("clear loc count: " + clearLocations.size());
        MapLocation closestLocation = null;
        long shortestDistance = 1000; //the number this starts as should not matter

        //choose best location from list
        for (MapLocation location : clearLocations) {
            for (int workerId: earthWorkerMap.keySet()) {

                MapLocation workerLocation = Player.gc.unit(workerId).location().mapLocation();

                if (earthWorkerMap.get(workerId).getRobotTaskQueue().size() == 0) {

                    if (workerLocation.distanceSquaredTo(location) <= 32) {
                        return location;

                    } else if (closestLocation == null) {
                        closestLocation = location;
                        shortestDistance = location.distanceSquaredTo(workerLocation);

                    } else if (closestLocation.distanceSquaredTo(workerLocation) < shortestDistance) {
                        closestLocation = location;
                        shortestDistance = location.distanceSquaredTo(workerLocation);
                    }
                }
            }

        }
        System.out.println("closesloc: " + closestLocation);
        return closestLocation;
    }

    /**
     * Runs through the earthTaskMap and will update progress on each task
     */
    private void updateTaskMap() {
        for (int globalTaskId: earthTaskMap.keySet()) {
            GlobalTask globalTask = earthTaskMap.get(globalTaskId);
            Command taskCommand = earthTaskMap.get(globalTaskId).getCommand();

            if (globalTask.getWorkersOnTask().size() == 0) {
                sendTaskToClosestIdleWorker(globalTask);

            } else {
                switch (taskCommand) {
                    case CONSTRUCT_FACTORY:
                        manageConstruction(globalTask);
                        break;
                    case CONSTRUCT_ROCKET:
                        manageConstruction(globalTask);
                        break;
                    case LOAD_ROCKET:
                        break;
                    case BUILD:
                }
            }
        }
    }

    /**
     * Sends task to the nearest worker
     * @param globalTask The task you want to send to the worker
     */
    private void sendTaskToClosestIdleWorker(GlobalTask globalTask) {
        int workerId = findNearestIdleWorker();
        if (workerId == -1) {
            return;
        }

        int taskId = globalTask.getTaskId();
        globalTask.addWorkerToList(workerId);
        MapLocation taskLocation = globalTask.getTaskLocation();

        RobotTask moveTask = new RobotTask(taskId, 1, Command.MOVE, taskLocation);
        RobotTask blueprintTask;
        RobotTask buildTask = new RobotTask(taskId, 3, Command.BUILD, taskLocation);


        switch (globalTask.getCommand()) {
            case CONSTRUCT_FACTORY:
                earthWorkerMap.get(workerId).addTask(moveTask);

                blueprintTask = new RobotTask(taskId, 2, Command.BLUEPRINT_FACTORY, taskLocation);
                earthWorkerMap.get(workerId).addTask(blueprintTask);

                earthWorkerMap.get(workerId).addTask(buildTask);
                System.out.println("This should not run! Ran for worker " + workerId);

                break;
            case CONSTRUCT_ROCKET:
                earthWorkerMap.get(workerId).addTask(moveTask);

                blueprintTask = new RobotTask(taskId, 2, Command.BLUEPRINT_ROCKET, taskLocation);
                earthWorkerMap.get(workerId).addTask(blueprintTask);

                earthWorkerMap.get(workerId).addTask(buildTask);

                break;
            case LOAD_ROCKET:
                break;
        }
    }

    /**
     * Finds the nearest worker
     * @return The nearest worker
     */
    private int findNearestIdleWorker() {
        for (int workerId: earthWorkerMap.keySet()) {
            if (earthWorkerMap.get(workerId).getRobotTaskQueue().size() == 0) {
                return workerId;
            }
        }

        return -1;
    }

    /**
     * Method that is called every time the global task is updated. Workers will fulfill the priority task to
     * clone themselves and put them next to the structure. At the beginning of each round, the global task
     * will look for those new workers and add them to the list and send out robot commands.
     * @param globalTask The task you want to add workers to
     */
    private void manageConstruction(GlobalTask globalTask) {
        if (globalTask.getCompletionStage() == 4) {
            globalTask.incrementCompletionStage();
            System.out.println("Finished building! Waiting one turn to delete...");
        } else if (globalTask.getCompletionStage() == 5) {
            earthFinishedTasks.put(globalTask.getTaskId(), globalTask);

        } else {
//            VecUnit unitList = Player.gc.senseNearbyUnits(globalTask.getTaskLocation(), 2);
//
//            for (int i = 0; i < unitList.size(); i++) {
//                Unit unit = unitList.get(i);
//
//                // If the unit is on your team, a worker, and not already in the list
//                if (unit.team() == Player.gc.team() && unit.unitType() == UnitType.Worker && !globalTask.getWorkersOnTask().contains(unit.id())) {
//                    globalTask.addWorkerToList(unit.id());
//                    System.out.println("ADDED ROBOT: " + unit.id() + " TO LIST!");
//
//                    RobotTask blueprintTask;
//                    if (globalTask.getCommand() == Command.CONSTRUCT_FACTORY) {
//                        blueprintTask = new RobotTask(globalTask.getTaskId(), 2, Command.BLUEPRINT_FACTORY, globalTask.getTaskLocation());
//                    } else {
//                        blueprintTask = new RobotTask(globalTask.getTaskId(), 2, Command.BLUEPRINT_ROCKET, globalTask.getTaskLocation());
//
//                    }
//                    earthWorkerMap.get(unit.id()).addTask(blueprintTask);
//
//                    RobotTask buildTask = new RobotTask(globalTask.getTaskId(), 3, Command.BUILD, globalTask.getTaskLocation());
//                    earthWorkerMap.get(unit.id()).addTask(buildTask);
//                }
//            }
        }
    }

    /**
     * That that will run the execute() command for all the units in the given HashMap
     * @param searchMap The HashMap of units
     */
    private void runUnitMap(HashMap<Integer, UnitInstance> searchMap) {
        for (int unitId: searchMap.keySet()) {
            searchMap.get(unitId).run();
        }
    }

    /**
     * Since the method has not yet been implemented in the API, we must manually check if any unit died last round
     */
    private void updateDeadUnits() {
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
    private HashMap<Integer, UnitInstance> findDeadUnits(HashSet<Integer> unitSet, HashMap<Integer, UnitInstance> searchMap) {
        ArrayList<Integer> deadUnits = new ArrayList<>();
        for (int unitId: searchMap.keySet()) {
            if (!unitSet.contains(unitId)) {
                deadUnits.add(unitId);

                // If the unit is dead, we must update the HashSets of the tasks it was part of.
                UnitInstance unit = searchMap.get(unitId);
                for (int i = 0; i < unit.getRobotTaskQueue().size(); i++) {
                    int globalTaskId = unit.pollTask().getTaskId();
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
    private HashMap<Integer, GlobalTask> removeFinishedTasks(HashMap<Integer, GlobalTask> tasks, HashMap<Integer, GlobalTask> finishedTasks) {
        for (int taskId: finishedTasks.keySet()) {
            tasks.remove(taskId);
            System.out.println("Deleting tasks! Goodbye!");
        }

        return tasks;
    }

    /**
     * Method that will add all the robots created this round to their indicated unit map
     * @param unitMap The unit map you want to add robots to
     * @param stagingMap The map you are pulling the units from
     */
    private HashMap<Integer, UnitInstance> addStagingUnitsToMap(HashMap<Integer, UnitInstance> unitMap, HashMap<Integer, UnitInstance> stagingMap) {
        for (int unitId: stagingMap.keySet()) {
            unitMap.put(unitId, stagingMap.get(unitId));
        }

        return unitMap;
    }
}
