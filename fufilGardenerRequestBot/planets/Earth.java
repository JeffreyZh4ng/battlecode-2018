package planets;
import robots.Robot;
import commandsAndRequests.Task;
import structureStuff.Blueprint;
import structureStuff.Factory;
import structureStuff.Rocket;

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
        // Execute blueprints, Rockets, Workers, Factories, Attackers

    }
}
