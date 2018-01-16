import bc.MapLocation;

import java.util.ArrayList;
import java.util.HashSet;

public class GlobalTask {

    public static int taskIndex = 0;

    private int taskId;
    private int completionStage;
    private boolean isCreated;
    private Command command;
    private HashSet<Integer> workersOnTask;
    private MapLocation taskLocation;

    public GlobalTask(Command command, MapLocation taskLocation) {
        taskIndex++;
        this.taskId = taskIndex;
        this.completionStage = 1;
        this.command = command;
        this.workersOnTask = new HashSet<>();
        this.taskLocation = taskLocation;
    }

    public int getTaskId() {
        return taskId;
    }

    public int getCompletionStage() {
        return completionStage;
    }

    public void incrementCompletionStage() {
        completionStage++;
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
    }

    public void removeWorkerFromList(int workerId) {
        if (workersOnTask.contains(workerId)) {
            workersOnTask.remove(workerId);
        }
    }
}
