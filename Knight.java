import bc.Unit;
import bc.VecUnit;

public class Knight extends Attacker {

    public Knight(int id) {
        super(id);
    }

    public void run() {
        this.runAttacker();
    }

    @Override
    public boolean runBattleAction() {
        return false;
    }
}
