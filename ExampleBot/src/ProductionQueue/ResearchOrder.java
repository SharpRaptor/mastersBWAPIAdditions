package ProductionQueue;

import bwapi.TechType;
import bwapi.Unit;
import main.BaseAPI;

import java.util.List;

/**
 * Specialization class for a production order to start researching a technology
 */
public class ResearchOrder extends ProductionOrder {

    TechType toProduce;

    public ResearchOrder(TechType techToResearch, int priority)
    {
        this.priority = priority;
        this.toProduce = techToResearch;
        this.orderTime = BaseAPI.getGameTime();
        this.status = OrderStatus.commissioned;
    }

    public TechType getToProduce() {return toProduce;}

    @Override
    public String toString()
    {
        return "Research: Tech - " + toProduce.toString() + ", Priority - " + priority + ", Time - " + orderTime + ", Status - " + status.toString();
    }

    @Override
    public boolean canAfford()
    {
        if ((BaseAPI.baseAPI.self.minerals()>= toProduce.mineralPrice()) &&
                (BaseAPI.baseAPI.self.gas() >= toProduce.gasPrice())){
            return true;
        }

        return false;
    }

    @Override
    public boolean isToProduceFree()
    {
        List<Unit> allUnits = BaseAPI.baseAPI.self.getUnits();
        for (Unit currentUnit : allUnits) {
            if (currentUnit.getType() == this.toProduce.whatResearches()) {
                if (currentUnit.isIdle() && currentUnit.isCompleted()){
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean checkHasStarted()
    {
        if (BaseAPI.baseAPI.self.isResearching(toProduce)) {
            this.setStatus(OrderStatus.started);
            return true;
        }
        return false;
    }

    @Override
    public boolean checkHasFinished()
    {
        if (BaseAPI.baseAPI.self.hasResearched(toProduce)){
            this.setStatus(OrderStatus.finished);
            return true;
        }
        return false;
    }
}
