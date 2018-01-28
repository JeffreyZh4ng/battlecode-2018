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
        Team otherTeam = Player.team == Team.Blue ? Team.Red : Team.Blue;
        VecUnit enemyUnits = Player.gc.senseNearbyUnitsByTeam(this.getLocation(), getVisionRange(), otherTeam);

        if (enemyUnits.size() == 0) {
            return true;
        }

        if (Player.gc.isAttackReady(this.getId())) {
            Unit closestUnit = this.getClosestEnemy(enemyUnits);
            int closestDistanceToUnit = (int)this.getLocation().distanceSquaredTo(closestUnit.location().mapLocation());

            if (closestDistanceToUnit > this.getAttackRange()) {
                if (Player.gc.isMoveReady(this.getId())) {
                    move(closestUnit.location().mapLocation());
                }
            }

            if (Player.gc.canAttack(this.getId(), closestUnit.id())) {
                Player.gc.attack(this.getId(), closestUnit.id());
            }
        }

        // Recalculate the closest unit in case you can javelin in this turn too
        Unit closestUnit = this.getClosestEnemy(enemyUnits);
        if (Player.gc.canJavelin(this.getId(), closestUnit.id())) {
            Player.gc.javelin(this.getId(), closestUnit.id());
        }

        return false;
    }
}
