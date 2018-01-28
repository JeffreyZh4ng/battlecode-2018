import bc.*;

import java.util.*;
import java.util.stream.IntStream;

public class Player {

    public static final int NUMBER_OF_LOCATIONS_TO_CHECK = 200;

    public static final GameController gc = new GameController();
    public static final Team team = gc.team();
    public static ArrayList<MapLocation> enemyStartingLocations = new ArrayList<>();



    public static void main(String[] args) {

        addStartingWorkersToEarthMap();
        storeEnemyLocations();
        queueUnitResearch();

        while (true) {
            if (gc.round() % 2 == 0) {
                System.runFinalization();
                System.gc();
            }
            if (gc.planet() == Planet.Earth && gc.team() == Team.Blue) {
                System.out.println("Round number: " + gc.round());
                System.out.println("Time left: " + gc.getTimeLeftMs());
                System.out.println("Karbonite: " + gc.karbonite());
                System.out.println("worker size: " + Earth.earthWorkerMap.size());
                System.out.println("attacker size: " + Earth.earthAttackerMap.size());
                System.out.println("attack target size: " + Earth.earthFocusedTargets.size());

                if (gc.round() == 1) {
                    Earth.createGlobalTask(Command.CONSTRUCT_FACTORY, null);
                    Earth.createGlobalTask(Command.CONSTRUCT_FACTORY, null);
                    Earth.createGlobalTask(Command.CONSTRUCT_FACTORY, null);
                }

                if (gc.round() == 500) {
                    Earth.createGlobalTask(Command.CONSTRUCT_ROCKET, null);
                    Earth.createGlobalTask(Command.CONSTRUCT_ROCKET, null);
                    Earth.createGlobalTask(Command.CONSTRUCT_ROCKET, null);
                    Earth.createGlobalTask(Command.CONSTRUCT_ROCKET, null);
                    Earth.createGlobalTask(Command.CONSTRUCT_ROCKET, null);
                    Earth.createGlobalTask(Command.CONSTRUCT_ROCKET, null);
                    Earth.createGlobalTask(Command.CONSTRUCT_ROCKET, null);
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
    private static void storeEnemyLocations() {
        VecUnit startingUnits = gc.startingMap(Planet.Earth).getInitial_units();
        for (int i = 0; i < startingUnits.size(); i++) {
            Unit startingUnit = startingUnits.get(i);
            if (startingUnit.team() != Player.team) {
                enemyStartingLocations.add(startingUnit.location().mapLocation());
                Earth.earthMainAttackStack.push(startingUnit.location().mapLocation());
            }
        }
    }

    /**
     * Helper that will queue all unit research
     */
    private static void queueUnitResearch() {
        gc.queueResearch(UnitType.Ranger);
        gc.queueResearch(UnitType.Ranger);
        gc.queueResearch(UnitType.Rocket);
        gc.queueResearch(UnitType.Mage);
        gc.queueResearch(UnitType.Mage);
        gc.queueResearch(UnitType.Mage);
        gc.queueResearch(UnitType.Worker);
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

//    /**
//     * A method that will convert the recognizable string back into a MapLocation
//     * @param location The MapLocation represented by the string
//     * @return A MapLocation that represents the string
//     */
//    public static MapLocation stringToLocation(String location) {
//        Planet mapPlanet;
//        if (location.charAt(0) == ' ') {
//            mapPlanet = Planet.Mars;
//            location = location.substring(1);
//        } else {
//            mapPlanet = Planet.Earth;
//        }
//
//        int spaceIndex = location.indexOf(' ');
//        int xLocation = Integer.parseInt(location.substring(0, spaceIndex));
//        location = location.substring(spaceIndex + 1);
//        int yLocation = Integer.parseInt(location);
//
//        return new MapLocation(mapPlanet, xLocation, yLocation);
//    }

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

//    /**
//     * Method that will find locations on mars that will let rockets land
//     */
//    // TODO: Redo
//    public static void findPassableMarsThreeSquares() {
//        ArrayList<MapLocation> availableLocations = new ArrayList<>();
//        PlanetMap marsStartingMap = Player.gc.startingMap(Planet.Mars);
//        for (int x = 1; x < marsStartingMap.getWidth() - 1; x++) {
//            for (int y = 1; y < marsStartingMap.getHeight() -1; y++) {
//                MapLocation location = new MapLocation(Planet.Mars, x, y);
//                boolean isClear = true;
//                for (Direction direction : Direction.values()) {
//                    if (availableLocations.contains(location.add(direction)) || marsStartingMap.isPassableTerrainAt(location.add(direction)) == 0) {
//                        isClear = false;
//                        break;
//                    }
//                }
//                if (isClear) {
//                    availableLocations.add(location);
//                }
//            }
//        }
//        availableLandingLocations = availableLocations;
//    }
//
//    /**
//     * Gets a landing location for mars
//     * @return The landing location
//     */
//    // TODO: Redo
//    public static MapLocation getLandingLocation() {
//        if (availableLandingLocations.size() == 0) {
//            return null;
//        }
//        MapLocation location = availableLandingLocations.get(0);
//        availableLandingLocations.remove(0);
//        return location;
//    }

    /**
     * Temp helper method that will get a location on mars for the rocket to land
     * @return The location the rocket can land
     */
    // TODO: Temp method redo
    public static MapLocation getRandomLocationToLandOnMars() {
        PlanetMap map = gc.startingMap(Planet.Mars);
        int width = (int)(map.getWidth());
        int height = (int)(map.getHeight());
        while (true) {
            int randomX = (int)(Math.random() * width);
            int randomY = (int)(Math.random() * height);

            MapLocation landingLocation = new MapLocation(Planet.Mars, randomX, randomY);
            if (Player.isLocationEmpty(landingLocation)) {
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
}


