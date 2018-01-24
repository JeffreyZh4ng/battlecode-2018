import bc.Team;
import bc.Unit;
import bc.VecUnit;

public class Healer extends Attacker {

    public Healer(int id) {
        super(id);
    }

    @Override
    public void run() {
        runBattleAction();
    }

    @Override
    public boolean runBattleAction() {
        VecUnit friendlyUnits = Player.gc.senseNearbyUnitsByTeam(this.getLocation(), getAttackRange(), Player.team);

        if (friendlyUnits.size() == 0) {
            friendlyUnits = Player.gc.senseNearbyUnitsByTeam(this.getLocation(), getVisionRange(), Player.team);
        }
        if (friendlyUnits.size() == 0) {
            return false;
        }

        if (Player.gc.isHealReady(this.getId())) {
            Unit lowestHealthUnit = getLowestHealthFriendly(friendlyUnits);
            int distanceToLowestHealthUnit = (int)(this.getLocation().distanceSquaredTo(lowestHealthUnit.location().mapLocation()));

            if (distanceToLowestHealthUnit > this.getAttackRange()) {
                if (Player.gc.isMoveReady(this.getId())) {
                    move(lowestHealthUnit.location().mapLocation());
                }
            }

            if (Player.gc.canHeal(this.getId(), lowestHealthUnit.id())) {
                Player.gc.heal(this.getId(), lowestHealthUnit.id());
            }
        }

        return false;
    }

    /**
     * Given a VecUnit of units, will return the unit with the lowest health
     * @param units the units to compare
     * @return the weakest of the given units
     */
    private Unit getLowestHealthFriendly(VecUnit units) {
        if (units.size() == 0) {
            // System.out.println("unit list was empty");
            return null;
        }
        Unit weakestUnit = null;
        int weakestUnitHealth = -1;
        for (int i = 0; i < units.size(); i++) {
            int unitHealth = (int)units.get(i).health();
            if ((weakestUnit == null  || unitHealth < weakestUnitHealth)) {
                weakestUnitHealth = unitHealth;
                weakestUnit = units.get(i);
            }
        }
        return weakestUnit;
    }
}
