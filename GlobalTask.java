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

    public boolean hasStructureBeenBlueprinted() {
        return hasBlueprinted;
    }

    public void structureHasBeenBlueprinted() {
        hasBlueprinted = true;
    }

    public boolean isStructureBuilt() {
        return isBuilt;
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




    public void addWorkerToList(int workerId) {
        unitsOnTask.add(workerId);
        RobotTask moveTask = new RobotTask(taskId, Command.MOVE, taskLocation);
        Earth.earthWorkerMap.get(workerId).setCurrentTask(moveTask);
    }

    public void removeWorkerFromList(int workerId) {
        if (unitsOnTask.contains(workerId)) {
            unitsOnTask.remove(workerId);
        }
    }

    public void addAttackerToList(int attackerId) {
        unitsOnTask.add(attackerId);
        RobotTask moveTask = new RobotTask(taskId, Command.MOVE, taskLocation);
        Earth.earthAttackerMap.get(attackerId).setCurrentTask(moveTask);
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
            default:
                return false;
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
                    } else {
                        buildOrCloneHelper(unitId);
                    }
                    break;
                case BLUEPRINT_FACTORY:
                    hasBlueprinted = true;
                    removeDuplicateTasks();
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
                    } else {
                        buildOrCloneHelper(unitId);
                    }
                    break;
                case BLUEPRINT_ROCKET:
                    hasBlueprinted = true;
                    removeDuplicateTasks();
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
                    sendRocketRequestHelper(unitId);
                    break;
                case STALL:
                    sendRocketRequestHelper(unitId);
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
        if (Earth.earthWorkerMap.size() < MAX_WORKER_COUNT) {
            nextTask = new RobotTask(this.getTaskId(), Command.CLONE, this.getTaskLocation());
        } else {
            nextTask = new RobotTask(this.getTaskId(), Command.BUILD, this.getTaskLocation());
        }
        Earth.earthWorkerMap.get(unitId).setCurrentTask(nextTask);
    }

    /**
     * Helper method that will control the requests to the rocket and robots for loading the rocket. If the
     * rocket cannot load the robot immediately, it will send a stall request to the unit.
     * @param unitId The id of the unit who finished its task
     */
    private void sendRocketRequestHelper(int unitId) {
        int rocketId = Player.gc.senseUnitAtLocation(this.getTaskLocation()).id();
        if (Earth.earthRocketMap.get(rocketId).loadUnit(unitId)) {
            RobotTask nextTask = new RobotTask(this.getTaskId(), Command.STALL, this.getTaskLocation());
            if (Player.gc.unit(unitId).unitType() == UnitType.Worker) {
                System.out.println("Setting  task to stall unit: " + unitId);
                Earth.earthWorkerMap.get(unitId).setEmergencyTask(nextTask);
            } else {
                System.out.println("Setting  task to stall unit: " + unitId);
                Earth.earthAttackerMap.get(unitId).setEmergencyTask(nextTask);
            }
            return;
        }

        RobotTask nextTask = new RobotTask(this.getTaskId(), Command.STALL, this.getTaskLocation());
        if (Player.gc.unit(unitId).unitType() == UnitType.Worker) {
            System.out.println("Setting emergency task to stall unit: " + unitId);
            Earth.earthWorkerMap.get(unitId).setEmergencyTask(nextTask);
        } else {
            System.out.println("Setting emergency task to stall unit: " + unitId);
            Earth.earthAttackerMap.get(unitId).setEmergencyTask(nextTask);
        }
    }
    /**
     * When the last command in the string of tasks is finished. Remove the task of all the rest of the units.
     * If the task is still the top task of the task queue, remove it
     */
    private void finishedTask() {
        for (int unitId: unitsOnTask) {
            if (Earth.earthWorkerMap.containsKey(unitId)) {
                Earth.earthWorkerMap.get(unitId).removeTask();
            } else {
                Earth.earthAttackerMap.get(unitId).removeTask();
            }
        }

        if (Earth.earthTaskQueue.size() != 0 && Earth.earthTaskQueue.peek().getTaskId() == this.getTaskId()) {
            Earth.earthTaskQueue.poll();
        }
    }

    /**
     * Helper method that will ensure that once the blueprint is created that any duplicate blueprint tasks
     * are removed and the robots are given a new task
     */
    private void removeDuplicateTasks() {
        for (int unitId: unitsOnTask) {
            UnitInstance unit = Earth.earthWorkerMap.get(unitId);
            if (unit.getCurrentTask().getCommand() == Command.BLUEPRINT_FACTORY ||
                    unit.getCurrentTask().getCommand() == Command.BLUEPRINT_ROCKET) {
                buildOrCloneHelper(unitId);
            }
        }
    }
}
