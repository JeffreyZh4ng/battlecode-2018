package commandsAndRequests;

import bc.MapLocation;

public class RobotTask {

    private Command command;
    private MapLocation commandLocation;

    public RobotTask(Command command, MapLocation commandLocation) {
        this.command = command;
        this.commandLocation = commandLocation;
    }

    public Command getCommand() {
        return command;
    }

    public MapLocation getCommandLocation() {
        return commandLocation;
    }
}
