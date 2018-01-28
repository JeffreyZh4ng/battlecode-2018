import bc.MapLocation;
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
        if (Player.gc.isAttackReady(this.getId())) {
            MapLocation enemyTargetLocation = Player.gc.unit(this.getFocusedTargetId()).location().mapLocation();
            int distanceToTarget = (int)(this.getLocation().distanceSquaredTo(enemyTargetLocation));

            if (distanceToTarget > this.getAttackRange()) {
                if (Player.gc.isMoveReady(this.getId())) {
                    System.out.println("Attacker " + this.getId() + " moved forwards in combat");
                    this.inCombatMove(true, enemyTargetLocation);
                }
            }

            if (Player.gc.canAttack(this.getId(), this.getFocusedTargetId())) {
                Player.gc.attack(this.getId(), this.getFocusedTargetId());
                System.out.println("Attacker: " + this.getId() + " attacked enemy unit " + this.getFocusedTargetId());
            }
        }
        
        return false;
    }
}


