import bc.MapLocation;

public class AttackTarget {

    private int targetId;
    private MapLocation targetLocation;

    public AttackTarget(int targetId, MapLocation targetLocation) {
        this.targetId = targetId;
        this.targetLocation = targetLocation;
    }

    public int getTargetId() {
        return targetId;
    }

    public MapLocation getTargetLocation() {
        return targetLocation;
    }
}
