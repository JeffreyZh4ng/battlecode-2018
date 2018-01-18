import bc.Direction;
import bc.MapLocation;
import bc.Planet;
import bc.UnitType;
import bc.VecMapLocation;

import java.util.Map;

public class Worker extends Robot {

    private static final int MOVEMENT_HEAT_LIMIT = 10;
    private static final int MIN_WORKER_LIST_SIZE = 4;
    private static final int DIRECTION_MAX_VAL = 8;

    public Worker(int id) {
        super(id);
    }

    @Override
    public void run() {

        senseArea(this.getId(), 50);

        if (this.getRobotTaskQueue().size() != 0) {

            System.out.println("Tasks in queue: " + this.getRobotTaskQueue().size() + " for robot: " + this.getId());
            manageCurrentRobotTask();
        }

        if (this.getEmergencyTask() != null) {
            if (executeTask(this.getEmergencyTask())) {
                this.setEmergencyTask(null);
            }

        } else if (this.getRobotTaskQueue().size() != 0) {
            RobotTask currentTask = this.getRobotTaskQueue().peek();
            System.out.println("Robot: " + this.getId() + " Current command: " + currentTask.getCommand());

            if (executeTask(currentTask)) {
                this.pollTask();
                GlobalTask globalTask = Earth.earthTaskMap.get(currentTask.getTaskId());
                globalTask.incrementCompletionStage();
                System.out.println("Global task stage: " + globalTask.getCompletionStage());
            }

        } else {
            wander();
            // wanderAndMineKarbonite();
        }

        mineKarbonite();

    }

    /**
     * Method that manages what task the worker should be on and other nuances such as if it should set a
     * priority task based on different parameters
     */
    private void manageCurrentRobotTask() {
        GlobalTask globalTask = Earth.earthTaskMap.get(this.getTopTask().getTaskId());

        if (globalTask.getCompletionStage() > 3) {
            while (this.getRobotTaskQueue().size() > 0 && this.getTopTask().getTaskId() == globalTask.getTaskId()) {
                System.out.println("Robot: " + this.getId() + " Removing all the rest of the tasks with the same ID!");
                this.removeTask();
            }
            System.out.println("Tasks in queue after removing: " + this.getRobotTaskQueue().size() + " for robot: " + this.getId());
            return;
        }

        int completionStage = this.getTopTask().getCompletionStage();
        while (completionStage < globalTask.getCompletionStage() && completionStage < 3) {
            this.removeTask();
            completionStage = this.getTopTask().getCompletionStage();
        }

        // Need to reset the completion stage if tasks have been popped.
        completionStage = this.getTopTask().getCompletionStage();

        // If the task is on the build stage and there are not more than 4 workers in the list, clone
        if (completionStage == 3 && globalTask.getWorkersOnTask().size() < MIN_WORKER_LIST_SIZE && this.getEmergencyTask() == null) {
            if (Player.gc.unit(this.getId()).abilityHeat() < 10) {
                RobotTask emergencyTask = new RobotTask(-1, -1, Command.CLONE, globalTask.getTaskLocation());
                this.setEmergencyTask(emergencyTask);
            }
        }
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
                if (Player.gc.isMoveReady(this.getId())) {
                    System.out.println("Robot: " + this.getId() + " MOVING!");
                    //System.out.println("dmap: "+getDepthMap(Player.gc.unit(this.getId()).location().mapLocation(), commandLocation));
                    return move(this.getId(), commandLocation);

                } else {
                    return false;
                }

            case CLONE:
                return cloneWorker(commandLocation);
            case BUILD:
                return buildStructure(commandLocation);
            case BLUEPRINT_FACTORY:
                return blueprintStructure(commandLocation, UnitType.Factory);
            case BLUEPRINT_ROCKET:
                return blueprintStructure(commandLocation, UnitType.Rocket);
            default:
                return true;
        }
    }

    /**
     * Given a MapLocation, see if you can clone a worker and put it at that spot
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

                    int clonedWorkerId = Player.gc.senseUnitAtLocation(newLocation).id();
                    UnitInstance newWorker = new Worker(clonedWorkerId);

                    System.out.println(this.getTopTask());
                    RobotTask buildTask = new RobotTask(this.getTopTask().getTaskId(), 3, Command.BUILD, commandLocation);
                    newWorker.addTask(buildTask);

                    Earth.earthStagingWorkerMap.put(clonedWorkerId, newWorker);
                    Earth.earthTaskMap.get(this.getTopTask().getTaskId()).addWorkerToList(clonedWorkerId);

                    System.out.println("Robot: " + this.getId() + " Cloned worker!");
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
        int structureId = Player.gc.senseUnitAtLocation(commandLocation).id();

        if (Player.gc.canBuild(this.getId(), structureId)) {
            Player.gc.build(this.getId(), structureId);

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

                System.out.println("Robot: " + this.getId() + " Built structure!");
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

            System.out.println("Robot: " + this.getId() + " Blueprinted structure!");

            //remove from planed locations
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
        int randomLocation = (int)(Math.random()*locations.size());

        MapLocation wanderLocation = locations.get(randomLocation);
        RobotTask wanderTask = new RobotTask(-1, -1, Command.MOVE, wanderLocation);

        this.setEmergencyTask(wanderTask);
    }

    /**
     * Method that will run when the worker has no tasks left. The worker will wander around and will mine karbonite
     */
    private void wanderAndMineKarbonite() {
        for (int i = 0; i < DIRECTION_MAX_VAL + 1; i++) {
            Direction direction = Direction.swigToEnum(i);
            MapLocation newLocation = Player.gc.unit(this.getId()).location().mapLocation().add(direction);
            if (Player.gc.canHarvest(this.getId(), direction) && Player.gc.karboniteAt(newLocation) >= 3) {

                if (this.getEmergencyTask() == null) {
                    RobotTask newTask = new RobotTask(-1, -1, Command.MOVE, newLocation);
                    System.out.println("Robot: " + this.getId() + " WANDERING!");
                    this.setEmergencyTask(newTask);
                }
            }
        }
    }

    /**
     * Finds the nearest known of karbonite location
     * @return karbonite location or null if none known of
     */
    private MapLocation getNearestKarboniteLocation() {
        MapLocation nearestLocation = null;
        MapLocation myLocation = Player.gc.unit(this.getId()).location().mapLocation();
        //check all locations
        for (int x = 0; x < Player.gc.startingMap(Player.gc.planet()).getWidth(); x++) {
            for (int y = 0; y < Player.gc.startingMap(Player.gc.planet()).getHeight(); y++) {
                MapLocation locationToTest = new MapLocation(Player.gc.planet(),x,y);
                if (Player.gc.canSenseLocation(locationToTest) && Player.gc.karboniteAt(locationToTest)>0) {
                    if (nearestLocation== null) {
                        nearestLocation = locationToTest;
                    } else if (myLocation.distanceSquaredTo(locationToTest)<myLocation.distanceSquaredTo(nearestLocation)){
                        nearestLocation = locationToTest;
                    }
                }
            }
        }
        return nearestLocation;
    }
    /**
     * Method that will check if a worker can mine karbonite. If it has not performed an action this turn and
     * there is a karbonite pocket in adjacent squares, it will mine it
     */
    private void mineKarbonite() {
        for (int i = 0; i < DIRECTION_MAX_VAL + 1; i++) {
            Direction direction = Direction.swigToEnum(i);
            MapLocation newLocation = Player.gc.unit(this.getId()).location().mapLocation().add(direction);
            if (Player.gc.canHarvest(this.getId(), direction) && Player.gc.karboniteAt(newLocation) > 0) {
                Player.gc.harvest(this.getId(), direction);
                break;
            }
        }
    }
}

