package ProductionQueue;

import bwapi.Unit;
import bwapi.UnitType;
import main.BaseAPI;

import java.util.List;

/**
 * Specialisation class for the production order to make a unit or building
 */
public class UnitBuildingOrder extends ProductionOrder {

    UnitType toProduce;
    Unit startedUnit;

    public UnitBuildingOrder(UnitType unitBuildingToProduce, int priority)
    {
        this.priority = priority;
        this.toProduce = unitBuildingToProduce;
        this.orderTime = BaseAPI.getGameTime();
        this.status = OrderStatus.commissioned;
    }

    public UnitType getToProduce() {return toProduce;}

    /**
     * Set the unit that is being produced in the order. Used to check if it is finished.
     * @param unitIn The unit that has just started for this order.
     */
    public void setStartedUnit(Unit unitIn) {this.startedUnit = unitIn;}

    /**
     * Get the unit that is being produced or built.
     * @return The unit that is being produced or built.
     */
    public Unit getStartedUnit() {return startedUnit;}

    @Override
    public String toString()
    {
        return "Create: Unit/Building - " + toProduce.toString() + ", Priority - " + priority + ", Time - " + orderTime + ", Status - " + status.toString();
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

    public boolean enoughSupply()
    {
        if (BaseAPI.baseAPI.self.supplyUsed()+toProduce.supplyRequired() <= BaseAPI.baseAPI.self.supplyTotal()){
            return true;
        }
        return false;
    }



    @Override
    public boolean isToProduceFree()
    {
        if (this.toProduce.whatBuilds().first.isWorker())
        {
            //producing building that requires worker
            return true;
        }

        List<Unit> allUnits = BaseAPI.baseAPI.self.getUnits();
        for (Unit currentUnit : allUnits) {
            if (currentUnit.getType() == this.toProduce.whatBuilds().first) {
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
        return false;
    }

    @Override
    public boolean checkHasFinished()
    {
        if (startedUnit.isCompleted())
        {
            setStatus(OrderStatus.finished);
            return true;
        }
        return false;
    }

    /**
     * Check to see if the unit that was being built still exists. This will be false if the building creating it was destroyed or the unit was cancelled manually.
     * @return True if the unit is still being built. False if it isn't
     */
    public boolean checkIfStillTraining()
    {
        if(!startedUnit.exists()){
            this.setStatus(OrderStatus.commissioned);
            return false;
        }
        return true;
    }
}
