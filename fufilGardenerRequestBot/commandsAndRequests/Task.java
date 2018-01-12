package commandsAndRequests;
import bc.MapLocation;

import java.util.ArrayList;

public class Task {

    public static int taskIndex = 0;

    private int taskId;
    private Command command;
    private ArrayList<Integer> workersOnTaskList;
    private MapLocation taskLocation;

    public Task(Task task, Command command, MapLocation taskLocation) {
        taskIndex++;
        this.taskId = taskIndex;
        this.command = command;
        workersOnTaskList = new ArrayList<>();
        this.taskLocation = taskLocation;
    }

    public void addWorkerToList(int workerId) {
        this.workersOnTaskList.add(workerId);
    }

}
