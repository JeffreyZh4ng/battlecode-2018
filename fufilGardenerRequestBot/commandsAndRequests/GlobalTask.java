package commandsAndRequests;
import bc.MapLocation;

import java.util.ArrayList;

public class GlobalTask {

    public static int taskIndex = 0;

    private int taskId;
    private int completionStage;
    private boolean isRequestFulfilled;
    private Command command;
    private ArrayList<Integer> workersOnTask;
    private MapLocation taskLocation;

    public GlobalTask(Command command, MapLocation taskLocation) {
        taskIndex++;
        this.taskId = taskIndex;
        this.completionStage = 1;
        this.isRequestFulfilled = false;
        this.command = command;
        this.workersOnTask = new ArrayList<>();
        this.taskLocation = taskLocation;
    }

    public void addWorkerToList(int workerId) {
        this.workersOnTask.add(workerId);
    }

    public int getTaskId() {
        return taskId;
    }

    public int getCompletionStage() {
        return completionStage;
    }

    public Command getCommand() {
        return command;
    }

    public MapLocation getTaskLocation() {
        return taskLocation;
    }
}
