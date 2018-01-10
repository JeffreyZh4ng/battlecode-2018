import bc.*;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

public class Player {

    //public static PriorityQueue<Task> karboniteUsageStack = new PriorityQueue<>();
    //public static Stack<Task> emergencyKarboniteUsageStack = new Stack<>();
    public static HashMap<Integer, Worker> earthWorkerHashMap = new HashMap<>();
    public static Queue<Task> karboniteQueue = new PriorityQueue<>();


    public static void main(String[] args) {

        GameController gameController = new GameController();
        addWorkersToHashMap(gameController);
        PlanetMap earthMap = gameController.startingMap(Planet.Earth);
        findKarbonite(earthMap);

        while (true) {
            System.out.println("Current round: " + gameController.round());
            // Task to remove dead units from the HashMaps

            /*VecUnit units = gameController.myUnits();
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);

                // Most methods on gameController take unit IDs, instead of the unit objects themselves.
                Direction randomDirection = pickRandomDirection();
                if (gameController.isMoveReady(unit.id()) && gameController.canMove(unit.id(), randomDirection)) {
                    gameController.moveRobot(unit.id(), randomDirection);
                }
            }*/

            gameController.nextTurn();
        }
    }

    public static void findKarbonite(PlanetMap planetMap) {
        int planetWidth = (int) planetMap.getWidth();
        int planetHeight = (int) planetMap.getHeight();
    }

    /**
     * Method that will add all the workers on earth to the HashMap of workers
     * @param gameController The game controller
     */
    public static void addWorkersToHashMap(GameController gameController) {
        VecUnit units = gameController.myUnits();
        for (int i = 0; i < units.size(); i++) {
            int unitId = units.get(i).id();
            Worker worker = new Worker(unitId, null);
            earthWorkerHashMap.put(unitId, worker);
        }
    }

    /**
     * Method that will return a random direction when called. Intended to be used for preliminary testing
     * @return One of 8 random directions
     */
    public static Direction pickRandomDirection() {
        int randomInt = (int)(Math.random()*8 + 1);
        switch (randomInt) {
            case 1:
                return Direction.North;
            case 2:
                return Direction.Northeast;
            case 3:
                return Direction.East;
            case 4:
                return Direction.Southeast;
            case 5:
                return Direction.South;
            case 6:
                return Direction.Southwest;
            case 7:
                return Direction.West;
            case 8:
                return Direction.Northwest;
            default:
                return Direction.Center;
        }
    }
}