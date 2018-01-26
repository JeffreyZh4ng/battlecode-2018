import bc.*;

import java.util.*;

public class Earth {

    private static final int WORKERS_ON_CONSTRUCT_TASK = 4;
    private static final int UNITS_ON_LOAD_TASK = 8;

    public static int knightCount = 0;
    public static int rangerCount = 0;
    public static int mageCount = 0;
    public static int healerCount = 0;

    public static Queue<MapLocation> earthMainAttackQueue = new LinkedList<>();
    public static HashSet<Integer> earthFocusedTargets = new HashSet<>();

    public static Queue<GlobalTask> earthTaskQueue = new LinkedList<>();
    public static HashMap<Integer, GlobalTask> earthTaskMap = new HashMap<>();

    public static HashMap<Integer, Rocket> earthRocketMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthWorkerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthAttackerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthFactoryMap = new HashMap<>();

    public static HashSet<Integer> earthGarrisonedUnits = new HashSet<>();
    public static HashMap<Integer, UnitInstance> earthStagingWorkerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> earthStagingAttackerMap = new HashMap<>();

    public static HashMap<String, Integer> earthKarboniteMap = initializeKarboniteMap();
    public static ArrayList<MapLocation> availableStructureLocations = null;

    public static void execute() {
        updateKarboniteMap();
        updateDeadUnits();

        updateTaskQueue();

        runRocketMap();

        runUnitMap(earthWorkerMap);
        runUnitMap(earthAttackerMap);
        runUnitMap(earthFactoryMap);

        removeGarrisonedUnits();

        addStagingUnitsToMap();
    }

    /**
     * This method will be called when a factory or blueprint want to be constructed. This method will help
     * choose the location of the structure and add it to the global task list
     * @param command The command of the task that you want to be added to the global list
     */
    public static void createGlobalTask(Command command) {
        MapLocation globalTaskLocation = pickStructureLocation();
        System.out.println("Picked location: " + globalTaskLocation.toString());

        earthTaskQueue.add(new GlobalTask(command, globalTaskLocation));
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

        GlobalTask globalTask = earthTaskQueue.peek();
        if (!earthTaskMap.containsKey(globalTask.getTaskId())) {
            earthTaskMap.put(globalTask.getTaskId(), globalTask);
            System.out.println("Adding task: " + globalTask.getTaskId() + " to the map!");
        }

        if (globalTask.getCommand() == Command.LOAD_ROCKET) {
            if (getUnitsToLoadRocket(globalTask)) {
                earthTaskQueue.poll();
                System.out.println("Units have been assigned for task: " + globalTask.getTaskId());
                updateTaskQueue();
            }
        } else {
            if (getWorkersToConstruct(globalTask)) {
                System.out.println("Units have been assigned for task: " + globalTask.getTaskId());
                earthTaskQueue.poll();
                updateTaskQueue();
            }
        }
    }

    /**
     * Helper method that will manage assigning tasks to units for loading rockets
     * @param globalTask The current global task that is at the top of the queue
     * @return If the units have been assigned to load the rocket have been assigned tasks
     */
    private static boolean getUnitsToLoadRocket(GlobalTask globalTask) {
        int unitsOnTaskCount = globalTask.getUnitsOnTask().size();

        ArrayList<Integer> unitSet = new ArrayList<>();
        if (unitsOnTaskCount < 1) {
            unitSet.addAll(Player.getNearestFriendlyUnit(globalTask.getTaskLocation(), true, 1));
            unitsOnTaskCount++;
        }

        unitSet.addAll(Player.getNearestFriendlyUnit(globalTask.getTaskLocation(), false,
                UNITS_ON_LOAD_TASK - unitsOnTaskCount));

        for (Integer unitId : unitSet) {
            globalTask.addUnitToList(unitId);
        }

        return globalTask.getUnitsOnTask().size() >= UNITS_ON_LOAD_TASK;
    }

    /**
     * Helper method that will manage assigning tasks to workers for constructing structures
     * @param globalTask The current global task that is at the top of the queue
     * @return If workers have been assigned to complete the task return true
     */
    private static boolean getWorkersToConstruct(GlobalTask globalTask) {

        // Before doing any searching, check if the task has been completed already
        if (globalTask.hasBuilt()) {
            return true;
        }

        int workersOnTaskCount = globalTask.getUnitsOnTask().size();

        ArrayList<Integer> workerSet = Player.getNearestFriendlyUnit(globalTask.getTaskLocation(),
                true, WORKERS_ON_CONSTRUCT_TASK - workersOnTaskCount);

        for (Integer workerId : workerSet) {
            if (!globalTask.getUnitsOnTask().contains(workerId)) {
                globalTask.addWorkerToList(workerId);
                System.out.println("Added worker " + workerId + " to task " + globalTask.getTaskId());
            }
        }

        return globalTask.getUnitsOnTask().size() >= WORKERS_ON_CONSTRUCT_TASK;
    }

    /**
     * finds all initial karbonite locations
     * @return a HashMap of locations with karbonite initially
     */
    // TODO: This should go in the Player class
    private static HashMap<String, Integer> initializeKarboniteMap() {
        PlanetMap initialMap = Player.gc.startingMap(Player.gc.planet());
        HashMap<String, Integer> initialKarboniteValues = new HashMap<>();
        for (int x = 0; x < initialMap.getWidth(); x++) {
            for (int y = 0; y < initialMap.getHeight(); y++) {
                MapLocation location = new MapLocation(Player.gc.planet(), x, y);
                if (initialMap.initialKarboniteAt(location) > 0) {
                    initialKarboniteValues.put(Player.locationToString(location), (int)initialMap.initialKarboniteAt(location));
                }
            }
        }
        return initialKarboniteValues;
    }

    /**
     * Method that will update the karbonite values on the map at the beginning of each round.
     */
    // TODO: Rework
    private static void updateKarboniteMap() {
        ArrayList<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Integer> locationEntry : earthKarboniteMap.entrySet()) {
            MapLocation location = Player.stringToLocation(locationEntry.getKey());

            if (Player.gc.canSenseLocation(location)) {
                int karboniteAt = (int)Player.gc.karboniteAt(location);
                if (karboniteAt == 0) {
                    toRemove.add(locationEntry.getKey());
                } else if (karboniteAt != locationEntry.getValue()) {
                    earthKarboniteMap.put(locationEntry.getKey(), karboniteAt);
                }

            }
        }
        for (String location : toRemove) {
            earthKarboniteMap.remove(location);
        }
    }

    /**
     * Method that will pick the best MapLocation to build a structure
     * @return The MapLocation of the best place to build a structure or null if no locations exist or no available workers exist
     */
    // TODO: Rework
    private static MapLocation pickStructureLocation() {
        MapLocation closestLocation = null;
        long shortestDistance = 100000;

        //choose best location from list
        for (MapLocation location : availableStructureLocations) {
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
        availableStructureLocations.remove(closestLocation);
        return closestLocation;
    }

//    /**
//     * Method that will check if we need to send out a wave of clone emergency tasks. Checks if the worker
//     * already has an emergency task. If it doesn't it will a receive a clone emergency task
//     */
//    private static void doWeNeedWorkers() {
//        if (earthWorkerMap.size() < NUMBER_OF_WORKERS_NEEDED) {
//            for (int workerId: earthWorkerMap.keySet()) {
//
//                UnitInstance worker = earthWorkerMap.get(workerId);
//                if (worker.getEmergencyTask() == null) {
//                    System.out.println("Worker " + workerId + " received a clone task!");
//                    worker.setEmergencyTask(new RobotTask(-1, Command.CLONE, worker.getLocation()));
//                }
//            }
//        }
//    }

    /**
     * Update and remove launched rocket. Needs to be specific to for rockets because of their unique functionality
     */
    private static void runRocketMap() {
        ArrayList<Integer> rocketRemoveList = new ArrayList<>();

        // Loops through and checks if the rocket can take off. If it takes off, remove it from the rocket list
        for (int rocketId: earthRocketMap.keySet()) {
            Rocket rocket = earthRocketMap.get(rocketId);
            rocket.run();
            if (rocket.isInFlight()) {
                rocketRemoveList.add(rocketId);
            }
        }

        for (int rocketId: rocketRemoveList) {
            earthRocketMap.remove(rocketId);
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
            if (units.get(i).location().isOnPlanet(Planet.Earth)) {
                unitSet.add(units.get(i).id());
            }
        }

        decrementAttackCounts(unitSet);
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
                if (unit.getCurrentTask() != null && unit.getCurrentTask().getTaskId() != -1) {
                    int globalTaskId = unit.getCurrentTask().getTaskId();
                    System.out.println("Removing unit from task: " + globalTaskId);
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
     * Helper method for the updateDeadUnits method that will decrement all the values of the current attacking units
     * @param unitSet The set of units returned by the Game Controller
     */
    private static void decrementAttackCounts(HashSet<Integer> unitSet) {
        for (int unitId: earthAttackerMap.keySet()) {
            if (!unitSet.contains(unitId)) {

                switch (earthAttackerMap.get(unitId).getUnitType()) {
                    case Knight:
                        knightCount--;
                    case Ranger:
                        System.out.println("Decremented ranger count!");
                        rangerCount--;
                    case Mage:
                        mageCount--;
                    case Healer:
                        healerCount--;
                }
            }
        }
    }

    /**
     * Helper method that will remove all garrisoned units from their respective hash maps so they run next round
     */
    private static void removeGarrisonedUnits() {
        for (int unitId: earthGarrisonedUnits) {
            if (earthWorkerMap.containsKey(unitId)) {
                earthWorkerMap.remove(unitId);
            } else if (earthAttackerMap.containsKey(unitId)) {
                earthAttackerMap.remove(unitId);
            }
        }

        earthGarrisonedUnits.clear();
    }

    /**
     * Method that will add all the robots created this round to their indicated unit map
     */
    private static void addStagingUnitsToMap() {
        for (int unitId : earthStagingWorkerMap.keySet()) {
            earthWorkerMap.put(unitId, earthStagingWorkerMap.get(unitId));
            System.out.println("Added unit: " + unitId + " To the worker list");
        }
        earthStagingWorkerMap.clear();

        for (int unitId : earthStagingAttackerMap.keySet()) {
            earthAttackerMap.put(unitId, earthStagingAttackerMap.get(unitId));
            System.out.println("Added unit: " + unitId + " To the attacker list");
        }
        earthStagingAttackerMap.clear();
    }
}
