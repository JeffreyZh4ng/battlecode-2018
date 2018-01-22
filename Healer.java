import bc.Team;
import bc.Unit;
import bc.VecUnit;

public class Healer extends Attacker{

    public Healer(int id) {
        super(id);
    }

    @Override
    public boolean runBattleAction() {
        return false;
    }

    @Override
    public void run() {
        runBattleAction();
    }

//    @Override
//    public boolean runBattleAction() {
//        VecUnit friendlyUnits = Player.gc.senseNearbyUnitsByTeam(this.getLocation(), getVisionRange(), Player.team);
//
//        if (friendlyUnits.size() == 0) {
//            return true;
//        }
//
//        if (Player.gc.isHealReady(this.getId())) {
//            Unit lowestHealthUnit = getLowestHealthFriendly(friendlyUnits);
//            int distanceToLowestHealthUnit = (int)(this.getLocation().distanceSquaredTo(lowestHealthUnit.location().mapLocation()));
//
//            if (closestDistanceToUnit > this.getAttackRange()) {
//                if (Player.gc.isMoveReady(this.getId())) {
//                    move(this.getId(), closestUnit.location().mapLocation());
//                }
//            }
//
//            if (Player.gc.canHeal(this.getId(), closestUnit.id())) {
//                Player.gc.heal(this.getId(), closestUnit.id());
//            }
//        }
//
//        return false;
//    }
//
//    private Unit getLowestHealthFriendly(VecUnit units) {
//        if (units.size() == 0) {
//            System.out.println("unit list was empty");
//            return null;
//        }
//        Unit minDistanceUnit = null;
//        int closestDistanceToUnit = -1;
//        for (int i = 0; i < units.size(); i++) {
//            int distanceToUnit = (int)(this.getLocation().distanceSquaredTo(units.get(i).location().mapLocation()));
//            if ((minDistanceUnit == null && minSquaredRadius < distanceToUnit) ||
//                    (minDistanceUnit != null && distanceToUnit < closestDistanceToUnit)) {
//                closestDistanceToUnit = distanceToUnit;
//                minDistanceUnit = units.get(i);
//            }
//        }
//        return minDistanceUnit;
//    }
}
