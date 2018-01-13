package units.robots;
import bc.MapLocation;
import commandsAndRequests.Command;
import commandsAndRequests.RobotTask;
import units.Robot;

public class Worker extends Robot {

    public Worker(int id) {
        super(id);
    }

    @Override
    public void run() {

        System.out.println("Got to run method! Task queue size: " + this.robotTaskQueue.size());
        if (this.emergencyTask != null) {
            if (executeTask(this.emergencyTask)) {
                this.emergencyTask = null;
            }
            return;
        }

        if (this.robotTaskQueue.size() != 0) {
            System.out.println("Successfully ran robot: " + this.id + "'s run() method!");
            RobotTask currentTask = this.robotTaskQueue.peek();
            if (executeTask(currentTask)) {
                this.robotTaskQueue.poll();
            }

        } else {
            // Mine for karbonite
        }

    }

    private boolean executeTask(RobotTask robotTask) {
        Command robotCommand = robotTask.getCommand();
        MapLocation commandLocation = robotTask.getCommandLocation();
        switch (robotCommand) {
            case MOVE:
                System.out.println("Running MOVE!");
                return move(this.id, commandLocation);
            case BUILD:
                //return build(commandLocation);
            case CLONE:
                //return clone();
            case BLUEPRINT_FACTORY:
                //return blueprintFactory(commandLocation);
            case BLUEPRINT_ROCKET:
                //return blueprintRocket(commandLocation);
            default:
                return mineKarbonite(commandLocation);
        }
    }

    private boolean mineKarbonite(MapLocation commandLocation) {
        return true;
    }

//
//    /**
//     * Method that will need to clone an idle worker and will remove it from the idle worker HashMap. Add the
//     * new robot to the staging area
//     */
//    public boolean cloneWorker() {
//        Direction direction = Player.returnAvailableDirection(this.id);
//        if (direction == null) {
//            System.out.println("No directions available to clone");
//            return false;
//
//        } else if (Player.gameController.canReplicate(this.id, direction)) {
//            Player.gameController.replicate(this.id, direction);
//            MapLocation clonedWorkerLocation = Player.gameController.unit(this.id).location().mapLocation().add(direction);
//            Unit clonedWorker = Player.gameController.senseUnitAtLocation(clonedWorkerLocation);
//            Worker clonedWorkerInstance = new Worker(clonedWorker.id(), null);
//            Earth.earthStagingWorkerHashMap.put(clonedWorker.id(), clonedWorkerInstance);
//
//            System.out.println("Successfully cloned!");
//            return true;
//
//        } else {
//            System.out.println("Could not replicate. Karbonite left: " + Player.gameController.karbonite());
//            return false;
//        }
//    }
//
//    /**
//     * Method will find the optimal location to build a factory and will check if the player has enough
//     * Karbonite. If all conditions are met, the worker will lay down a blueprint and will start to build the factory.
//     * If the conditions are not met, the blueprintFactory command will be moved to next round and the robot will remain
//     * in the idle robot HashMap.
//     */
//    public boolean blueprintFactory() {
//        Direction direction = Player.returnAvailableDirection(this.id);
//        if (direction == null) {
//            System.out.println("No directions available to build factory");
//            return false;
//
//        } else if (Player.gameController.canBlueprint(this.id, UnitType.Factory, direction)) {
//            Player.gameController.blueprint(this.id, UnitType.Factory, direction);
//            MapLocation factoryBlueprintLocation = Player.gameController.unit(this.id).location().mapLocation().add(direction);
//            Unit factory = Player.gameController.senseUnitAtLocation(factoryBlueprintLocation);
//
//            this.targetId = factory.id();
//            this.command = Command.BUILD;
//
//            System.out.println("Successfully built blueprint... Building!");
//            return true;
//
//        } else {
//            System.out.println("Could not build factory. Karbonite left: " + Player.gameController.karbonite());
//            return false;
//        }
//    }
//
//    /**
//     * Method will find the optimal location to build a rocket and will check if the player has enough
//     * Karbonite. If all conditions are met, the worker will lay down a blueprint and will start to build the rocket.
//     * If the conditions are not met, the buildRocket command will be moved to next round and the robot will remain
//     * in the idle robot HashMap.
//     */
//    public boolean buildRocket() {
//        Direction direction = Player.returnAvailableDirection(this.id);
//        if (direction == null) {
//            System.out.println("No directions available to build rocket");
//            return false;
//
//        } else if (Player.gameController.canBlueprint(this.id, UnitType.Rocket, direction)) {
//            Player.gameController.blueprint(this.id, UnitType.Rocket, direction);
//            MapLocation rocketBlueprintLocation = Player.gameController.unit(this.id).location().mapLocation().add(direction);
//            Unit rocket = Player.gameController.senseUnitAtLocation(rocketBlueprintLocation);
//
//            this.targetId = rocket.id();
//            this.command = Command.BUILD;
//
//            System.out.println("Successfully built blueprint... Building!");
//            return true;
//
//        } else {
//            System.out.println("Could not build rocket. Karbonite left: " + Player.gameController.karbonite());
//            return false;
//        }
//    }
}

