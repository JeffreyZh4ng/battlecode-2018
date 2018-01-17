import bc.Direction;
import bc.MapLocation;
import bc.Planet;
import bc.PlanetMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MoveTests {

//    public HashMap<Integer, HashSet<MapLocation>> cleanDepthMap(HashMap<Integer, HashSet<MapLocation>> depthMap,
//                                                        MapLocation Location) {
//
//        for (int i = 0; i < depthMap.size(); i++) {
//            if ()
//        }
//    }

    /**
     * This method will get a depth map of all the shortest paths from an initial MapLocation to a final MapLocation
     * @param initialLocation The initial MapLocation
     * @param finalLocation The final MapLocation
     */
    public static HashMap<Integer, HashSet<MapLocation>> getDepthMap(MapLocation initialLocation, MapLocation finalLocation)
    {
        HashMap<Integer, HashSet<MapLocation>> depthMap = new HashMap<>();

        HashMap<String, Integer> allLocations = new HashMap<>();
        allLocations.put(initialLocation.toString(), 0);

        depthMap = searchForPath(initialLocation, finalLocation, 1, depthMap, allLocations);
        depthMap.remove(-1);

        return depthMap;
    }

    /**
     * A recursive function that will compile all the shortest paths between a given initial and final MapLocation
     * into a depth map. The map contains all points 'x' moves away from the initial position until the final position
     * To find the shortest paths, trace the index of the final position and find adjacent locations with an index of n-1
     * @param initialLocation The MapLocation that you are find a path to the finalLocation from
     * @param finalLocation The final MapLocation of the move
     * @param depthIndex An index that represents the number of moves you are away from the initial MapLocation
     * @param depthMap A map that stores all the paths as moves away from the initial MapLocation
     * @param allLocations A lookup map that keeps track of all the locations you have visited
     * @return A depth map of all the shortest paths
     */
    public static HashMap<Integer, HashSet<MapLocation>> searchForPath(MapLocation initialLocation, MapLocation finalLocation,
                                                                       int depthIndex, HashMap<Integer, HashSet<MapLocation>> depthMap,
                                                                       HashMap<String, Integer> allLocations) {

        ArrayList<MapLocation> nextLocations = getAvailablePositions(initialLocation);

        // If you have reached the destination, remove all the depthMap paths with greater lengths than the current path
        if (initialLocation.isAdjacentTo(finalLocation)) {
            if (depthIndex < depthMap.size()) {
                for (int i = depthIndex; i < depthMap.size(); i++) {
                    depthMap.remove(i + 1);
                }
            }
            allLocations.put(finalLocation.toString(), depthIndex);
            depthMap.put(-1, new HashSet<>());
            System.out.println("BOOP: " + depthIndex);
            return depthMap;
        }

        // If the current depth index is greater than the size of the depthMap, (there exists in our map a
        // path that takes less moves than our current path) return.
        if (depthMap.get(-1) != null && depthIndex >= allLocations.get(finalLocation.toString())) {
            System.out.println("BOP: " + depthIndex);
            return depthMap;
        }

        ArrayList<MapLocation> runSearchForPath = new ArrayList<>();
        HashSet<MapLocation> depthSet;
        if (depthMap.get(depthIndex) == null) {
            depthSet = new HashSet<>();
        } else {
            depthSet = depthMap.get(depthIndex);
        }
        for (MapLocation location: nextLocations) {
            if (!allLocations.containsKey(location.toString()) || depthIndex < allLocations.get(location.toString())) {
                System.out.println("Running for: " + mapLocationToString(location) + " Depth index: " + depthIndex);
                allLocations.put(location.toString(), depthIndex);
                runSearchForPath.add(location);
                depthSet.add(location);
            }
        }
        depthMap.put(depthIndex, depthSet);

        for (MapLocation location: runSearchForPath) {
            if (!allLocations.containsKey(location.toString()) || depthIndex <= allLocations.get(location.toString())) {
                System.out.println("Running recur for : " + mapLocationToString(location));
                depthMap = searchForPath(location, finalLocation, depthIndex + 1, depthMap, allLocations);
            }
        }

        return depthMap;
    }

    /**
     * Method that will give all open adjacent positions to a given MapLocation
     * @param currentLocation The location you want to find adjacent location to
     * @return A list of all available adjacent positions
     */
    public static ArrayList<MapLocation> getAvailablePositions(MapLocation currentLocation) {
        ArrayList<MapLocation> availablePositions = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            MapLocation locationToCheck = currentLocation.add(Direction.swigToEnum(i));
            if (Player.inOccupiable(locationToCheck)) {
                availablePositions.add(locationToCheck);
            }
        }

        return availablePositions;
    }

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

//    /**
//     * A method that will convert the recognizable string back into a MapLocation
//     * @param location The MapLocation represented by the string
//     * @return A MapLocation that represents the string
//     */
//    public static MapLocation stringToMapLocation(String location) {
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
}
