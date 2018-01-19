import bc.*;

import java.util.Map;

public class Worker extends Robot {

    private static final int DIRECTION_MAX_VAL = 8;
    private RobotTask emergencyTask = null;

    public Worker(int id) {
        super(id);
    }

    @Override
    public void run() {

        if (emergencyTask != null) {
            if (executeTask(emergencyTask)) {
                System.out.println("Unit: " + this.getId() + "Finished emergency task!");
                emergencyTask = null;
            }

        } else if (!this.isIdle()) {
            if (executeTask(this.getCurrentTask())) {
                GlobalTask globalTask = Earth.earthTaskMap.get(this.getCurrentTask().getTaskId());
                System.out.println("Unit: " + this.getId() + " has finished task: " + this.getCurrentTask().getCommand());
                globalTask.finishedTask(this.getId(), this.getCurrentTask().getCommand());

                // Perform run again?
                run();
            }

        } else {
            System.out.println("Unit: " + this.getId() + " wandering!");
            wander();
        }

        mineKarbonite();
    }

    /**
     * Executes the task from the task queue
     *
     * @param robotTask The task the robot has to complete
     * @return If the task was completed or not
     */
    private boolean executeTask(RobotTask robotTask) {
        Command robotCommand = robotTask.getCommand();
        MapLocation commandLocation = robotTask.getCommandLocation();
        System.out.println("Unit: " + this.getId() + " " + robotCommand);

        switch (robotCommand) {
            case MOVE:
                return this.move(this.getId(), commandLocation);
            case CLONE:
                return cloneWorker(commandLocation);
            case BUILD:
                return buildStructure(commandLocation);
            case BLUEPRINT_FACTORY:
                return blueprintStructure(commandLocation, UnitType.Factory);
            case BLUEPRINT_ROCKET:
                return blueprintStructure(commandLocation, UnitType.Rocket);
            default:
                System.out.println("This should never happen unless I forgot something so returning true");
                return true;
        }
    }

    /**
     * Given a MapLocation, see if you can clone a worker and put it at that spot
     *
     * @param commandLocation The MapLocation of the new worker
     * @return If the worker was cloned or not
     */
    private boolean cloneWorker(MapLocation commandLocation) {
        MapLocation robotCurrentLocation = Player.gc.unit(this.getId()).location().mapLocation();

        for (int i = 0; i < DIRECTION_MAX_VAL; i++) {
            Direction direction = Direction.swigToEnum(i);
            MapLocation newLocation = robotCurrentLocation.add(direction);

            if (commandLocation.isAdjacentTo(newLocation)) {

                Direction directionToClone = robotCurrentLocation.directionTo(newLocation);
                if (Player.gc.canReplicate(this.getId(), directionToClone)) {
                    Player.gc.replicate(this.getId(), directionToClone);

                    int clonedWorkerId = Player.senseUnitAtLocation(newLocation).id();
                    UnitInstance newWorker = new Worker(clonedWorkerId);

                    Earth.earthStagingWorkerMap.put(clonedWorkerId, newWorker);

                    System.out.println("Unit: " + this.getId() + " Cloned worker!");
                    System.out.println("New worker has ID of: " + clonedWorkerId);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Given a MapLocation of a blueprint, build it until it reaches full health and becomes a rocket/factory
     * @param commandLocation The location of the unfinished structure
     * @return If the structure finished building
     */
    private boolean buildStructure(MapLocation commandLocation) {
        int structureId = Player.senseUnitAtLocation(commandLocation).id();

        if (Player.gc.canBuild(this.getId(), structureId)) {
            Player.gc.build(this.getId(), structureId);
            System.out.println("Unit: " + this.getId() + " ran build()");

            if (Player.gc.unit(structureId).structureIsBuilt() > 0) {

                UnitType unitType = Player.gc.unit(structureId).unitType();
                if (unitType == UnitType.Factory) {
                    UnitInstance factory = Earth.earthFactoryMap.get(structureId);
                    UnitInstance builtFactory = new Factory(factory.getId(), true, commandLocation);
                    Earth.earthFactoryMap.put(factory.getId(), builtFactory);
                } else {
                    UnitInstance rocket = Earth.earthRocketMap.get(structureId);
                    UnitInstance builtRocket = new Rocket(rocket.getId(), true, commandLocation);
                    Earth.earthFactoryMap.put(rocket.getId(), builtRocket);
                }

                System.out.println("Unit: " + this.getId() + " Built structure!");
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
        MapLocation robotCurrentLocation = Player.gc.unit(this.getId()).location().mapLocation();
        Direction directionToBlueprint = robotCurrentLocation.directionTo(commandLocation);

        if (Player.gc.canBlueprint(this.getId(), unitType, directionToBlueprint)) {
            Player.gc.blueprint(this.getId(), unitType, directionToBlueprint);

            int structureId = Player.gc.senseUnitAtLocation(commandLocation).id();
            UnitInstance newStructure;
            if (unitType == UnitType.Factory) {
                newStructure = new Factory(structureId, false, commandLocation);
                Earth.earthFactoryMap.put(structureId, newStructure);
            } else {
                newStructure = new Rocket(structureId, false, commandLocation);
                Earth.earthRocketMap.put(structureId, newStructure);
            }

            System.out.println("Unit: " + this.getId() + " Blueprinted structure!");
            Earth.planedStructureLocations.remove(commandLocation.toString());

            return true;
        }

        return false;
    }

    /**
     * Make worker wander to a random location within its vision radius
     */
    private void wander() {
        MapLocation currentLocation = Player.gc.unit(this.getId()).location().mapLocation();
        VecMapLocation locations = Player.gc.allLocationsWithin(currentLocation, 50);
        int randomLocation = (int) (Math.random() * locations.size());

        MapLocation wanderLocation = locations.get(randomLocation);
        emergencyTask = new RobotTask(-1, Command.MOVE, wanderLocation);
    }

//    /**
//     * Method that will run when the worker has no tasks left. The worker will wander around and will mine karbonite
//     */
//    private void wanderAndMineKarbonite() {
//        for (int i = 0; i < DIRECTION_MAX_VAL + 1; i++) {
//            Direction direction = Direction.swigToEnum(i);
//            MapLocation newLocation = Player.gc.unit(this.getId()).location().mapLocation().add(direction);
//            if (Player.gc.canHarvest(this.getId(), direction) && Player.gc.karboniteAt(newLocation) >= 3) {
//
//                if (this.getEmergencyTask() == null) {
//                    RobotTask newTask = new RobotTask(-1, -1, Command.MOVE, newLocation);
//                    System.out.println("Robot: " + this.getId() + " WANDERING!");
//                    this.setEmergencyTask(newTask);
//                }
//            }
//        }
//    }

//    /**
//     * Finds the nearest known of karbonite location
//     *
//     * @return karbonite location or null if none known of
//     */
//    private MapLocation getNearestKarboniteLocation() {
//        MapLocation nearestLocation = null;
//        MapLocation myLocation = Player.gc.unit(this.getId()).location().mapLocation();
//        //check all locations
//        for (int x = 0; x < Player.gc.startingMap(Player.gc.planet()).getWidth(); x++) {
//            for (int y = 0; y < Player.gc.startingMap(Player.gc.planet()).getHeight(); y++) {
//                MapLocation locationToTest = new MapLocation(Player.gc.planet(), x, y);
//                if (Player.gc.canSenseLocation(locationToTest) && Player.gc.karboniteAt(locationToTest) > 0) {
//                    if (nearestLocation == null) {
//                        nearestLocation = locationToTest;
//                    } else if (myLocation.distanceSquaredTo(locationToTest) < myLocation.distanceSquaredTo(nearestLocation)) {
//                        nearestLocation = locationToTest;
//                    }
//                }
//            }
//        }
//        return nearestLocation;
//    }

    /**
     * Method that will check if a worker can mine karbonite. If it has not performed an action this turn and
     * there is a karbonite pocket in adjacent squares, it will mine it
     */
    private void mineKarbonite() {
        for (int i = 0; i < DIRECTION_MAX_VAL + 1; i++) {
            Direction direction = Direction.swigToEnum(i);
            MapLocation newLocation = Player.gc.unit(this.getId()).location().mapLocation().add(direction);
            if (Player.gc.canHarvest(this.getId(), direction) && Player.karboniteAt(newLocation) > 0) {
                Player.gc.harvest(this.getId(), direction);
                break;
            }
        }
    }
}

