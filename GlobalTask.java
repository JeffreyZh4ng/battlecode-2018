import bc.MapLocation;
import bc.UnitType;

import java.util.HashSet;

public class GlobalTask {

    public static int taskIndex = 0;
    private static final int MAX_WORKER_COUNT = 4;

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

    public boolean hasBlueprinted() {
        return hasBlueprinted;
    }

    public void structureHasBeenBuilt() {
        isBuilt = true;
    }

    public boolean hasBuilt() {
        return isBuilt;
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

        // If the worker currently is wandering, then poll the task
        if (worker.hasTasks() && worker.getCurrentTask().getCommand() == Command.WANDER) {
            System.out.println("Worker: " + worker.getId() + " removed its wander task");
            worker.pollCurrentTask();
        }

        RobotTask moveTask = new RobotTask(taskId, Command.MOVE, taskLocation);
        worker.addTaskToQueue(moveTask);

        RobotTask blueprintTask;
        if (getCommand() == Command.CONSTRUCT_FACTORY) {
            blueprintTask = new RobotTask(taskId, Command.BLUEPRINT_FACTORY, taskLocation);
        } else {
            blueprintTask = new RobotTask(taskId, Command.BLUEPRINT_ROCKET, taskLocation);
        }
        worker.addTaskToQueue(blueprintTask);

        RobotTask buildTask = new RobotTask(taskId, Command.BUILD, taskLocation);
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

        // If the unit is currently wandering, remove the task
        if (unit.hasTasks() && unit.getCurrentTask().getCommand() == Command.WANDER) {
            System.out.println("Unit: " + unit.getId() + " removed its wander task");
            unit.pollCurrentTask();
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
}
