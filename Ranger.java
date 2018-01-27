import bc.*;

import java.util.ArrayList;

public class Ranger extends Attacker {

    private static final int MIN_ATTACK_RANGE = 10;

    public Ranger(int id) {
        super(id);
    }

    @Override
    public void run() {
        runAttacker();
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
            } else if (distanceToTarget < MIN_ATTACK_RANGE) {
                if (Player.gc.isMoveReady(this.getId())) {
                    System.out.println("Attacker " + this.getId() + " moved backwards in combat");
                    this.inCombatMove(false, enemyTargetLocation);
                }
            }

            if (Player.gc.canAttack(this.getId(), this.getFocusedTargetId())) {
                Player.gc.attack(this.getId(), this.getFocusedTargetId());
                System.out.println("Attacker: " + this.getId() + " attacked enemy unit " + this.getFocusedTargetId());
            }
        }

        return false;
    }

    /**
     * Overridden method so that the best target that the ranger finds is not inside of the area the ranger cant target
     * @param enemyUnits The list of enemy units the ranger can see
     */
    @Override
    public void findBestTarget(VecUnit enemyUnits) {
        ArrayList<Integer> enemyUnitIds = new ArrayList<>();
        for (int i = 0; i < enemyUnits.size(); i++) {
            enemyUnitIds.add(enemyUnits.get(i).id());
        }

        // Checks if your focused target is inside your min attack range
        boolean isTargetInsideMinAttackRange = false;
        if (Player.gc.canSenseUnit(this.getFocusedTargetId())) {

            MapLocation enemyUnitLocation = Player.gc.unit(this.getFocusedTargetId()).location().mapLocation();
            if (this.getLocation().distanceSquaredTo(enemyUnitLocation) < MIN_ATTACK_RANGE) {
                isTargetInsideMinAttackRange = true;
            }
        }

        // If your current focused attack target is not within the enemy units in your vision range, or it is
        // within your min attack range, pick a new target
        if (!enemyUnitIds.contains(this.getFocusedTargetId()) || isTargetInsideMinAttackRange) {
            for (int i = 0; i < enemyUnits.size(); i++) {

                int enemyUnitId = enemyUnits.get(i).id();
                MapLocation enemyTargetLocation = Player.gc.unit(enemyUnitId).location().mapLocation();
                int distanceToEnemy = (int)(this.getLocation().distanceSquaredTo(enemyTargetLocation));

                if (Earth.earthFocusedTargets.contains(enemyUnitId) && distanceToEnemy > MIN_ATTACK_RANGE) {
                    this.setFocusedTargetId(enemyUnitId);

                    System.out.println("Attacker: " + this.getId() + " is targeting new enemy unit: " + enemyUnitId);
                    return;
                }
            }

            int enemyId = getClosestEnemy(enemyUnits).id();
            Earth.earthFocusedTargets.add(enemyId);
            this.setFocusedTargetId(enemyId);

            System.out.println("Attacker: " + this.getId() + " creating new focused attack target: " + enemyId);
        }
    }

    /**
     * Method that will get the closest enemy unit in range and takes into account the rangers minimum attack range
     * @param enemyUnits The list of all enemy units in vision range
     * @return The closest unit that the ranger can attack
     */
    @Override
    public Unit getClosestEnemy(VecUnit enemyUnits) {
        Unit closestEnemy = enemyUnits.get(0);
        MapLocation thisUnitLocation = this.getLocation();
        int closestDistance = (int)(thisUnitLocation.distanceSquaredTo(enemyUnits.get(0).location().mapLocation()));

        for (int i = 0; i < enemyUnits.size(); i++) {
            MapLocation enemyUnitLocation = enemyUnits.get(i).location().mapLocation();
            if (thisUnitLocation.distanceSquaredTo(enemyUnitLocation) < closestDistance &&
                    thisUnitLocation.distanceSquaredTo(enemyUnitLocation) > MIN_ATTACK_RANGE) {

                closestEnemy = enemyUnits.get(i);
                closestDistance = (int)(thisUnitLocation.distanceSquaredTo(enemyUnits.get(i).location().mapLocation()));
            }
        }

        return closestEnemy;
    }
}
