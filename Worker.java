import bc.*;

import java.util.*;

public class Worker extends Robot {

    // TODO: create a method that will analyze the map and determine the number of workers needed
    private static final int NUMBER_OF_WORKERS_NEEDED = 4;

    private MapLocation spawnLocation;

    public Worker(int id) {
        super(id);
        spawnLocation = Player.gc.unit(id).location().mapLocation();
    }

    @Override
    public void run() {

        System.out.println("Worker: " + this.getId() + " location: " + Player.locationToString(this.getLocation()));

        if (this.getEmergencyTask() != null) {
            executeEmergencyTask();
        }

        if (this.hasTasks()) {
            checkTaskStatus();
            executeCurrentTask();
            if (this.hasTasks() && this.getCurrentTask().getCommand() == Command.STALL) {
                return;
            }

        } else if (!this.hasTasks()) {
            executeIdleActions();
        }

        // The worker will always try to mine karbonite when it can
        mineKarbonite();
    }

    /**
     * Method that will check the current status of the the worker's task. Removes task if it is already finished
     */
    private void checkTaskStatus() {
        if (this.getCurrentTask().getTaskId() != -1) {

            GlobalTask currentGlobalTask = Earth.earthTaskMap.get(this.getCurrentTask().getTaskId());
            if (currentGlobalTask.checkGlobalTaskStatus(this.getCurrentTask().getCommand())) {
                System.out.println("Worker: " + this.getId() + " popped task " + this.getCurrentTask().getCommand());
                this.pollCurrentTask();

                // If the task was already completed, check if the next one was completed as well
                if (this.hasTasks()) {
                    checkTaskStatus();
                }
            }
        }
    }

    /**
     * Helper method that will control how the robot operates when it has an emergency task that is not STALL
     */
    private void executeEmergencyTask() {
        if (executeTask(this.getEmergencyTask())) {
            System.out.println("Worker: " + this.getId() + " Finished emergency task!");
            this.setEmergencyTask(null);
        }
    }

    /**
     * Helper method that will run the workers current tasks. If it finished one, it checks if it can start the next
     */
    private void executeCurrentTask() {
        if (this.hasTasks() || this.getEmergencyTask() != null) {
            System.out.println("Worker " + this.getId() + " on task " + this.getCurrentTask().getCommand() +
                    " in global task: " + this.getCurrentTask().getTaskId() + " at " + Player.locationToString(this.getCurrentTask().getCommandLocation()));
        }

        if (this.hasTasks() || this.getEmergencyTask() != null) {
            if (executeTask(this.getCurrentTask())) {
                System.out.println("Worker: " + this.getId() + " has finished task: " + this.getCurrentTask().getCommand());
                this.pollCurrentTask();

                // If the worker has completed the current task, check if it can also complete the next one
                if (this.hasTasks()) {
                    executeCurrentTask();
                }
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
                return this.pathManager(commandLocation);
            case WANDER:
                return this.pathManager(commandLocation);
            case CLONE:
                return cloneWorker(commandLocation);
            case BUILD:
                return buildStructure(commandLocation);
            case BLUEPRINT_FACTORY:
                return blueprintStructure(commandLocation, UnitType.Factory);
            case BLUEPRINT_ROCKET:
                return blueprintStructure(commandLocation, UnitType.Rocket);
            case STALL:
                this.requestUnitToLoad(commandLocation);
                return false;
            default:
                System.out.println("Critical error occurred in Worker: " + this.getId());
                return true;
        }
    }

    /**
     * Helper method that will control what the robot does when it has no current tasks
     */
    private void executeIdleActions() {
        MapLocation newMoveLocation = getNearestKarboniteLocation();

        if (newMoveLocation != null) {
            this.addTaskToQueue(new RobotTask(-1, Command.WANDER, newMoveLocation));
            System.out.println("Worker: " + this.getId() + " Setting task to wander and mine");

        } else {
            wanderWithinRadius(100);
            System.out.println("Worker: " + this.getId() + " Wandering!");
        }
    }

    /**
     * Given a MapLocation, see if you can clone a worker and put it at that spot
     * @param commandLocation The MapLocation of the new worker
     * @return If the worker was cloned or not
     */
    private boolean cloneWorker(MapLocation commandLocation) {
        MapLocation robotCurrentLocation = Player.gc.unit(this.getId()).location().mapLocation();

        for (int i = 0; i < 8; i++) {
            Direction direction = Direction.swigToEnum(i);
            MapLocation newLocation = robotCurrentLocation.add(direction);

            if (commandLocation.isAdjacentTo(newLocation)) {

                Direction directionToClone = robotCurrentLocation.directionTo(newLocation);

                if (Player.gc.canReplicate(this.getId(), directionToClone)) {
                    Player.gc.replicate(this.getId(), directionToClone);

                    int clonedWorkerId = Player.senseUnitAtLocation(newLocation).id();
                    UnitInstance newWorker = new Worker(clonedWorkerId);

                    Earth.earthStagingWorkerMap.put(clonedWorkerId, newWorker);

                    System.out.println("Worker: " + this.getId() + " Cloned worker!");
                    System.out.println("New worker has ID of: " + clonedWorkerId);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Given the map location of a blueprint you want to build, check if you can build it and add
     * a new blueprint instance to the blueprint map
     * @param commandLocation The MapLocation of the blueprint you want to build
     * @param structureType Either a factory or rocket blueprint
     * @return If the blueprint was built or not
     */
    private boolean blueprintStructure(MapLocation commandLocation, UnitType structureType) {
        Direction directionToBlueprint = this.getLocation().directionTo(commandLocation);

        if (Player.gc.canBlueprint(this.getId(), structureType, directionToBlueprint) &&
                this.getLocation().isAdjacentTo(commandLocation)) {

            Player.gc.blueprint(this.getId(), structureType, directionToBlueprint);
            int structureId = Player.gc.senseUnitAtLocation(commandLocation).id();

            if (structureType == UnitType.Factory) {
                UnitInstance newStructure = new Factory(structureId, false);
                Earth.earthFactoryMap.put(structureId, newStructure);
            } else {
                Rocket newStructure = new Rocket(structureId, false);
                Earth.earthRocketMap.put(structureId, newStructure);
            }

            // Set the global task variable hasBlueprinted to true
            Earth.earthTaskMap.get(this.getCurrentTask().getTaskId()).structureHasBeenBlueprinted();

            System.out.println("Worker: " + this.getId() + " Blueprinted structure at " + Player.locationToString(commandLocation));
            return true;
        }

        return false;
    }

    /**
     * Given a MapLocation of a blueprint, build it until it reaches full health and becomes a rocket/factory
     * @param commandLocation The location of the unfinished structure
     * @return If the structure finished building
     */
    private boolean buildStructure(MapLocation commandLocation) {
        int structureId;
        if (Player.gc.hasUnitAtLocation(commandLocation)) {
            structureId = Player.senseUnitAtLocation(commandLocation).id();
        } else {

            // If for some reason the factory at the given location disappeared, return true to pop the task
            return true;
        }

        if (Player.gc.canBuild(this.getId(), structureId)) {
            Player.gc.build(this.getId(), structureId);
            System.out.println("Worker: " + this.getId() + " is building structure " + structureId);

            // Check if it can clone here because we know it has no path when it is building and while building
            // Is when you need another worker the most
            if (Player.gc.karbonite() > 60 && Earth.earthWorkerMap.size() < NUMBER_OF_WORKERS_NEEDED) {
                executeTask(new RobotTask(-1, Command.CLONE, commandLocation));
            }

            if (Player.gc.unit(structureId).structureIsBuilt() > 0) {

                UnitType unitType = Player.gc.unit(structureId).unitType();
                if (unitType == UnitType.Factory) {
                    UnitInstance builtFactory = new Factory(structureId, true);
                    Earth.earthFactoryMap.put(structureId, builtFactory);
                } else {
                    Rocket builtRocket = new Rocket(structureId, true);
                    Earth.earthFactoryMap.put(structureId, builtRocket);
                }

                // Set the global task variable hasBlueprinted to true
                Earth.earthTaskMap.get(this.getCurrentTask().getTaskId()).structureHasBeenBuilt();

                System.out.println("Worker: " + this.getId() + " Built structure");
                return true;
            }
        }
        return false;
    }

    /**
     * Method that will check if a worker can mine karbonite. If it has not performed an action this turn and
     * there is a karbonite pocket in adjacent squares, it will mine it
     */
    private void mineKarbonite() {
        for (int i = 0; i < 8 + 1; i++) {
            Direction direction = Direction.swigToEnum(i);
            MapLocation newLocation = Player.gc.unit(this.getId()).location().mapLocation().add(direction);
            if (Player.gc.canHarvest(this.getId(), direction) && Player.karboniteAt(newLocation) > 0) {
                Player.gc.harvest(this.getId(), direction);
                System.out.println("Worker: " + this.getId() + " mined karbonite");
                break;
            }
        }
    }

    /**
     * Method that will get the nearest location of a karbonite deposit. POTENTIAL MEM LEAK IF RUNS TOO MUCH
     * @return The MapLocation of the nearest karbonite deposit. Null if there is no karbonite on the map
     */
    // TODO: Change this so that is senses all locations within a radius of x. If is finds any within the radius
    // TODO: Of the unit, THEN start the search algorithm.
    private MapLocation getNearestKarboniteLocation() {

        MapLocation destinationLocation = null;

        MapLocation myLocation = this.getLocation();

        Queue<MapLocation> frontier = new LinkedList<>();
        frontier.add(myLocation);

        HashMap<String, MapLocation> checkedLocations = new HashMap<>();
        checkedLocations.put(Player.locationToString(myLocation), myLocation);

        int counter = 0;
        while (!frontier.isEmpty() && counter < 100) {

            // Get next direction to check around. Will put in the checked location a pair with the key as the
            // Next location with the value as the current location.
            MapLocation currentLocation = frontier.poll();
            if (Player.gc.canSenseLocation(currentLocation) && Player.gc.karboniteAt(currentLocation) > 0) {
                destinationLocation = currentLocation;
                //checkedLocations.put(Player.locationToString(destinationLocation), currentLocation);
                frontier.clear();
                break;
            }

            // shuffle makes stay more in a general area rather than constantly gravitate northward then
            ArrayList<Direction> moveDirections = Player.getMoveDirections();
            Collections.shuffle(moveDirections);
            // Check if locations around frontier location have already been added to came from and if they are empty
            for (Direction nextDirection: moveDirections) {
                MapLocation nextLocation = currentLocation.add(nextDirection);

                if (Player.gc.canSenseLocation(nextLocation) && Player.isLocationEmpty(nextLocation) &&
                        !checkedLocations.containsKey(Player.locationToString(nextLocation))) {
                    frontier.add(nextLocation);
                    checkedLocations.put(Player.locationToString(nextLocation), currentLocation);
                }
            }
            counter++;
        }

        return destinationLocation;
    }

    /**
     * Method that will set a robots task to wander within a certain radius
     * @param radius The radius to wander in
     */
    private void wanderWithinRadius(int radius) {
        VecMapLocation mapLocations = Player.gc.allLocationsWithin(spawnLocation, radius);

        MapLocation wanderLocation = null;
        while (wanderLocation == null) {

            int randomLocation = (int)(Math.random() * mapLocations.size());
            if (Player.isLocationEmpty(mapLocations.get(randomLocation))) {
                wanderLocation = mapLocations.get(randomLocation);
            }
        }

        this.addTaskToQueue(new RobotTask(-1, Command.WANDER, wanderLocation));
    }
}

