package planets;

import bc.VecUnit;
import commandsAndRequests.Globals;
import robots.Robot;
import commandsAndRequests.Task;
import structures.Blueprint;
import structures.Factory;
import structures.Rocket;

import java.util.HashMap;

public class Earth {
    public static HashMap<Integer, Task> earthTaskMap = new HashMap<>();
    public static HashMap<Integer, Task> earthAttackTargetsMap = new HashMap<>();
    public static HashMap<Integer, Task> earthProduceRobotMap = new HashMap<>();

    public static HashMap<Integer, Blueprint> earthBlueprintMap = new HashMap<>();
    public static HashMap<Integer, Rocket> earthRocketMap = new HashMap<>();
    public static HashMap<Integer, Robot> earthWorkerMap = new HashMap<>();
    public static HashMap<Integer, Factory> earthFactoryMap = new HashMap<>();
    public static HashMap<Integer, Robot> earthAttackerMap = new HashMap<>();

    public void execute() {
        removeDeadUnits();

        // Execute blueprints, Rockets, Workers, Factories, Attackers
        for (int blueprintId: earthBlueprintMap.keySet()) {

        }
    }

    /**
     * Since the method has not yet been implemented in the API, we must manually check if any unit died
     * last round
     */
    private void removeDeadUnits() {
        VecUnit units = Globals.gameController.myUnits();
        for (int blueprintId: earthBlueprintMap.keySet()) {

        }
    }


}
