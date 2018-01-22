import bc.MapLocation;

public class RobotTask {

    private int taskId;
    private Command command;
    private MapLocation commandLocation;

    public RobotTask(int taskId, Command command, MapLocation commandLocation) {
        this.taskId = taskId;
        this.command = command;
        this.commandLocation = commandLocation;
    }

    public int getTaskId() {
        return taskId;
    }

    public Command getCommand() {
        return command;
    }

    public MapLocation getCommandLocation() {
        return commandLocation;
    }

    public void setCommandLocation(MapLocation commandLocation) {
        this.commandLocation = commandLocation;
    }
}
