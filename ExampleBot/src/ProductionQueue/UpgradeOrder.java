package ProductionQueue;

import bwapi.Unit;
import bwapi.UpgradeType;
import main.BaseAPI;

import java.util.List;

/**
 * Specialization class for the production order to start researching an upgrade
 */
public class UpgradeOrder extends ProductionOrder {

    UpgradeType toProduce;
    int upgradeLevel;

    public UpgradeOrder(UpgradeType techToResearch, int priority, int level)
    {
        this.priority = priority;
        this.toProduce = techToResearch;
        this.orderTime = BaseAPI.getGameTime();
        this.status = OrderStatus.commissioned;
        this.upgradeLevel = level;
    }

    public UpgradeType getToProduce() {return toProduce;}
    public int getUpgradeLevel() {return upgradeLevel;}

    @Override
    public String toString()
    {
        return "Upgrade: Tech - " + toProduce.toString() + " " + upgradeLevel + ", Priority - " + priority + ", Time - " + orderTime + ", Status - " + status.toString();
    }


    @Override
    public boolean canAfford()
    {
        if ((BaseAPI.baseAPI.self.minerals()>= toProduce.mineralPrice(upgradeLevel)) &&
                (BaseAPI.baseAPI.self.gas() >= toProduce.gasPrice(upgradeLevel))){
            return true;
        }

        return false;
    }


    @Override
    public boolean isToProduceFree()
    {
        List<Unit> allUnits = BaseAPI.baseAPI.self.getUnits();
        for (Unit currentUnit : allUnits) {
            if (currentUnit.getType() == this.toProduce.whatUpgrades()) {
                if (currentUnit.isIdle()&&currentUnit.isCompleted()){
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean checkHasStarted()
    {
        if (BaseAPI.baseAPI.self.isUpgrading(toProduce)) {
            this.setStatus(OrderStatus.started);
            return true;
        }
        return false;

    }

    @Override
    public boolean checkHasFinished()
    {
        if (BaseAPI.baseAPI.self.getUpgradeLevel(toProduce) == upgradeLevel)
        {
            setStatus(OrderStatus.finished);
            return true;
        }
        return false;
    }
}
