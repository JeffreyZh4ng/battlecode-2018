package commandsAndRequests;
import bc.MapLocation;

import java.util.ArrayList;

public class GlobalTask {

    public static int taskIndex = 0;

    private int taskId;
    private boolean isRequestFulfilled;
    private Command command;
    private ArrayList<Integer> workersOnTaskList;
    private MapLocation taskLocation;

    public GlobalTask(Command command, MapLocation taskLocation) {
        taskIndex++;
        this.isRequestFulfilled = false;
        this.taskId = taskIndex;
        this.command = command;
        workersOnTaskList = new ArrayList<>();
        this.taskLocation = taskLocation;
    }

    public void addWorkerToList(int workerId) {
        this.workersOnTaskList.add(workerId);
    }

}
