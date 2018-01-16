import bc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Earth {
    public static HashMap<Integer, GlobalTask> earthTaskMap = new HashMap<>();

    // TODO: Possibly change these two two to queues?
    public static HashMap<Integer, GlobalTask> earthAttackTargetsMap = new HashMap<>();
    public static HashMap<Integer, GlobalTask> earthProduceRobotMap = new HashMap<>();

    public static HashMap<Integer, UnitInstance> earthRocketMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthWorkerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthFactoryMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthAttackerMap = new HashMap<>();

    public static HashMap<Integer, GlobalTask> earthFinishedTasks = new HashMap<>();

    public static HashMap<Integer, UnitInstance> earthStagingWorkerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthStagingAttackerMap = new HashMap<>();

    public void execute() {

        updateDeadUnits();

        updateTaskMap();

        runUnitMap(earthRocketMap);
        runUnitMap(earthWorkerMap);
        runUnitMap(earthFactoryMap);
        runUnitMap(earthAttackerMap);

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
            case CONSTRUCT_FACTORY:
                globalTaskLocation = pickStructureLocation();
                break;
            case CONSTRUCT_ROCKET:
                globalTaskLocation = pickStructureLocation();
                break;
            default:
                globalTaskLocation = pickStructureLocation();
                break;
        }

        GlobalTask newGlobalTask = new GlobalTask(command, globalTaskLocation);
        int globalTaskId = newGlobalTask.getTaskId();

        earthTaskMap.put(globalTaskId, newGlobalTask);
    }

    public void wrapUpGlobalTask(GlobalTask globalTask) {
        for (int workerId: globalTask.getWorkersOnTask()) {
            UnitInstance worker = earthWorkerMap.get(workerId);
        }
    }

    /**
     * Method that will pick the best MapLocation to build a structure
     * @return The MapLocation of the best place to build a structure
     */
    // TODO: Need to implement this method... Obviously
    private MapLocation pickStructureLocation() {
        int x = (int)(Math.random()*21);
        int y = (int)(Math.random()*21);
        System.out.println("Coordinates: " + x + ", " + y);
        return new MapLocation(Planet.Earth, x, y);
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
        } else if (globalTask.getCompletionStage() == 5) {
            earthFinishedTasks.put(globalTask.getTaskId(), globalTask);

        } else {
            VecUnit unitList = Player.gc.senseNearbyUnits(globalTask.getTaskLocation(), 2);

            for (int i = 0; i < unitList.size(); i++) {
                Unit unit = unitList.get(i);

                // If the unit is on your team, a worker, and not already in the list
                if (unit.team() == Player.gc.team() && unit.unitType() == UnitType.Worker && !globalTask.getWorkersOnTask().contains(unit.id())) {
                    globalTask.addWorkerToList(unit.id());
                    System.out.println("ADDED ROBOT: " + unit.id() + " TO LIST!");

                    RobotTask blueprintTask;
                    if (globalTask.getCommand() == Command.CONSTRUCT_FACTORY) {
                        blueprintTask = new RobotTask(globalTask.getTaskId(), 2, Command.BLUEPRINT_FACTORY, globalTask.getTaskLocation());
                    } else {
                        blueprintTask = new RobotTask(globalTask.getTaskId(), 2, Command.BLUEPRINT_ROCKET, globalTask.getTaskLocation());

                    }
                    earthWorkerMap.get(unit.id()).addTask(blueprintTask);

                    RobotTask buildTask = new RobotTask(globalTask.getTaskId(), 3, Command.BUILD, globalTask.getTaskLocation());
                    earthWorkerMap.get(unit.id()).addTask(buildTask);
                }
            }
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
