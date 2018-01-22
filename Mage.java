public class Mage extends Attacker{

    public Mage(int id) {
        super(id);
    }

    @Override
    public void run() {
        runBattleAction();
    }

    @Override
    public boolean runBattleAction() {
        return false;
    }
}
