import bc.*;

import java.util.*;

public class Player {

    public static int UNIT_CAP;
    public static boolean ROCKET_PRIORITY;
    private static final int NUMBER_OF_LOCATIONS_TO_CHECK = 200;
    private static int BUILD_ROUND;

    public static final GameController gc = new GameController();
    public static final Team team = gc.team();
    public static ArrayList<MapLocation> enemyStartingLocations = new ArrayList<>();



    public static void main(String[] args) {

        addStartingWorkersToEarthMap();
        System.out.println(Earth.earthWorkerMap.size());
        storeEnemyLocations(false);
        queueUnitResearch();

        while (true) {
            if (gc.round() % 2 == 0) {
                System.runFinalization();
                System.gc();
            }
            if (gc.planet() == Planet.Earth) {
                System.out.println("Round number: " + gc.round());
                System.out.println("Time left: " + gc.getTimeLeftMs());
                System.out.println("Karbonite: " + gc.karbonite());

                if (gc.round() == 1) {
                    Earth.createGlobalTask(Command.CONSTRUCT_FACTORY, null);
                    Earth.createGlobalTask(Command.CONSTRUCT_FACTORY, null);
                    Earth.createGlobalTask(Command.CONSTRUCT_FACTORY, null);
                }

                if (gc.round() == 100) {
                    storeEnemyLocations(true);
                }

                Earth.execute();

                System.out.println("");
            } else {
                Mars.execute();
                System.out.println("");
            }
            gc.nextTurn();
        }

    }

    /**
     * Method that will get the starting locations of all the enemy workers created when the game has started
     * and will store them in the earth attack queue and the initial enemy locations array list
     */
    private static void storeEnemyLocations(boolean addLocationToAttackMap) {
        VecUnit startingUnits = gc.startingMap(Planet.Earth).getInitial_units();
        for (int i = 0; i < startingUnits.size(); i++) {

            Unit startingUnit = startingUnits.get(i);
            if (startingUnit.team() != Player.team) {
                if (addLocationToAttackMap) {
                    Earth.earthMainAttackStack.push(startingUnit.location().mapLocation());
                } else {
                    enemyStartingLocations.add(startingUnit.location().mapLocation());
                }
            }
        }
    }

    /**
     * Helper that will queue all unit research
     */
    private static void queueUnitResearch() {
        gc.queueResearch(UnitType.Knight);
        gc.queueResearch(UnitType.Rocket);
        gc.queueResearch(UnitType.Worker);
        gc.queueResearch(UnitType.Knight);
        gc.queueResearch(UnitType.Knight);
        gc.queueResearch(UnitType.Rocket);
        gc.queueResearch(UnitType.Rocket);
    }

    /**
     * Looks at how much karbonite is around you to determine when to clone and when to start building factories
     */
    private static void getBuildRound() {
        PlanetMap map = gc.startingMap(Planet.Earth);

        int maxKarbonite = 0;
    }

    /**
     * Simplified method of senseUnitAtLocation that will handle the exception of if the location is not visible.
     * Catches all errors.
     * @param mapLocation The location of the unit that you want to sense
     * @return The unit at the location, null if
     */
    public static Unit senseUnitAtLocation(MapLocation mapLocation) {
        PlanetMap initialMap = gc.startingMap(mapLocation.getPlanet());
        if (initialMap.onMap(mapLocation)) {
            if (gc.canSenseLocation(mapLocation)) {
                if (gc.hasUnitAtLocation(mapLocation)) {
                    return gc.senseUnitAtLocation(mapLocation);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        return null;
    }

    /**
     * Will return the karbonite at a specific location. If the location is not on the map or the location is
     * not in the range of a robot, return -1
     * @param mapLocation The location you want to check the karbonite value of
     * @return The value of karbonite at the location
     */
    public static int karboniteAt(MapLocation mapLocation) {
        PlanetMap initialMap = gc.startingMap(mapLocation.getPlanet());
        if (initialMap.onMap(mapLocation)) {
            if (gc.canSenseLocation(mapLocation)) {
                return (int)(gc.karboniteAt(mapLocation));
            } else {
                return -1;
            }
        }

        return -1;
    }

    /**
     * Method that will add all the workers on earth to the HashMap of workers at the beginning of the game
     */
    private static void addStartingWorkersToEarthMap() {
        VecUnit startingUnits = gc.startingMap(Planet.Earth).getInitial_units();
        for (int i = 0; i < startingUnits.size(); i++) {
            if (startingUnits.get(i).team() == team) {
                int unitId = startingUnits.get(i).id();
                UnitInstance worker = new Worker(unitId);

                Earth.earthWorkerMap.put(unitId, worker);
            }
        }
    }

    /**
     * Helper method that will return all the directions around the robot except fo the center
     * @return All the directions except for the center
     */
    public static ArrayList<Direction> getMoveDirections() {
        ArrayList<Direction> directions = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            directions.add(Direction.swigToEnum(i));
        }
        return directions;
    }

    /**
     * Method that will convert a MapLocation into an easily recognizable string.
     * @param mapLocation The MapLocation that you want to convert
     * @return A string that represents the MapLocation
     */
    public static String locationToString(MapLocation mapLocation) {
        StringBuilder convertedLocation = new StringBuilder();

        if (mapLocation.getPlanet() == Planet.Mars) {
            convertedLocation.append(" ");
        }
        convertedLocation.append(mapLocation.getX());
        convertedLocation.append(" ");
        convertedLocation.append(mapLocation.getY());

        return convertedLocation.toString();
    }

    /**
     * Method that will check if a location is empty. Checks if the location is onMap, passableTerrain,
     * and if it is not occupied by a unit.
     * @param location The location to check
     * @return If the location appears empty
     */
    public static boolean isLocationEmpty(MapLocation location) {
        PlanetMap planetMap = gc.startingMap(location.getPlanet());
        if (planetMap.onMap(location) && planetMap.isPassableTerrainAt(location) > 0) {
            if (Player.gc.hasUnitAtLocation(location)) {
                return false;
            }
            return true;
        }

        return false;
    }

    /**
     * Method that will check if a location is empty. Checks if the location is onMap, passableTerrain,
     * and if it is not occupied by a structure
     * @param location The location to check
     * @return If the location appears empty
     */
    public static boolean isLocationEmptyForStructure(MapLocation location) {
        PlanetMap planetMap = gc.startingMap(location.getPlanet());
        if (planetMap.onMap(location) && planetMap.isPassableTerrainAt(location) > 0) {
            if (Player.gc.hasUnitAtLocation(location)) {
                Unit unit = Player.gc.senseUnitAtLocation(location);
                return unit.unitType() != UnitType.Factory && unit.unitType() != UnitType.Rocket;
            }
            return true;
        }

        return false;
    }

    /**
     * Helper method to see if a location is on the map and passable.
     * @param mapLocation The location you want to check
     * @return If the location is on the map and passable
     */
    public static boolean isOnMap(MapLocation mapLocation) {
        PlanetMap planetMap = gc.startingMap(mapLocation.getPlanet());
        return planetMap.onMap(mapLocation) && planetMap.isPassableTerrainAt(mapLocation) > 0;
    }

    /**
     * Temp helper method that will get a location on mars for the rocket to land
     * @return The location the rocket can land
     */
    public static MapLocation getRandomLocationToLandOnMars() {
        PlanetMap map = gc.startingMap(Planet.Mars);
        int width = (int)(map.getWidth());
        int height = (int)(map.getHeight());
        while (true) {
            int randomX = (int)(Math.random() * width);
            int randomY = (int)(Math.random() * height);

            MapLocation landingLocation = new MapLocation(Planet.Mars, randomX, randomY);
            boolean isClear = true;

            for (Direction direction : Direction.values()) {
                if (Player.isLocationEmpty(landingLocation.add(direction))) {
                    isClear = false;
                    break;
                }
            }
            if (isClear) {
                return landingLocation;
            }

        }
    }

    /**
     * Finds the nearest units to a given task that are not already on the task
     * @param globalTask The global task of that is requesting units
     * @return The list of closest idle workers to add to the global task
     */
    public static ArrayList<Integer> getNearestFriendlyUnit(GlobalTask globalTask, boolean isWorker, int numberRequested) {

        HashMap<String, Integer> unitLocations = new HashMap<>();
        if (isWorker) {
            for (int unitId: Earth.earthWorkerMap.keySet()) {
                unitLocations.put(locationToString(Earth.earthWorkerMap.get(unitId).getLocation()), unitId);
            }
        } else {
            for (int unitId: Earth.earthAttackerMap.keySet()) {
                unitLocations.put(locationToString(Earth.earthAttackerMap.get(unitId).getLocation()), unitId);
            }
        }

        // Compile the list of units already on the task and remove them from the unitLocations list. Check if the
        // Unit is not in a garrison before removing its location
        for (int unitId: globalTask.getUnitsOnTask()) {
            if (!gc.unit(unitId).location().isInGarrison()) {

                MapLocation unitLocation = Player.gc.unit(unitId).location().mapLocation();
                if (unitLocations.containsKey(locationToString(unitLocation))) {
                    unitLocations.remove(locationToString(unitLocation));
                }
            }
        }

        MapLocation centerLocation = globalTask.getTaskLocation();
        Queue<MapLocation> frontier = new LinkedList<>();
        frontier.add(centerLocation);

        HashSet<String> checkedLocations = new HashSet<>();
        checkedLocations.add(locationToString(centerLocation));

        // Initializes the list of closest units and will check if there is a unit at the spot of the task
        // Because we automatically add that into the checked locations list
        ArrayList<Integer> closestUnitIds = new ArrayList<>();
        PlanetMap planetMap = gc.startingMap(centerLocation.getPlanet());
        if (unitLocations.containsKey(locationToString(centerLocation))) {
            closestUnitIds.add(unitLocations.get(locationToString(centerLocation)));
        }

        int moveRadius = 0;
        while (!frontier.isEmpty() && moveRadius < NUMBER_OF_LOCATIONS_TO_CHECK) {
            MapLocation currentLocation = frontier.poll();

            for (Direction nextDirection : getMoveDirections()) {
                MapLocation nextMapLocation = currentLocation.add(nextDirection);
                String nextLocation = locationToString(nextMapLocation);

                if (planetMap.onMap(nextMapLocation) && !checkedLocations.contains(nextLocation)) {
                    checkedLocations.add(nextLocation);
                    frontier.add(nextMapLocation);

                    if (unitLocations.containsKey(nextLocation)) {
                        closestUnitIds.add(unitLocations.get(nextLocation));

                        if (closestUnitIds.size() == numberRequested) {
                            return closestUnitIds;
                        }
                    }
                }
            }

            moveRadius++;
        }

        return closestUnitIds;
    }

    /**
     * Utilizes breadth first search algorithm to check if location will ever be accesible, does not consider units as obstacles
     * @param startLocation the location to start search from
     * @param destination the destination location
     * @return if the destination is accessible
     */
    public static boolean isLocationAccessible(MapLocation startLocation, MapLocation destination) {
        Queue<MapLocation> frontier = new LinkedList<>();
        frontier.add(startLocation);

        HashMap<String, MapLocation> checkedLocations = new HashMap<>();
        checkedLocations.put(Player.locationToString(startLocation), startLocation);

        while (!frontier.isEmpty()) {

            // Get next direction to check around. Will put in the checked location a pair with the key as the
            // Next location with the value as the current location.
            MapLocation currentLocation = frontier.poll();
            if (currentLocation.isAdjacentTo(destination)) {
                return true;
            }

            // Check if locations around frontier location have already been added to came from and if they are empty
            for (Direction nextDirection: Player.getMoveDirections()) {
                MapLocation nextLocation = currentLocation.add(nextDirection);

                if (Player.isLocationEmptyForStructure(nextLocation) && !checkedLocations.containsKey(Player.locationToString(nextLocation))) {
                    frontier.add(nextLocation);
                    checkedLocations.put(Player.locationToString(nextLocation), currentLocation);
                }
            }
        }
        return false;
    }

    public static int getPassableArea(MapLocation startLocation) {

        Queue<MapLocation> frontier = new LinkedList<>();
        frontier.add(startLocation);

        HashSet<String> checkedLocations = new HashSet<>();
        checkedLocations.add(Player.locationToString(startLocation));

        while (!frontier.isEmpty()) {

            // Get next direction to check around. Will put in the checked location a pair with the key as the
            // Next location with the value as the current location.
            MapLocation currentLocation = frontier.poll();

            // Check if locations around frontier location have already been added to came from and if they are empty
            for (Direction nextDirection: Player.getMoveDirections()) {
                MapLocation nextLocation = currentLocation.add(nextDirection);

                if (Player.isLocationEmptyForStructure(nextLocation) && !checkedLocations.contains(Player.locationToString(nextLocation))) {
                    frontier.add(nextLocation);
                    checkedLocations.add(Player.locationToString(nextLocation));
                }
            }
        }
        return checkedLocations.size();
    }

    public static void setProduceNumber() {
        int workerId = Earth.getBestWorkerId();
        int area = getPassableArea(Earth.earthWorkerMap.get(workerId).getLocation());
        if (area < 100) {
            UNIT_CAP = 20;
        } else if (area < 200) {
            UNIT_CAP = 50;
        } else {
            UNIT_CAP = 100;
        }
    }

    public static void setRocketPriority() {
        MapLocation myLocation = Earth.earthWorkerMap.get(Earth.getBestWorkerId()).getLocation();
        for (MapLocation enemyStartingLocation: Player.enemyStartingLocations) {
            if (!Player.isLocationAccessible(myLocation, enemyStartingLocation)) {
                ROCKET_PRIORITY = false;
                return;
            }
        }

        ROCKET_PRIORITY = true;
    }
}


