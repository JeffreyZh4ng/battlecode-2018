import bc.*;

import java.util.*;

public class Worker extends Robot {

    public Worker(int id) {
        super(id);
    }

    @Override
    public void run() {

        if (this.getEmergencyTask() != null) {
            if (executeTask(this.getEmergencyTask())) {
                // System.out.println("Worker: " + this.getId() + " Finished emergency task!");

                if (this.getCurrentTask() != null && this.getEmergencyTask().getCommand() == Command.STALL) {
                    GlobalTask globalTask = Earth.earthTaskMap.get(this.getCurrentTask().getTaskId());
                    globalTask.finishedTask(this.getId(), this.getCurrentTask().getCommand());
                    return;
                }
                this.setEmergencyTask(null);
                return;
            }

        } else if (!this.isIdle()) {
            if (executeTask(this.getCurrentTask())) {
                // System.out.println("Worker: " + this.getId() + " has finished task: " + this.getCurrentTask().getCommand());
                if (this.getCurrentTask().getTaskId() != -1) {
                    GlobalTask globalTask = Earth.earthTaskMap.get(this.getCurrentTask().getTaskId());
                    globalTask.finishedTask(this.getId(), this.getCurrentTask().getCommand());
                    run();
                    return;
                } else {
                    this.removeTask();
                }
            }

        } else if (Earth.earthTaskQueue.size() == 0) {
            // System.out.println("Worker: " + this.getId() + " Setting task to wander and mine");

            boolean karboniteHere = false;
            for (Direction direction : Direction.values()) {
                if (Earth.earthKarboniteMap.containsKey(Player.mapLocationToString(this.getLocation().add(direction)))) {
                    karboniteHere = true;
                }
            }
            if(!karboniteHere) {
                wanderToMine();
            }
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
        // System.out.println("Worker: " + this.getId() + " " + robotCommand);

        switch (robotCommand) {
            case MOVE:
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
                return true;
            default:
                // System.out.println("Critical error occurred in Worker: " + this.getId());
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

                    // System.out.println("Worker: " + this.getId() + " Cloned worker!");
                    // System.out.println("New worker has ID of: " + clonedWorkerId);
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
     * @param unitType Either a factory or rocket blueprint
     * @return If the blueprint was built or not
     */
    private boolean blueprintStructure(MapLocation commandLocation, UnitType unitType) {
        MapLocation robotCurrentLocation = Player.gc.unit(this.getId()).location().mapLocation();
        Direction directionToBlueprint = robotCurrentLocation.directionTo(commandLocation);

        if (Player.gc.canBlueprint(this.getId(), unitType, directionToBlueprint) &&
                this.getLocation().isAdjacentTo(commandLocation)) {

            Player.gc.blueprint(this.getId(), unitType, directionToBlueprint);

            int structureId = Player.gc.senseUnitAtLocation(commandLocation).id();

            if (unitType == UnitType.Factory) {
                UnitInstance newStructure = new Factory(structureId, false, commandLocation);
                Earth.earthFactoryMap.put(structureId, newStructure);
            } else {
                Rocket newStructure = new Rocket(structureId, false, commandLocation);
                Earth.earthRocketMap.put(structureId, newStructure);
            }
            // System.out.println("Worker: " + this.getId() + " Blueprinted structure at " + commandLocation.toString());
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
        try {
            structureId = Player.senseUnitAtLocation(commandLocation).id();
        } catch (Exception e) {
            // System.out.println("Worker: " + this.getId() + " Was not able to sense the location!");
            return false;
        }

        if (Player.gc.canBuild(this.getId(), structureId) && this.getLocation().isAdjacentTo(commandLocation)) {
            Player.gc.build(this.getId(), structureId);
            // System.out.println("Worker: " + this.getId() + " ran build()");

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

                    GlobalTask loadRocket = new GlobalTask(8, Command.LOAD_ROCKET, commandLocation);
                    Earth.earthTaskQueue.add(loadRocket);
                }

                // System.out.println("Worker: " + this.getId() + " Built structure");
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
                break;
            }
        }
    }

    /**
     * Method that will set the robots current task to wander and mine karbonite
     */
    private void wanderToMine() {
        MapLocation karboniteLocation = getPathToKarbonite(this.getLocation(), Player.gc.startingMap(Player.gc.planet()));
        if (karboniteLocation != null && this.getMovePathStack() != null) {
            this.setCurrentTask(new RobotTask(-1, Command.MOVE, karboniteLocation));
            // System.out.println("Setting the current task to go mine karbonite");
        }
    }

    /**
     * Method that will get the path to the nearest karbonite deposit.
     * @param startingLocation The starting location of the robot
     * @param map The map the robot is on
     * @return The stack of path values to the karbonite deposit
     */
    private MapLocation getPathToKarbonite(MapLocation startingLocation, PlanetMap map) {

        if (Earth.earthKarboniteMap.size() == 0) {
            return null;
        }
        ArrayList<Direction> moveDirections = Player.getMoveDirections();

        //shuffle directions so that wandering doesn't gravitate towards a specific direction
        MapLocation destinationLocation = null;

        Queue<MapLocation> frontier = new LinkedList<>();
        frontier.add(startingLocation);

        HashMap<String, MapLocation> checkedLocations = new HashMap<>();
        checkedLocations.put(startingLocation.toString(), startingLocation);

        while (!frontier.isEmpty()) {
            // Get next direction to check around
            MapLocation currentLocation = frontier.poll();


            Collections.shuffle(moveDirections, new Random());
            // Check if locations around frontier location have already been added to came from and if they are empty
            for (Direction nextDirection : moveDirections) {
                MapLocation nextLocation = currentLocation.add(nextDirection);

                if (Player.isLocationEmpty(map, nextLocation) && !checkedLocations.containsKey(nextLocation.toString())) {
                    frontier.add(nextLocation);
                    checkedLocations.put(nextLocation.toString(), currentLocation);
                    if (Earth.earthKarboniteMap.containsKey(Player.mapLocationToString(nextLocation))) {
                        frontier.clear();
                        destinationLocation = nextLocation;
                    }
                }
            }
        }

        if (destinationLocation == null) {
            return null;
        }
        Stack<MapLocation> newPath = new Stack<>();
        MapLocation currentTraceLocation = destinationLocation;

        // trace back path
        while (!currentTraceLocation.equals(startingLocation)) {
            newPath.push(currentTraceLocation);
            currentTraceLocation = checkedLocations.get(currentTraceLocation.toString());
            if (currentTraceLocation == null) {
                break;
            }
        }
        this.setMovePathStack(newPath);

        return destinationLocation;
    }
}

