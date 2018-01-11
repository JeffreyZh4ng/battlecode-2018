import bc.Direction;
import bc.MapLocation;
import bc.Unit;
import bc.UnitType;

public class Worker extends Robot {

    private int id;
    private int targetId;
    private Command command;

    public Worker(int id, Command command) {
        super(id);
        this.targetId = 0;
        this.command = command;
    }

    @Override
    public boolean addTaskToQueue(Task task) {
        return false;
    }

    /* public boolean executeTask() {
        switch (command) {
            case BUILD:
                if (Player.gameController.canBuild(this.id, this.targetId)) {
                    Player.gameController.build(this.id, this.targetId);
                    if (Player.gameController.unit(targetId).health() == 200) {
                        this.command = null;
                        System.out.println("Finished building blueprint!");
                        return true;
                    } else {
                        return false;
                    }

                } else {
                    System.out.println("Idk WTF is going on!");
                    return true;
                }

            case MINE_KARBONITE:
                return true;

            default:
                return true;
        }
    }*/

    /**
     * Method that will need to clone an idle worker and will remove it from the idle worker HashMap. Add the
     * new robot to the staging area
     */
    /*public boolean cloneWorker() {
        Direction direction = Player.returnAvailableDirection(this.id);
        if (direction == null) {
            System.out.println("No directions available to clone");
            return false;

        } else if (Player.gameController.canReplicate(this.id, direction)) {
            Player.gameController.replicate(this.id, direction);
            MapLocation clonedWorkerLocation = Player.gameController.unit(this.id).location().mapLocation().add(direction);
            Unit clonedWorker = Player.gameController.senseUnitAtLocation(clonedWorkerLocation);
            Worker clonedWorkerInstance = new Worker(clonedWorker.id(), null);
            Earth.earthStagingWorkerHashMap.put(clonedWorker.id(), clonedWorkerInstance);

            System.out.println("Successfully cloned!");
            return true;

        } else {
            System.out.println("Could not replicate. Karbonite left: " + Player.gameController.karbonite());
            return false;
        }
    }*/

    /**
     * Method will find the optimal location to build a factory and will check if the player has enough
     * Karbonite. If all conditions are met, the worker will lay down a blueprint and will start to build the factory.
     * If the conditions are not met, the blueprintFactory command will be moved to next round and the robot will remain
     * in the idle robot HashMap.
     */
    /*public boolean blueprintFactory() {
        Direction direction = Player.returnAvailableDirection(this.id);
        if (direction == null) {
            System.out.println("No directions available to build factory");
            return false;

        } else if (Player.gameController.canBlueprint(this.id, UnitType.Factory, direction)) {
            Player.gameController.blueprint(this.id, UnitType.Factory, direction);
            MapLocation factoryBlueprintLocation = Player.gameController.unit(this.id).location().mapLocation().add(direction);
            Unit factory = Player.gameController.senseUnitAtLocation(factoryBlueprintLocation);

            this.targetId = factory.id();
            this.command = Command.BUILD;

            System.out.println("Successfully built blueprint... Building!");
            return true;

        } else {
            System.out.println("Could not build factory. Karbonite left: " + Player.gameController.karbonite());
            return false;
        }
    }*/

    /**
     * Method will find the optimal location to build a rocket and will check if the player has enough
     * Karbonite. If all conditions are met, the worker will lay down a blueprint and will start to build the rocket.
     * If the conditions are not met, the buildRocket command will be moved to next round and the robot will remain
     * in the idle robot HashMap.
     */
    /*public boolean buildRocket() {
        Direction direction = Player.returnAvailableDirection(this.id);
        if (direction == null) {
            System.out.println("No directions available to build rocket");
            return false;

        } else if (Player.gameController.canBlueprint(this.id, UnitType.Rocket, direction)) {
            Player.gameController.blueprint(this.id, UnitType.Rocket, direction);
            MapLocation rocketBlueprintLocation = Player.gameController.unit(this.id).location().mapLocation().add(direction);
            Unit rocket = Player.gameController.senseUnitAtLocation(rocketBlueprintLocation);

            this.targetId = rocket.id();
            this.command = Command.BUILD;

            System.out.println("Successfully built blueprint... Building!");
            return true;

        } else {
            System.out.println("Could not build rocket. Karbonite left: " + Player.gameController.karbonite());
            return false;
        }
    }*/
}
