import bc.*;

public class Ranger extends Attacker {

    private static final int MIN_ATTACK_RANGE = 10;

    public Ranger(int id) {
        super(id);
    }

    public void run() {
        runAttacker();
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
            Unit closestUnit = this.getClosestEnemy(enemyUnits);
            int closestDistanceToUnit = (int)this.getLocation().distanceSquaredTo(closestUnit.location().mapLocation());

            if (closestDistanceToUnit > this.getAttackRange()) {
                if (Player.gc.isMoveReady(this.getId())) {
                    move(closestUnit.location().mapLocation());
                }
            }

            if (closestDistanceToUnit < MIN_ATTACK_RANGE) {
                // TODO: try move back could be better then this
                //Player.gc.moveRobot(this.getId(), closestUnit.location().mapLocation().directionTo(this.getLocation()));
                closestUnit = this.getClosestEnemy(Player.gc.senseNearbyUnitsByTeam(this.getLocation(), getAttackRange(), otherTeam));
            }

            if (Player.gc.canAttack(this.getId(), closestUnit.id())) {
                Player.gc.attack(this.getId(), closestUnit.id());
            }
        }

        return false;
    }
}
