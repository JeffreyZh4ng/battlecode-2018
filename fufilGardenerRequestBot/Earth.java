import bc.GameController;
import bc.MapLocation;
import bc.VecUnit;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

public class Earth {

    private static final int MINIMUM_WORKER_THRESHOLD = 2;

    public static HashMap<Integer, Worker> earthBusyWorkerHashMap = new HashMap<>();
    public static HashMap<Integer, Worker> earthIdleWorkerHashMap = new HashMap<>();
    public static HashMap<Integer, Worker> earthStagingWorkerHashMap = new HashMap<>();

    public static Queue<MapLocation> karboniteQueue = new PriorityQueue<>();
    public static Queue<Task> workerEarthCurrentTaskQueue = new PriorityQueue<>();
    public static Queue<Task> workerEarthNextRoundTaskQueue = new PriorityQueue<>();

    public void execute() {
        workerEarthCurrentTaskQueue = workerEarthNextRoundTaskQueue;
        workerEarthNextRoundTaskQueue = new PriorityQueue<>();
        // removeDeadUnits(); // Need to wait for an implementation that will check if a unit died from the API
        System.out.println("Current round: " + Player.gameController.round());

        // This method will iterate through the busy worker list and will move each robot according to
        // its assigned task. If the task is completed this turn, the robot should be removed from the
        // busy list and moved to the staging list for next round (implemented in execute task).
        for (int workerId: earthBusyWorkerHashMap.keySet()) {
            Worker worker = earthBusyWorkerHashMap.get(workerId);
            worker.executeTask();
        }

        // Iterates through the worker queue and will assign tasks to the idle workers. Helper methods
        // should remove the robot from the idle map and put it in the busy map.
        for (int i = 0; i < workerEarthCurrentTaskQueue.size(); i++) {

            int workerId;
            Task task = workerEarthCurrentTaskQueue.poll();

            if (earthIdleWorkerHashMap.size() < MINIMUM_WORKER_THRESHOLD) {
                cloneWorkerHelper(task);

            } else {
                switch (task) {
                    case BUILD_FACTORY:
                        buildFactoryHelper(task);
                        break;

                    case BUILD_ROCKET:
                        buildRocketHelper(task);
                        break;

                    case CLONE:
                        cloneWorkerHelper(task);
                        break;
                }
            }
        }

        // If there are still idle workers after completing all tasks, send them off to collect karbonite
        for (int i = 0; i < earthIdleWorkerHashMap.size(); i++) {
            // int robotId = selectWorkerForTask()
            // Given a queue of MapLocations, search for the nearest idle robot and send the task off to it
        }

        // Removes workers from the staging area to the idle map at the end of each round
        System.out.println("Staging workers size: " + earthStagingWorkerHashMap.keySet().size());
        for (int workerId: earthStagingWorkerHashMap.keySet()) {
            Worker worker = earthStagingWorkerHashMap.get(workerId);
            earthIdleWorkerHashMap.put(workerId, worker);
        }
        earthStagingWorkerHashMap = new HashMap<>();
    }

    /**
     * Helper method that will move the worker instance from one HasMap to another based on if it can perform
     * the action or not
     */
    private static void buildFactoryHelper(Task task) {
        int workerId;
        workerId = selectWorkerForTask(Task.BUILD_FACTORY);
        if(earthIdleWorkerHashMap.get(workerId).buildFactory()) {
            Worker worker = earthIdleWorkerHashMap.get(workerId);
            earthIdleWorkerHashMap.remove(workerId);
            earthBusyWorkerHashMap.put(workerId, worker);
        } else {
            workerEarthNextRoundTaskQueue.add(task);
        }
    }

    /**
     * Helper method that will move the worker instance from one HasMap to another based on if it can perform
     * the action or not
     */
    private static void buildRocketHelper(Task task) {
        int workerId;
        workerId = selectWorkerForTask(Task.BUILD_ROCKET);
        if (earthIdleWorkerHashMap.get(workerId).buildRocket()) {
            Worker worker = earthIdleWorkerHashMap.get(workerId);
            earthIdleWorkerHashMap.remove(workerId);
            earthBusyWorkerHashMap.put(workerId, worker);
        } else {
            workerEarthNextRoundTaskQueue.add(task);
        }
    }

    /**
     * Helper method that will move the worker instance from one HasMap to another based on if it can perform
     * the action or not
     */
    private static void cloneWorkerHelper(Task task) {
        int workerId;
        workerId = selectWorkerForTask(Task.CLONE);
        if (earthIdleWorkerHashMap.get(workerId).cloneWorker()) {
            Worker worker = earthIdleWorkerHashMap.get(workerId);
            earthIdleWorkerHashMap.remove(workerId);
            earthStagingWorkerHashMap.put(workerId, worker);
        } else {
            workerEarthNextRoundTaskQueue.add(task);
        }
        return;
    }

    /**
     * This method will return the best worker to complete a given task. This method will then return the int
     * id that robot is referenced by in the HashMap. The instance of the robot will then implement the task
     * @param task The given task a worker needs to complete
     * @return The id of the worker most suited to complete a task
     */
    public static int selectWorkerForTask(Task task) {
        switch (task) {
            case BUILD_FACTORY:
                break;
            case BUILD_ROCKET:
                break;
            case CLONE:
                System.out.println(earthIdleWorkerHashMap.keySet().size());
                Integer[] keySet = earthIdleWorkerHashMap.keySet()
                        .toArray(new Integer[earthIdleWorkerHashMap.keySet().size()]);
                return keySet[0];
        }
        return 1;
    }

    /**
     * Method will be given a karbonite location and will return the id of the closest idle worker. Method
     * much more depth. Dont want to send a worker to a deposit where one worker is already near ect.
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
}
