import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class Mars {

    public static HashMap<Integer, GlobalTask> marsTaskMap = new HashMap<>();
    public static Queue<GlobalTask> marsTaskQueue = new LinkedList<>();

    public static HashMap<Integer, UnitInstance> marsRocketMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> marsWorkerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> marsAttackerMap = new HashMap<>();

    public static HashSet<Integer> marsFinishedTasks = new HashSet<>();

    public static HashMap<Integer, UnitInstance> marsStagingWorkerMap = new HashMap<>();
    public static HashMap<Integer, UnitInstance> marsStagingAttackerMap = new HashMap<>();

    public void execute() {

    }
}
