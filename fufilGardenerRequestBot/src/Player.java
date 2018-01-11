package src;

import bc.*;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

public class Player {

    private static final int MINIMUM_WORKER_THRESHOLD = 2;

    public static GameController gameController = new GameController();
    // public static HashMap<Integer, src.Robot> earthUnitsHashMap = new HashMap<>();
    public static HashMap<Integer, Worker> earthBusyWorkerHashMap = new HashMap<>();
    public static HashMap<Integer, Worker> earthIdleWorkerHashMap = new HashMap<>();
    public static HashMap<Integer, Worker> earthStagingWorkerHashMap = new HashMap<>();
    // public static HashMap<Integer, Building> earthStructureHashMap = new HashMap<>();
    // public static HashMap<Integer, Squadron> squadronHashMap = new HashMap<>();
    public static Queue<MapLocation> karboniteQueue = new PriorityQueue<>();
    public static Queue<Task> workerCurrentTaskQueue = new PriorityQueue<>();
    public static Queue<Task> workerNextRoundTaskQueue = new PriorityQueue<>();
    // public static Queue<src.Task> hitList = new PriorityQueue<>();
    // public static Stack<src.Task> emergencyTasks = new Stack<>();

    public static void main(String[] args) {

        addWorkersToHashMap(gameController);
        PlanetMap earthMap = gameController.startingMap(Planet.Earth);
        //findKarbonite(earthMap); // Need to write a method that will iterate over all spaces on the map
        // And will find the greatest concentrations of karbonite

        gameController.queueResearch(UnitType.Worker);
        gameController.queueResearch(UnitType.Ranger);
        gameController.queueResearch(UnitType.Rocket);
        workerNextRoundTaskQueue.add(Task.BUILD_FACTORY);
        workerNextRoundTaskQueue.add(Task.CLONE);

        while (true) {
            workerCurrentTaskQueue = workerNextRoundTaskQueue;
            workerNextRoundTaskQueue = new PriorityQueue<>();
            // removeDeadUnits(); // Need to wait for an implementation that will check if a unit died from the API
            System.out.println("Current round: " + gameController.round());

            // This method will iterate through the busy worker list and will move each robot according to
            // its assigned task. If the task is completed this turn, the robot should be removed from the
            // busy list and moved to the staging list for next round (implemented in execute task).
            for (int robotId: earthBusyWorkerHashMap.keySet()) {
                Worker worker = earthBusyWorkerHashMap.get(robotId);
                worker.executeTask();
            }

            // Iterates through the worker queue and will assign tasks to the idle workers. Helper methods
            // should remove the robot from the idle map and put it in the busy map.
            for (int i = 0; i < workerCurrentTaskQueue.size(); i++) {

                if (earthIdleWorkerHashMap.size() < MINIMUM_WORKER_THRESHOLD) {
                    workerNextRoundTaskQueue.add(Task.CLONE);
                    cloneWorker();
                    break;
                } else {
                    Task task = workerCurrentTaskQueue.poll();
                    switch (task) {
                        case BUILD_FACTORY:
                            buildFactory();
                            break;
                        case BUILD_ROCKET:
                            buildRocket();
                            break;
                        case CLONE:
                            cloneWorker();
                            break;
                    }
                }
            }

            // If there are still idle workers after completing all tasks, send them off to collect karbonite
            for (int i = 0; i < earthIdleWorkerHashMap.size(); i++) {
                // Given a queue of MapLocations, search for the nearest idle robot and send the task off to it
            }

            gameController.nextTurn();
        }
    }

    /**
     * Method that will need to clone an idle worker and will remove it from the idle worker HashMap. Add the
     * new robot to the staging area
     */
    public static void cloneWorker() {

    }

    /**
     * Method will find the optimal location to build a factory and will check if the player has enough
     * Karbonite. If all conditions are met, the worker will lay down a blueprint and will start to build the factory.
     * If the conditions are not met, the buildFactory task will be moved to next round and the robot will remain
     * in the idle robot HashMap.
     */
    public static void buildFactory() {

    }

    /**
     * Method will find the optimal location to build a rocket and will check if the player has enough
     * Karbonite. If all conditions are met, the worker will lay down a blueprint and will start to build the rocket.
     * If the conditions are not met, the buildRocket task will be moved to next round and the robot will remain
     * in the idle robot HashMap.
     */
    public static void buildRocket() {

    }

    /**
     * Method that will add all the workers on earth to the HashMap of workers at the beginning of the game
     * @param gameController The game controller
     */
    public static void addWorkersToHashMap(GameController gameController) {
        VecUnit units = gameController.myUnits();
        for (int i = 0; i < units.size(); i++) {
            int unitId = units.get(i).id();
            Worker worker = new Worker(unitId, null);
            earthIdleWorkerHashMap.put(unitId, worker);
        }
    }

    /**
     * Method that will return a random direction when called. Intended to be used for preliminary testing
     * @return One of 8 random directions
     */
    public static Direction pickRandomDirection() {
        int randomInt = (int)(Math.random()*8 + 1);
        switch (randomInt) {
            case 1:
                return Direction.North;
            case 2:
                return Direction.Northeast;
            case 3:
                return Direction.East;
            case 4:
                return Direction.Southeast;
            case 5:
                return Direction.South;
            case 6:
                return Direction.Southwest;
            case 7:
                return Direction.West;
            case 8:
                return Direction.Northwest;
            default:
                return Direction.Center;
        }
    }
}