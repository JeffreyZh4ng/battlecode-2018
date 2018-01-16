import bc.MapLocation;

public class RobotTask {

    private int taskId;
    private int completionStage;
    private Command command;
    private MapLocation commandLocation;

    public RobotTask(int taskId, int completionStage, Command command, MapLocation commandLocation) {
        this.taskId = taskId;
        this.completionStage = completionStage;
        this.command = command;
        this.commandLocation = commandLocation;
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

    public MapLocation getCommandLocation() {
        return commandLocation;
    }
}
