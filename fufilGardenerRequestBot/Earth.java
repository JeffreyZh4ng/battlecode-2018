import bc.GameController;
import bc.MapLocation;
import bc.VecUnit;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

public class Earth {

    private static final int MINIMUM_WORKER_THRESHOLD = 2;
    private static final int BEST_LAUNCH_ROUND = 375;

    public static HashMap<Integer, Worker> earthBusyWorkerHashMap = new HashMap<>();
    public static HashMap<Integer, Worker> earthIdleWorkerHashMap = new HashMap<>();
    public static HashMap<Integer, Worker> earthStagingWorkerHashMap = new HashMap<>();

    public static Queue<MapLocation> karboniteQueue = new PriorityQueue<>();
    public static Queue<Command> workerEarthCurrentCommandQueue = new PriorityQueue<>();
    public static Queue<Command> workerEarthNextRoundCommandQueue = new PriorityQueue<>();

    public void execute() {
        workerEarthCurrentCommandQueue = new PriorityQueue<>();
        workerEarthCurrentCommandQueue.addAll(workerEarthNextRoundCommandQueue);
        workerEarthNextRoundCommandQueue = new PriorityQueue<>();
        // removeDeadUnits(); // Need to wait for an implementation that will check if a unit died from the API

        System.out.println("Current round: " + Player.gameController.round());
        System.out.println("Workers on standby: " + earthIdleWorkerHashMap.size());
        printIdleWorkers();
        remainingTasksInQueue();

        // This method will iterate through the busy worker list and will move each robot according to
        // its assigned task. If the task is completed this turn, the robot should be removed from the
        // busy list and moved to the staging list for next round (implemented in execute task).
        HashMap<Integer, Worker> tempBusyWorkerHashMap = new HashMap<>();
        for (int workerId: earthBusyWorkerHashMap.keySet()) {
            Worker worker = earthBusyWorkerHashMap.get(workerId);
            if (worker.executeTask()) {
                earthIdleWorkerHashMap.put(workerId, worker);
            } else {
                tempBusyWorkerHashMap.put(workerId, worker);
            }
        }
        earthBusyWorkerHashMap = tempBusyWorkerHashMap;

        // Iterates through the worker queue and will assign tasks to the idle workers. Helper methods
        // should remove the robot from the idle map and put it in the busy map.
        int currentTaskCount = workerEarthCurrentCommandQueue.size();
        for (int i = 0; i < currentTaskCount; i++) {

            if (earthIdleWorkerHashMap.size() == 0) {
                break;
            }

            Command command = workerEarthCurrentCommandQueue.poll();

            switch (command) {
                case BLUEPRINT_FACTORY:
                    blueprintFactoryHelper(command);
                    break;

                case BLUEPRINT_ROCKET:
                    blueprintRocketHelper(command);
                    break;

                case CLONE:
                    cloneWorkerHelper(command);
                    break;
            }
        }

        System.out.println("There are: " + earthIdleWorkerHashMap.size() + " Idle workers left");
        // If there are still idle workers after completing all tasks, send them off to collect karbonite
        for (int i = 0; i < earthIdleWorkerHashMap.size(); i++) {
            // int robotId = selectWorkerForTask()
            // Given a queue of MapLocations, search for the nearest idle robot and send the task off to it
        }

        // Removes workers from the staging area to the idle map at the end of each round
        // System.out.println("Staging workers size: " + earthStagingWorkerHashMap.keySet().size());
        for (int workerId: earthStagingWorkerHashMap.keySet()) {
            Worker worker = earthStagingWorkerHashMap.get(workerId);
            earthIdleWorkerHashMap.put(workerId, worker);
        }
        earthStagingWorkerHashMap = new HashMap<>();
    }

    private void printIdleWorkers() {
        for (int id: earthIdleWorkerHashMap.keySet()) {
            System.out.println("Worker " + id);
        }
    }

    /**
     * Helper method that will move the worker instance from one HasMap to another based on if it can perform
     * the action or not
     */
    private static void blueprintFactoryHelper(Command command) {
        int workerId = selectWorkerForTask(Command.BLUEPRINT_FACTORY);
        if(earthIdleWorkerHashMap.get(workerId).blueprintFactory()) {
            Worker worker = earthIdleWorkerHashMap.get(workerId);
            earthIdleWorkerHashMap.remove(workerId);
            earthBusyWorkerHashMap.put(workerId, worker);
        } else {
            workerEarthNextRoundCommandQueue.add(command);
        }
    }

    /**
     * Helper method that will move the worker instance from one HasMap to another based on if it can perform
     * the action or not
     */
    private static void blueprintRocketHelper(Command command) {
        int workerId = selectWorkerForTask(Command.BLUEPRINT_ROCKET);
        if (earthIdleWorkerHashMap.get(workerId).buildRocket()) {
            Worker worker = earthIdleWorkerHashMap.get(workerId);
            earthIdleWorkerHashMap.remove(workerId);
            earthBusyWorkerHashMap.put(workerId, worker);
        } else {
            workerEarthNextRoundCommandQueue.add(command);
        }
    }

    /**
     * Helper method that will move the worker instance from one HasMap to another based on if it can perform
     * the action or not
     */
    private static void cloneWorkerHelper(Command command) {
        int workerId = selectWorkerForTask(Command.CLONE);
        if (earthIdleWorkerHashMap.get(workerId).cloneWorker()) {
            Worker worker = earthIdleWorkerHashMap.get(workerId);
            earthIdleWorkerHashMap.remove(workerId);
            earthStagingWorkerHashMap.put(workerId, worker);
        } else {
            workerEarthNextRoundCommandQueue.add(command);
        }
        return;
    }

    /**
     * If there are no more idle workers, no more tasks will be able to be completed. Must move the remaining
     * tasks to the next round queue
     */
    private static void moveRemainingTasksToNextRound() {
        workerEarthNextRoundCommandQueue.addAll(workerEarthCurrentCommandQueue);
        workerEarthCurrentCommandQueue = new PriorityQueue<>();
    }

    /**
     * This method will return the best worker to complete a given command. This method will then return the int
     * id that robot is referenced by in the HashMap. The instance of the robot will then implement the command
     * @param command The given command a worker needs to complete
     * @return The id of the worker most suited to complete a command
     */
    public static int selectWorkerForTask(Command command) {
        Integer[] keySet;
        switch (command) {
            case BLUEPRINT_FACTORY:
                keySet = earthIdleWorkerHashMap.keySet()
                        .toArray(new Integer[earthIdleWorkerHashMap.keySet().size()]);
                return keySet[0];
            case BLUEPRINT_ROCKET:
                keySet = earthIdleWorkerHashMap.keySet()
                        .toArray(new Integer[earthIdleWorkerHashMap.keySet().size()]);
                return keySet[0];
            case CLONE:
                keySet = earthIdleWorkerHashMap.keySet()
                        .toArray(new Integer[earthIdleWorkerHashMap.keySet().size()]);
                return keySet[0];
        }
        return 1;
    }

    /**
     * Method will be given a karbonite location and will return the id of the closest idle worker. Method
     * much more depth. Don't want to send a worker to a deposit where one worker is already near ect.
     * @param mapLocation The location of the karbonite deposit
     * @return The id of the worker that will be sent to mine the karbonite
     */
    public static int selectWorkerForTask(MapLocation mapLocation) {
        return 1;
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

    private static void remainingTasksInQueue() {
        Queue<Command> copyOfCurrentQueue = new PriorityQueue<>();
        copyOfCurrentQueue.addAll(workerEarthCurrentCommandQueue);
        for (int i = 0; i < workerEarthCurrentCommandQueue.size(); i++) {
            System.out.println(copyOfCurrentQueue.poll());
        }
    }
}
