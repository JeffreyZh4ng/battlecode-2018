import bc.Direction;
import bc.MapLocation;
import bc.UnitType;

public class Worker extends Robot {

    public Worker(int id) {
        super(id);
    }

    @Override
    public void run() {

        // TODO: Make it so that workers are still able to mine karbonite/build after they move
        if (this.emergencyTask != null) {
            if (executeTask(this.emergencyTask)) {
                this.emergencyTask = null;
            }
            return;
        }

        if (this.robotTaskQueue.size() != 0) {
            RobotTask currentTask = this.robotTaskQueue.peek();
            if (executeTask(currentTask)) {
                this.robotTaskQueue.poll();
            }

        }
            // Mine for karbonite

    }

    /**
     * Executes the task from the task queue
     * @param robotTask The task the robot has to complete
     * @return If the task was completed or not
     */
    private boolean executeTask(RobotTask robotTask) {
        Command robotCommand = robotTask.getCommand();
        MapLocation commandLocation = robotTask.getCommandLocation();

        switch (robotCommand) {
            case MOVE:
                if (Player.gc.unit(this.id).movementHeat() < 10) {

                    // TODO: After the worker has moved, it can still perform actions. EX: It can mine karbonite every turn it moves
                    return move(this.id, commandLocation);
                } else {
                    System.out.println("Waiting for heat to be less than 10!");
                    return false;
                }

            case BUILD:
                return buildBlueprint(commandLocation);
            case CLONE:
                return cloneWorker(commandLocation);
            case BLUEPRINT_FACTORY:
                return blueprintStructure(commandLocation, UnitType.Factory);
            case BLUEPRINT_ROCKET:
                return blueprintStructure(commandLocation, UnitType.Rocket);
            default:
                return mineKarbonite(commandLocation);
        }
    }

    /**
     * Given a MapLocation, see if you can clone a worker and put it at that spot
     * @param commandLocation The MapLocation of the new worker
     * @return If the worker was cloned or not
     */
    private boolean cloneWorker(MapLocation commandLocation) {
        MapLocation robotCurrentLocation = Player.gc.unit(this.id).location().mapLocation();
        Direction directionToClone = robotCurrentLocation.directionTo(commandLocation);

        if (Player.gc.canReplicate(this.id, directionToClone)) {
            Player.gc.replicate(this.id, directionToClone);

            int clonedWorkerId = Player.gc.senseUnitAtLocation(commandLocation).id();
            Unit newWorker = new Worker(clonedWorkerId);

            //TODO: Don't know if this will break. Need to find out if a worker can move/act the round it was created.
            // yea something broke
//[earth:red] Exception in thread "main" java.lang.RuntimeException: No object returned, check whether it exists first.
//[earth:red] 	at bc.bcJNI.GameController_senseUnitAtLocation(Native Method)
//[earth:red] 	at bc.GameController.senseUnitAtLocation(GameController.java:124)
//[earth:red] 	at units.robots.Worker.cloneWorker(Worker.java:89)
//[earth:red] 	at units.robots.Worker.executeTask(Worker.java:67)
//[earth:red] 	at units.robots.Worker.run(Worker.java:34)
//[earth:red] 	at planets.Earth.runUnitMap(Earth.java:41)
//[earth:red] 	at planets.Earth.execute(Earth.java:29)
//[earth:red] 	at Player.main(Player.java:27)

            Earth.earthWorkerMap.put(clonedWorkerId, newWorker);
            return true;
        }

        return false;
    }

    /**
     * Given a MapLocation of a blueprint, build it until it reaches full health and becomes a rocket/factory
     * @param commandLocation The MapLocation of the blueprint
     * @return If the blueprint has reached full health
     */
    private boolean buildBlueprint(MapLocation commandLocation) {
        int blueprintId = Player.gc.senseUnitAtLocation(commandLocation).id();

        // Fucking MIT spaghetti code. Why does a boolean named method not return a boolean...
        // Check if one of the workers earlier in the list finished building it before you.
        // TODO: Make it so that when the building is complete, pop it from the global tasks and from any worker who has the task
        if (Player.gc.unit(blueprintId).structureIsBuilt() > 0) {
            return true;
        }

        if (Player.gc.canBuild(this.id, blueprintId)) {
            Player.gc.build(this.id, blueprintId);

            // If this robot finished building the structure, remove it from the blueprint list and add it rockets/factories
            // TODO: Combine the blueprint and factory/rocket classes into one. Create a boolean isBuilt in Structure to help
            if (Player.gc.unit(blueprintId).structureIsBuilt() > 0) {

                Earth.earthBlueprintMap.remove(blueprintId);
                UnitType blueprintType = Player.gc.unit(blueprintId).unitType();

                if (blueprintType == UnitType.Factory) {
                    Unit newFactory = new Factory(blueprintId, commandLocation);
                    Earth.earthFactoryMap.put(blueprintId, newFactory);

                } else if (blueprintType == UnitType.Rocket) {
                    Unit newRocket = new Rocket(blueprintId, commandLocation);
                    Earth.earthRocketMap.put(blueprintId, newRocket);
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Given the map location of a blueprint you want to build, check if you can build it and add
     * a new blueprint instance to the blueprint map
     * @param commandLocation The MapLocation of the blueprint you want to build
     * @param unitType Either a factory or rocket blueprint
     * @return If the blueprint was built or not
     */
    private boolean blueprintStructure(MapLocation commandLocation, UnitType unitType) {
        MapLocation robotCurrentLocation = Player.gc.unit(this.id).location().mapLocation();
        Direction directionToBlueprint = robotCurrentLocation.directionTo(commandLocation);

        if (Player.gc.canBlueprint(this.id, unitType, directionToBlueprint)) {
            Player.gc.blueprint(this.id, unitType, directionToBlueprint);

            int structureId = Player.gc.senseUnitAtLocation(commandLocation).id();
            Unit newStructure = new Blueprint(structureId, commandLocation);
            Earth.earthBlueprintMap.put(structureId, newStructure);

            return true;
        }

        return false;
    }

    private boolean mineKarbonite(MapLocation commandLocation) {
        return true;
    }
}
