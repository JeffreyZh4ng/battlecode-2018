import bc.*;

import java.util.*;

public class Player {

    public static final GameController gc = new GameController();
    // TODO: Need to go through code and find any instances of Player.gc.Team() and replace with this. Need to
    // TODO: reduce API calls because that is whats causing many timeout errors
    public static final Team team = gc.team();

    private static ArrayList<MapLocation> availableLandingLocations = null;

    public static void main(String[] args) {

        addStartingWorkersToEarthMap();
        Queue<MapLocation> enemyPositions = enemyLocations();
        gc.queueResearch(UnitType.Rocket);
        gc.queueResearch(UnitType.Ranger);
        gc.queueResearch(UnitType.Ranger);
        gc.queueResearch(UnitType.Mage);
        gc.queueResearch(UnitType.Mage);
        gc.queueResearch(UnitType.Mage);
        gc.queueResearch(UnitType.Worker);

        while (true) {
            if (gc.round() % 2 == 0) {
                System.runFinalization();
                System.gc();
            }
            if (gc.planet() == Planet.Earth && gc.team() == Team.Red) {
                System.out.println("Round number: " + gc.round());
                System.out.println("Karbonite: " + gc.karbonite());

                if (gc.round() == 1) {
                    setStructureLocations(0, 100);
                    Earth.createGlobalTask(Command.CONSTRUCT_FACTORY);
                }

//                if (gc.round() % 75 == 0) {
//                    findPassableMarsThreeSquares();
//                    Earth.createGlobalTask(Command.CONSTRUCT_ROCKET);
//                }

                Earth.execute();

                System.out.println("");
            } else {
                // Mars.execute();
            }

            gc.nextTurn();
        }
    }

    /**
     * Method that will get the starting locations of all the enemy workers created when the game has started
     * @return An array of enemy locations
     */
    public static Queue<MapLocation> enemyLocations() {
        int mapCenterX = (int)((gc.startingMap(Planet.Earth).getWidth()) / 2);
        int mapCenterY = (int)((gc.startingMap(Planet.Earth).getHeight()) / 2);

        Queue<MapLocation> enemyLocations = new LinkedList<>();
        for (int workerId: Earth.earthWorkerMap.keySet()) {
            MapLocation mapLocation = Earth.earthWorkerMap.get(workerId).getLocation();
            int xDistance = mapCenterX - mapLocation.getX();
            int yDistance = mapCenterY - mapLocation.getY();

            if (mapCenterX % 2 == 0) {
                mapCenterX--;
            }
            if (mapCenterY % 2 == 0) {
                mapCenterY--;
            }

            enemyLocations.add(new MapLocation(Planet.Earth, mapCenterX + xDistance, mapCenterY + yDistance));
            mapCenterX = (int)((gc.startingMap(Planet.Earth).getWidth()) / 2);
            mapCenterY = (int)((gc.startingMap(Planet.Earth).getHeight()) / 2);
        }

        return enemyLocations;
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
        VecUnit units = gc.myUnits();
        for (int i = 0; i < units.size(); i++) {
            int unitId = units.get(i).id();
            UnitInstance worker = new Worker(unitId);

            Earth.earthWorkerMap.put(unitId, worker);
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
     * A method that will convert the recognizable string back into a MapLocation
     * @param location The MapLocation represented by the string
     * @return A MapLocation that represents the string
     */
    public static MapLocation stringToLocation(String location) {
        Planet mapPlanet;
        if (location.charAt(0) == ' ') {
            mapPlanet = Planet.Mars;
            location = location.substring(1);
        } else {
            mapPlanet = Planet.Earth;
        }

        int spaceIndex = location.indexOf(' ');
        int xLocation = Integer.parseInt(location.substring(0, spaceIndex));
        location = location.substring(spaceIndex + 1);
        int yLocation = Integer.parseInt(location);

        return new MapLocation(mapPlanet, xLocation, yLocation);
    }

    /**
     * Finds structure locations based on initial map locations.
     * @return the initially available structure locations
     */
    // TODO: Redo
    public static void setStructureLocations(int maxNonPassable, int radius) {

        MapLocation workerLocation = null;
        int minDistance = -1;
        // find worker with smallest sum to other workers, implementation inefficnet but max 3 workers so not big concern
        VecUnit myWorkers = Player.gc.units();
        // // System.out.println(myWorkers);
        for (int i = 0; i < myWorkers.size(); i ++) {
            int distanceSum = 0;
            MapLocation workerOneLocation = myWorkers.get(i).location().mapLocation();
            for (int j = 0; j < myWorkers.size(); j ++) {
                if (i != j) {
                    distanceSum += workerOneLocation.distanceSquaredTo(myWorkers.get(j).location().mapLocation());
                }
            }
            if (minDistance == -1 || distanceSum < minDistance) {
                workerLocation = workerOneLocation;
                minDistance = distanceSum;
            }
        }
        // // System.out.println(workerLocation);

        VecMapLocation locationsToCheck = Player.gc.allLocationsWithin(workerLocation, radius);
        HashSet<String> chosenLocations = new HashSet<>();
        ArrayList<MapLocation> clearLocations = new ArrayList<>();

        for (int i = 0; i < locationsToCheck.size(); i++) {
            //location to test is the center location
            MapLocation locationToTest = locationsToCheck.get(i);
            int nonPassableCount = 0;
            boolean clear = true;
            if (!Player.gc.startingMap(Player.gc.planet()).onMap(locationToTest) || Player.gc.startingMap(Player.gc.planet()).isPassableTerrainAt(locationToTest) == 0) {
                clear = false;
            }
            for (Direction direction : Direction.values()) {
                if (!clear) {
                    break;
                }
                if (chosenLocations.contains(locationToTest.add(direction).toString())) {
                    clear = false;
                    break;
                }
                //is not passable terrain
                if (!Player.gc.startingMap(Player.gc.planet()).onMap(locationToTest.add(direction)) || Player.gc.startingMap(Player.gc.planet()).isPassableTerrainAt(locationToTest.add(direction)) == 0) {
                    nonPassableCount++;
                    if (nonPassableCount > maxNonPassable) {
                        clear = false;
                        break;
                    }
                }
            }
            if (clear) {
                clearLocations.add(locationToTest);
                chosenLocations.add(locationToTest.toString());
            }

        }
        Earth.availableStructureLocations = clearLocations;
    }

    /**
     * Method that will check if a location is empty. Checks if the location is onMap, passableTerrain,
     * and if it is not occupied by a factory or rocket. Return false if there is a robot there
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
     * Method that will find locations on mars that will let rockets land
     */
    // TODO: Redo
    public static void findPassableMarsThreeSquares() {
        ArrayList<MapLocation> availableLocations = new ArrayList<>();
        PlanetMap marsStartingMap = Player.gc.startingMap(Planet.Mars);
        for (int x = 1; x < marsStartingMap.getWidth() - 1; x++) {
            for (int y = 1; y < marsStartingMap.getHeight() -1; y++) {
                MapLocation location = new MapLocation(Planet.Mars, x, y);
                boolean isClear = true;
                for (Direction direction : Direction.values()) {
                    if (availableLocations.contains(location.add(direction)) || marsStartingMap.isPassableTerrainAt(location.add(direction)) == 0) {
                        isClear = false;
                        break;
                    }
                }
                if (isClear) {
                    availableLocations.add(location);
                }
            }
        }
        availableLandingLocations = availableLocations;
    }

    /**
     * Gets a landing location for mars
     * @return The landing location
     */
    // TODO: Redo
    public static MapLocation getLandingLocation() {
        if (availableLandingLocations.size() == 0) {
            return null;
        }
        MapLocation location = availableLandingLocations.get(0);
        availableLandingLocations.remove(0);
        return location;
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

        // Compile the list of units already on the task and remove them from the unitLocations list
        for (int unitId: globalTask.getUnitsOnTask()) {
            MapLocation unitLocation = Player.gc.unit(unitId).location().mapLocation();
            unitLocations.remove(locationToString(unitLocation));
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
        while (!frontier.isEmpty() && moveRadius < 8) {
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
}


