import bc.Team;
import bc.Unit;
import bc.VecUnit;

public class Knight extends Attacker {

    public Knight(int id) {
        super(id);
    }

    public void run() {
        this.runAttacker();
    }

    /**
     * Attacks the weakest enemy that it can, will move towards if unreachable
     * @return true if nothing to attack false if attacked or has enemy in range
     */
    @Override
    public boolean runBattleAction() {
        Team otherTeam = Player.gc.team() == Team.Blue ? Team.Red : Team.Blue;
        VecUnit enemyUnits = Player.gc.senseNearbyUnitsByTeam(this.getLocation(), getVisionRange(), otherTeam);

        if (enemyUnits.size() == 0) {
            return true;
        }

        if (Player.gc.isAttackReady(this.getId())) {
            Unit closestUnit = getClosestUnit(-1, enemyUnits);
            int closestDistanceToUnit = (int)this.getLocation().distanceSquaredTo(closestUnit.location().mapLocation());

            if (closestDistanceToUnit > 2) {
                System.out.println("BJHJHJHJ");
                if (Player.gc.isMoveReady(this.getId())) {
                    System.out.println(closestUnit.location().mapLocation().toString());
                    move(this.getId(), closestUnit.location().mapLocation());
                }
            }

            if (Player.gc.canAttack(this.getId(), closestUnit.id())) {
                Player.gc.attack(this.getId(), closestUnit.id());
            }
        }

        return false;
    }
}
