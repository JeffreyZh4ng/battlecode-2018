import bc.*;

import java.util.*;

public class Player {

    public static final GameController gc = new GameController();
    // TODO: Need to go through code and find any instances of Player.gc.Team() and replace with this. Need to
    // TODO: reduce API calls because that is whats causing many timeout errors
    public static final Team team = gc.team();


    public static void main(String[] args) {

        addStartingWorkersToEarthMap();
        Queue<MapLocation> enemyPositions = enemyLocations();

        while (true) {

            if (gc.round() % 2 == 0) {
                System.runFinalization();
                System.gc();
            }
            if (gc.planet() == Planet.Earth && gc.team() == Team.Blue) {

                System.out.println("Round number: " + gc.round());
                System.out.println("Time left: " + Player.gc.getTimeLeftMs());

                if (gc.round() == 1) {
                    setStructureLocations(0, 100);
                    Earth.createGlobalTask(Command.CONSTRUCT_FACTORY);
                    Earth.createGlobalTask(Command.CONSTRUCT_FACTORY);
                    Earth.createGlobalTask(Command.CONSTRUCT_FACTORY);
                }
                if (Earth.rangerCount > 10 && Earth.earthAttackTarget == null) {
                    System.out.println("Setting attack target!");
                    Earth.earthAttackTarget = enemyPositions.peek();
                    if (enemyPositions.size() != 0) {
                        enemyPositions.poll();
                    }
                }
                Earth.execute();

                System.out.println("");
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
     * Simplified method of isOccupiable that will handle any exceptions that the gc.isOccupiable method will
     * throw. Check if the location is on the map then checks if it can see the location. If the location can
     * be seen then return the default isOccupiable method. If we cant see the location then return true because
     * the worst that can happen is there is an enemy unit there which we can destroy.
     * @param mapLocation The location you want to check
     * @return If it is occupiable or not
     */
    public static boolean isOccupiable(MapLocation mapLocation) {
        PlanetMap initialMap = gc.startingMap(mapLocation.getPlanet());
        if (initialMap.onMap(mapLocation) && initialMap.isPassableTerrainAt(mapLocation) > 0) {
            if (gc.canSenseLocation(mapLocation)) {
                return gc.isOccupiable(mapLocation) > 0;
            } else {
                return true;
            }

        }

        return false;
    }

    /**
     * Is occupiable for structures
     * @param mapLocation The location you want to check
     * @return If it is occupiable or not
     */
    public static boolean isOccupiableForStructure(MapLocation mapLocation) {
        PlanetMap initialMap = gc.startingMap(mapLocation.getPlanet());
        if (initialMap.onMap(mapLocation) && initialMap.isPassableTerrainAt(mapLocation) > 0) {
            if (gc.canSenseLocation(mapLocation)) {
                if (gc.hasUnitAtLocation(mapLocation)) {
                    if (gc.senseUnitAtLocation(mapLocation).unitType() != UnitType.Factory ||
                            gc.senseUnitAtLocation(mapLocation).unitType() != UnitType.Rocket) {
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return true;
            }

        }

        return false;
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
                    // System.out.println("Map Location: " + mapLocation.toString());
                    return gc.senseUnitAtLocation(mapLocation);
                } else {
                    System.out.println("NO UNIT AT LOCATION");
                    return null;
                }
            } else {
                System.out.println("LOCATION IS NOT IN VIEW RANGE");
                return null;
            }
        }

        System.out.println("LOCATION IS NOT ON THE MAP");
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
                System.out.println("LOCATION IS NOT IN THE SENSE ZONE");
                return -1;
            }
        }

        System.out.println("LOCATION IS NOT ON THE MAP");
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
            directions.add( Direction.swigToEnum(i));
        }
        return directions;
    }

//    /**
//     * Method that will check if a location appears empty. Checks if the location is onMap, passableTerrain,
//     * and if it is not occupied by a factory or rocket. Return true if there is a robot there
//     * @param map The map to check
//     * @param location The location to check
//     * @return If the location appears empty
//     */
//    public static boolean doesLocationAppearEmpty(PlanetMap map, MapLocation location) {
//        if (map.onMap(location) && map.isPassableTerrainAt(location) > 0) {
//            if (Player.gc.hasUnitAtLocation(location)) {
//
//                UnitType unit = Player.gc.senseUnitAtLocation(location).unitType();
//                if (unit == UnitType.Factory || unit == UnitType.Rocket) {
//                    return false;
//                }
//            }
//
//            return true;
//        }
//
//        return false;
//    }

    /**
     * Method that will convert a MapLocation into an easily recognizable string.
     * @param mapLocation The MapLocation that you want to convert
     * @return A string that represents the MapLocation
     */
    public static String mapLocationToString(MapLocation mapLocation) {
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
    public static MapLocation stringToMapLocation(String location) {
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
    public static void setStructureLocations(int maxNonPassable, int radius) {

        MapLocation workerLocation = null;
        int minDistance = -1;
        // find worker with smallest sum to other workers, implementation inefficnet but max 3 workers so not big concern
        VecUnit myWorkers = Player.gc.units();
        // System.out.println(myWorkers);
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
        // System.out.println(workerLocation);

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
     * @param map The map to check
     * @param location The location to check
     * @return If the location appears empty
     */
    public static boolean isLocationEmpty(PlanetMap map, MapLocation location) {
        if (map.onMap(location) && map.isPassableTerrainAt(location) > 0) {
            if (Player.gc.hasUnitAtLocation(location)) {
                return false;
            }
            return true;
        }

        return false;
    }
}


