import bc.MapLocation;

import java.util.ArrayList;
import java.util.HashSet;

public class GlobalTask {

    public static int taskIndex = 0;

    private int taskId;
    private int minimumWorkers;
    private boolean hasBlueprinted;
    private Command command;
    private HashSet<Integer> workersOnTask;
    private MapLocation taskLocation;

    public GlobalTask(int minimumWorkers, Command command, MapLocation taskLocation) {
        taskIndex++;
        this.taskId = taskIndex;
        this.minimumWorkers = minimumWorkers;
        this.hasBlueprinted = false;
        this.command = command;
        this.workersOnTask = new HashSet<>();
        this.taskLocation = taskLocation;
    }

    public int getTaskId() {
        return taskId;
    }

    public int getMinimumWorkers() {
        return minimumWorkers;
    }

    public Command getCommand() {
        return command;
    }

    public HashSet<Integer> getWorkersOnTask() {
        return workersOnTask;
    }

    public MapLocation getTaskLocation() {
        return taskLocation;
    }

    public void addWorkerToList(int workerId) {
        workersOnTask.add(workerId);
        RobotTask moveTask = new RobotTask(taskId, Command.MOVE, taskLocation);
        Earth.earthWorkerMap.get(workerId).setCurrentTask(moveTask);
    }

    public void removeWorkerFromList(int workerId) {
        if (workersOnTask.contains(workerId)) {
            workersOnTask.remove(workerId);
        }
    }

    /**
     * When the unit has finished its task, call this method to determine what to do next
     * @param unitId The id of the unit who finished its task
     * @param command The command that it just finished
     */
    public void finishedTask(int unitId, Command command) {
        if (this.command == Command.CONSTRUCT_FACTORY) {
            switch (command) {

                case MOVE:
                    if (!hasBlueprinted) {
                        RobotTask nextTask = new RobotTask(this.getTaskId(), Command.BLUEPRINT_FACTORY, this.getTaskLocation());
                        Earth.earthWorkerMap.get(unitId).setCurrentTask(nextTask);

                        // Taking a risk here. We assume that if a worker is given a blueprint to make, it will
                        // Make it 100% of the time
                        hasBlueprinted = true;
                    } else {
                        buildOrCloneHelper(unitId);
                    }
                    break;
                case BLUEPRINT_FACTORY:
                    buildOrCloneHelper(unitId);
                    break;
                case CLONE:
                    RobotTask nextTask = new RobotTask(this.getTaskId(), Command.BUILD, this.getTaskLocation());
                    Earth.earthWorkerMap.get(unitId).setCurrentTask(nextTask);
                    break;
                case BUILD:
                    finishedTask();
                    break;
            }

        } else if (this.command == Command.CONSTRUCT_ROCKET) {
            switch (command) {

                case MOVE:
                    if (!hasBlueprinted) {
                        RobotTask nextTask = new RobotTask(this.getTaskId(), Command.BLUEPRINT_ROCKET, this.getTaskLocation());
                        Earth.earthWorkerMap.get(unitId).setCurrentTask(nextTask);

                        // Taking a risk here. We assume that if a worker is given a blueprint to make, it will
                        // Make it 100% of the time
                        hasBlueprinted = true;
                    } else {
                        buildOrCloneHelper(unitId);
                    }
                    break;
                case BLUEPRINT_ROCKET:
                    buildOrCloneHelper(unitId);
                    break;
                case CLONE:
                    RobotTask nextTask = new RobotTask(this.getTaskId(), Command.BUILD, this.getTaskLocation());
                    Earth.earthWorkerMap.get(unitId).setCurrentTask(nextTask);
                    break;
                case BUILD:
                    finishedTask();
                    break;
            }

        } else if (this.command == Command.LOAD_ROCKET) {
            switch (command) {
                case MOVE:
                    RobotTask nextTask = new RobotTask(this.getTaskId(), Command.STALL, this.getTaskLocation());

                    if (Earth.earthWorkerMap.containsKey(unitId)) {
                        Earth.earthAttackerMap.get(unitId).setCurrentTask(nextTask);
                    } else {
                        Earth.earthAttackerMap.get(unitId).setCurrentTask(nextTask);
                    }
                    // Send request to rocket to load unit.
                    break;
                case LOAD_ROCKET:
                    // Will send a request to the rocket to load this unit at its location
                    break;
            }
        }
    }

    /**
     * Helper method that will decide whether the next robot will build or clone as the next task
     * @param unitId The id of the robot who's next task you want to determine
     */
    private void buildOrCloneHelper(int unitId) {
        RobotTask nextTask;
        if (Earth.earthWorkerMap.size() < 40) {
            nextTask = new RobotTask(this.getTaskId(), Command.CLONE, this.getTaskLocation());
        } else {
            nextTask = new RobotTask(this.getTaskId(), Command.BUILD, this.getTaskLocation());
        }
        Earth.earthWorkerMap.get(unitId).setCurrentTask(nextTask);
    }

    /**
     * When the last command in the string of tasks is finished. Remove the task of all the rest of the units
     */
    private void finishedTask() {
        for (int unitId: workersOnTask) {
            if (Earth.earthWorkerMap.containsKey(unitId)) {
                Earth.earthWorkerMap.get(unitId).removeTask();
            } else {
                Earth.earthAttackerMap.get(unitId).removeTask();
            }
        }
    }
}
