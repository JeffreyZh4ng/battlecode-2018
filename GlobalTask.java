import bc.MapLocation;
import bc.UnitType;

import java.util.HashSet;

public class GlobalTask {

    public static int taskIndex = 0;
    private static final int MAX_WORKER_COUNT = 10;

    private int taskId;
    private boolean isBuilt;
    private boolean hasBlueprinted;
    private HashSet<Integer> unitsOnTask;
    private Command command;
    private MapLocation taskLocation;

    public GlobalTask(Command command, MapLocation taskLocation) {
        taskIndex++;
        this.taskId = taskIndex;
        this.isBuilt = false;
        this.hasBlueprinted = false;
        this.command = command;
        this.unitsOnTask = new HashSet<>();
        this.taskLocation = taskLocation;
    }

    public int getTaskId() {
        return taskId;
    }

    public void structureHasBeenBlueprinted() {
        hasBlueprinted = true;
    }

    public void structureHasBeenBuilt() {
        isBuilt = true;
    }

    public Command getCommand() {
        return command;
    }

    public HashSet<Integer> getUnitsOnTask() {
        return unitsOnTask;
    }

    public MapLocation getTaskLocation() {
        return taskLocation;
    }

    /**
     * Adds a worker to the task list and send the worker the set of commands it needs to complete the global task
     * @param workerId The id of the worker being added
     */
    public void addWorkerToList(int workerId) {
        unitsOnTask.add(workerId);
        UnitInstance worker = Earth.earthWorkerMap.get(workerId);

        RobotTask moveTask = new RobotTask(taskId, Command.MOVE, taskLocation);
        worker.addTaskToQueue(moveTask);

        RobotTask blueprintTask;
        if (getCommand() == Command.CONSTRUCT_FACTORY) {
            blueprintTask = new RobotTask(taskId, Command.BLUEPRINT_FACTORY, taskLocation);
        } else {
            blueprintTask = new RobotTask(taskId, Command.BLUEPRINT_ROCKET, taskLocation);
        }
        worker.addTaskToQueue(blueprintTask);

        if (Earth.earthWorkerMap.size() < MAX_WORKER_COUNT) {
            RobotTask cloneTask = new RobotTask(taskId, Command.CLONE, taskLocation);
            worker.addTaskToQueue(cloneTask);
        }

        RobotTask buildTask = new RobotTask(taskId, Command.MOVE, taskLocation);
        worker.addTaskToQueue(buildTask);
    }

    /**
     * Method that will be used only when a rocket wants to be loaded. Adds the unit to the list and updates
     * their tasks in their respective maps.
     * @param unitId The id of the unit
     */
    public void addUnitToList(int unitId) {
        unitsOnTask.add(unitId);
        UnitInstance unit;

        if (Player.gc.unit(unitId).unitType() == UnitType.Worker) {
            unit = Earth.earthWorkerMap.get(unitId);
        } else {
            unit = Earth.earthAttackerMap.get(unitId);
        }

        RobotTask moveTask = new RobotTask(taskId, Command.MOVE, taskLocation);
        unit.addTaskToQueue(moveTask);

        RobotTask stallTask = new RobotTask(taskId, Command.STALL, taskLocation);
        unit.addTaskToQueue(stallTask);
    }

    /**
     * Removes a worker from the list and at the same time tries to find another one to complete the tasks
     * @param workerId The id of the worker being removed
     */
    public void removeWorkerFromList(int workerId) {
        if (unitsOnTask.contains(workerId)) {
            System.out.println("Removing worker: " + workerId);
            unitsOnTask.remove(workerId);
        } else {
            System.out.println("Worker: " + workerId + " was not part of the task?");
        }

        // Assign the task to another worker. If none are found, it stops trying to find another unit
        for (int unitId: Earth.earthWorkerMap.keySet()) {
            if (Earth.earthWorkerMap.get(unitId).getCurrentTask().getTaskId() == -1) {
                addWorkerToList(unitId);
                break;
            }
        }
    }

    /**
     * Method that will check the global task status.
     * @return If the task has been completed or not
     */
    public boolean checkGlobalTaskStatus(Command command) {
        switch (command) {
            case BLUEPRINT_FACTORY:
                return hasBlueprinted;
            case BLUEPRINT_ROCKET:
                return hasBlueprinted;
            case BUILD:
                return isBuilt;
            case STALL:
                return false;
            default:
                return false;
        }
    }

//    /**
//     * Helper method that will decide whether the next robot will build or clone as the next task
//     * @param unitId The id of the robot who's next task you want to determine
//     */
//    private void buildOrCloneHelper(int unitId) {
//        RobotTask nextTask;
//        if (Earth.earthWorkerMap.size() < MAX_WORKER_COUNT) {
//            nextTask = new RobotTask(this.getTaskId(), Command.CLONE, this.getTaskLocation());
//        } else {
//            nextTask = new RobotTask(this.getTaskId(), Command.BUILD, this.getTaskLocation());
//        }
//        Earth.earthWorkerMap.get(unitId).setCurrentTask(nextTask);
//    }
//
//    /**
//     * Helper method that will control the requests to the rocket and robots for loading the rocket. If the
//     * rocket cannot load the robot immediately, it will send a stall request to the unit.
//     * @param unitId The id of the unit who finished its task
//     */
//    private void sendRocketRequestHelper(int unitId) {
//        int rocketId = Player.gc.senseUnitAtLocation(this.getTaskLocation()).id();
//        if (Earth.earthRocketMap.get(rocketId).loadUnit(unitId)) {
//            RobotTask nextTask = new RobotTask(this.getTaskId(), Command.STALL, this.getTaskLocation());
//            if (Player.gc.unit(unitId).unitType() == UnitType.Worker) {
//                System.out.println("Setting  task to stall unit: " + unitId);
//                Earth.earthWorkerMap.get(unitId).setEmergencyTask(nextTask);
//            } else {
//                System.out.println("Setting  task to stall unit: " + unitId);
//                Earth.earthAttackerMap.get(unitId).setEmergencyTask(nextTask);
//            }
//            return;
//        }
//
//        RobotTask nextTask = new RobotTask(this.getTaskId(), Command.STALL, this.getTaskLocation());
//        if (Player.gc.unit(unitId).unitType() == UnitType.Worker) {
//            System.out.println("Setting emergency task to stall unit: " + unitId);
//            Earth.earthWorkerMap.get(unitId).setEmergencyTask(nextTask);
//        } else {
//            System.out.println("Setting emergency task to stall unit: " + unitId);
//            Earth.earthAttackerMap.get(unitId).setEmergencyTask(nextTask);
//        }
//    }
//
//    /**
//     * When the last command in the string of tasks is finished. Remove the task of all the rest of the units.
//     * If the task is still the top task of the task queue, remove it
//     */
//    private void finishedTask() {
//        for (int unitId: unitsOnTask) {
//            if (Earth.earthWorkerMap.containsKey(unitId)) {
//                Earth.earthWorkerMap.get(unitId).removeTask();
//            } else {
//                Earth.earthAttackerMap.get(unitId).removeTask();
//            }
//        }
//
//        if (Earth.earthTaskQueue.size() != 0 && Earth.earthTaskQueue.peek().getTaskId() == this.getTaskId()) {
//            Earth.earthTaskQueue.poll();
//        }
//    }
//
//    /**
//     * Helper method that will ensure that once the blueprint is created that any duplicate blueprint tasks
//     * are removed and the robots are given a new task
//     */
//    private void removeDuplicateTasks() {
//        for (int unitId: unitsOnTask) {
//            UnitInstance unit = Earth.earthWorkerMap.get(unitId);
//            if (unit.getCurrentTask().getCommand() == Command.BLUEPRINT_FACTORY ||
//                    unit.getCurrentTask().getCommand() == Command.BLUEPRINT_ROCKET) {
//                buildOrCloneHelper(unitId);
//            }
//        }
//    }
}
