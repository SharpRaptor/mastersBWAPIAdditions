package ProductionQueue;


import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import main.BaseAPI;
import ProductionQueue.ProductionOrder.OrderStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * PRODUCTION QUEUE
 * Holds all production orders that are requested of it. Orders will be sorted by priority first, then by time ordered.
 * Each frame will go through list of orders to find which are ready to be executed and will pass back a ready order.
 *
 * REQUIRED USER BOT CALLS
 * onGameStart: Call onGameStart
 * onFrame: Call Update
 * onUnitCreate: Call unitStructureStarted
 * onUnitMorph: Call unitStructureStarted
 * onUnitDestroy: Call unitStructureDestroyed
 */
public class ProductionQueue {
    private ArrayList<ProductionOrder> allOrders;

    private boolean debugMessagesOn;
    private boolean debugOnScreen;

    public ProductionQueue(boolean debugMessagesOn, boolean debugOnScreen){
        this.debugMessagesOn = debugMessagesOn;
        this.debugOnScreen = debugOnScreen;
    }

    /**
     * Basic setup of the ProductionQueue that needs to be run at game start.
     * Would be in the constructor but due to the way that BWAPI initialises, some things should be done on game start, after the StarCraft engine has setup.
     */
    public void onGameStart()
    {
        allOrders = new ArrayList<ProductionOrder>();
    }


    /**
     * Update of the ProductionQueue. Checks are run to see if an order is ready.
     * Any that are ready for execution will be returned for the user to execute how they want.
     * @return ProductionOrder : ProductionOrder that is ready for execution. NULL if nothing.
     */
    public ProductionOrder Update()
    {
        if (debugOnScreen) {
            StringBuilder displayCommands = new StringBuilder("Commands:\n");
            for (ProductionOrder currentOrder : allOrders) {
                displayCommands.append(currentOrder.toString()).append("\n");
            }
            BaseAPI.baseAPI.game.drawTextScreen(10, 10, displayCommands.toString());
        }

        boolean itemRemoved = false;
        ArrayList<ProductionOrder> toRemove = new ArrayList<>();
        for (ProductionOrder currentOrder : allOrders)
        {
            if (currentOrder.getStatus() == OrderStatus.started) {
                if (currentOrder.checkHasFinished()) {
                    toRemove.add(currentOrder);
                    debugMessage("FINISHED: " + currentOrder.toString());
                }
                else if(currentOrder instanceof UnitBuildingOrder)
                {
                    if (!((UnitBuildingOrder) currentOrder).checkIfStillTraining())
                    {
                        debugMessage("ORDER RESET: " + currentOrder.toString());
                    }
                }
            }
        }
        for (ProductionOrder currentToRemove : toRemove)
        {
            itemRemoved = true;
            allOrders.remove(currentToRemove);
        }
        if (itemRemoved)
            sortQueue();


        for (ProductionOrder currentOrder : allOrders)
        {
            if(currentOrder.getStatus() == OrderStatus.ordered)
            {
                if(!currentOrder.checkHasStarted())
                    return null;
                else
                    debugMessage("STARTED: " + currentOrder.toString());
            }
        }

        for (ProductionOrder currentOrder : allOrders) {
            if (currentOrder.getStatus() != OrderStatus.commissioned)
                continue;

            if (!checkHaveAllPrereq(currentOrder))
                continue;

            if (!currentOrder.isToProduceFree())
                continue;

            if (currentOrder instanceof UnitBuildingOrder){
                if (!((UnitBuildingOrder) currentOrder).enoughSupply())
                    continue;
            }

            if (!currentOrder.canAfford())
                return null;



            currentOrder.setStatus(OrderStatus.ordered);
            return currentOrder;



            /*
            if(currentOrder.getStatus() == OrderStatus.commissioned) {
                if (checkHaveAllPrereq(currentOrder)){
                    if(currentOrder.canAfford()) {
                        if (currentOrder.isToProduceFree()) {
                            currentOrder.setStatus(OrderStatus.ordered);
                            return currentOrder;
                        } else {
                            //System.out.println("Building not available");
                            break;
                        }
                    } else {
                        //System.out.println("Cant afford");
                        break;
                    }
                }
                else
                {
                    //System.out.println("Don't have all prereq");
                }
            }
            */
        }
        return null;
    }

    /**
     * Add a unit or a building to the ProductionQueue. Will automatically add all prerequisite structures that aren't owned to the queue.
     * @param toBuild The Unit or Building wanted
     * @param priority The Priority level of this order. The Higher the number, the higher the priority.
     */
    public void AddToQueue(UnitType toBuild, int priority)
    {
        AddToQueue(toBuild, priority,true);
    }


    /**
     * Add a unit or building to the ProductionQueue, with the option of not automatically adding all prerequisite structures to the queue.
     * @param toBuild The Unit or Building wanted.
     * @param priority The Priority level of this order. The Higher the number, the higher the priority.
     * @param autoBuildPrereq True will automatically build all prerequisite structures not currently owned. False will only add this order to the queue.
     */
    public void AddToQueue(UnitType toBuild, int priority,  boolean autoBuildPrereq)
    {
        debugMessage("Beginning Add Unit: " + toBuild.toString());
        if (autoBuildPrereq)
        {
            for(UnitType currentReq : getAllPrereq(toBuild))
            {
                if(!checkIfPlayerHasUnitBuilding(currentReq) && (!checkIfHaveInProduction(currentReq))) {
                    AddToQueue(currentReq, priority, true);
                }
            }
        }
        allOrders.add(new UnitBuildingOrder(toBuild,priority));
        debugMessage("Added To List: " + toBuild.toString());
        sortQueue();
    }

    /**
     * Add a research to the ProductionQueue. Will automatically add all prerequisite structures that aren't owned to the queue.
     * @param toResearch The Technology to be researched
     * @param priority The Priority level of this order. The Higher the number, the higher the priority.
     */
    public void AddToQueue(TechType toResearch, int priority)
    {
        AddToQueue(toResearch, priority, true);
    }

    /**
     * Add a research to the ProductionQueue, with the option of not automatically adding all prerequisite structures to the queue.
     * @param toResearch The technology to be researched
     * @param priority The Priority level of this order. The Higher the number, the higher the priority.
     * @param autoBuildPrereq True will automatically build all prerequisite structures not currently owned. False will only add this order to the queue.
     */
    public void AddToQueue(TechType toResearch, int priority, boolean autoBuildPrereq)
    {
        debugMessage("Beginning Add Research");
        if (!BaseAPI.baseAPI.self.hasResearched(toResearch) && !checkIfHaveInProduction(toResearch)) {
            if (autoBuildPrereq) {
                for(UnitType currentReq : getAllPrereq(toResearch))
                {
                    if(!checkIfPlayerHasUnitBuilding(currentReq) && (!checkIfHaveInProduction(currentReq))) {
                        AddToQueue(currentReq, priority, true);
                    }
                }
            }
            allOrders.add(new ResearchOrder(toResearch, priority));
            debugMessage("Added To List: " + toResearch.toString());
            sortQueue();
        }
        else {
            debugMessage("Upgrade Already Researched or in the queue");
        }
    }

    /**
     * Add a upgrade to the ProductionQueue. Will automatically add all prerequisite structures that aren't owned to the queue.
     * @param toUpgrade The Upgrade to be researched
     * @param priority The Priority level of this order. The Higher the number, the higher the priority.
     * @param level The level of the upgrade to be researched. Level 1 by default and for non tiered upgrades.
     */
    public void AddToQueue(UpgradeType toUpgrade, int priority, int level) {AddToQueue(toUpgrade, priority, level, true);}

    /**
     * Add a upgrade to the ProductionQueue, with the option of not automatically adding all prerequisite structures to the queue.
     * @param toUpgrade The upgrade to be researched
     * @param priority The Priority level of this order. The Higher the number, the higher the priority.
     * @param autoBuildPrereq True will automatically build all prerequisite structures not currently owned. False will only add this order to the queue.
     * @param level The level of the upgrade to be researched. Level 1 by default for non tiered Upgrades
     */
    public void AddToQueue(UpgradeType toUpgrade, int priority, int level, boolean autoBuildPrereq)
    {
        debugMessage("Beginning Add Research");
        if ((!(BaseAPI.baseAPI.self.getUpgradeLevel(toUpgrade) == level)) && (!checkIfHaveInProduction(toUpgrade,level))) { /* If the upgrade is not already at the level requested and it isn't already in the queue */
            if (autoBuildPrereq) { /* If requested, add all the prerequisites that are not met yet */
                for(UnitType currentReq : getAllPrereq(toUpgrade, level))
                {
                    if(!checkIfPlayerHasUnitBuilding(currentReq) && (!checkIfHaveInProduction(currentReq))) {
                        AddToQueue(currentReq, priority, true);
                    }
                }
            }
            allOrders.add(new UpgradeOrder(toUpgrade, priority, level));
            debugMessage("Added To List: " + toUpgrade.toString());
            sortQueue();
        }
        else
        {
            debugMessage("Upgrade Already at that level or in the queue");
        }
    }



    /**
     * Remove a unit or building from the ProductionQueue
     * @param toRemove The Unit or Building to be cancelled
     * @param removeAll True will remove all of that type from the queue. False will remove only the first one found starting with the lowest priority.
     */
    public void RemoveFromQueue(UnitType toRemove, boolean removeAll)
    {
        debugMessage("Removing: " + toRemove.toString());
        ArrayList<ProductionOrder> removeList = new ArrayList<>();

        Collections.reverse(allOrders);

        for (ProductionOrder currentOrder : allOrders) {
            if (currentOrder instanceof UnitBuildingOrder)
            {
                if(((UnitBuildingOrder) currentOrder).getToProduce() == toRemove)
                {
                    removeList.add(currentOrder);
                    if (!removeAll) {
                        break;
                    }
                }
            }
        }
        for (ProductionOrder currentRemove : removeList)
        {
            allOrders.remove(currentRemove);
        }
        sortQueue();
    }

    /**
     * Remove a research from the ProductionQueue
     * @param toRemove the technology research to remove.
     */
    public void RemoveFromQueue(TechType toRemove)
    {
        debugMessage("Removing: " + toRemove.toString());
        boolean haveFound = false;
        ProductionOrder foundOrder = null;

        for (ProductionOrder currentOrder : allOrders) {
            if (currentOrder instanceof UnitBuildingOrder)
            {
                if(((ResearchOrder) currentOrder).getToProduce() == toRemove)
                {
                    haveFound = true;
                    foundOrder = currentOrder;
                    break;
                }
            }
        }

        if (haveFound)
            allOrders.remove(foundOrder);

        sortQueue();
    }

    /**
     * Remove a upgrade from the ProductionQueue
     * @param toRemove the upgrade research to remove.
     */
    public void RemoveFromQueue(UpgradeType toRemove)
    {
        debugMessage("Removing: " + toRemove.toString());
        boolean haveFound = false;
        ProductionOrder foundOrder = null;

        for (ProductionOrder currentOrder : allOrders) {
            if (currentOrder instanceof UnitBuildingOrder)
            {
                if(((UpgradeOrder) currentOrder).getToProduce() == toRemove)
                {
                    haveFound = true;
                    foundOrder = currentOrder;
                    break;
                }
            }
        }

        if (haveFound)
            allOrders.remove(foundOrder);
        sortQueue();
    }

    /**
     * Informs the queue that a unit has been started. The unit reference will be stored by the order
     * @param startedUnit The started unit
     */
    public void unitStructureStarted(Unit startedUnit)
    {
        if (startedUnit.getPlayer() == BaseAPI.baseAPI.self) {
            for (ProductionOrder currentOrder : allOrders) {
                if (currentOrder instanceof UnitBuildingOrder) {
                    if ((startedUnit.getType() == ((UnitBuildingOrder) currentOrder).getToProduce()) && (currentOrder.getStatus() == OrderStatus.ordered)) {
                        currentOrder.setStatus(OrderStatus.started);
                        ((UnitBuildingOrder) currentOrder).setStartedUnit(startedUnit);
                        debugMessage("STARTED: " + currentOrder.toString());
                        return;
                    }
                }
            }
        }
    }

    /**
     * Informs the queue that a building was destroyed. It will then go through and find out if that was one of the ones that was either under construction for an order.
     * Or if it was producing something for an order. If it was then it will set the associated order to commissioned again.
     * @param destroyedBuilding The destroyed unit
     */
    public void unitStructureDestroyed(Unit destroyedBuilding)
    {
        if ((destroyedBuilding.getPlayer() == BaseAPI.baseAPI.self)&&(destroyedBuilding.getType().isBuilding())) {
            if (!destroyedBuilding.isCompleted())
            {
                debugMessage("Building was being constructed: " + destroyedBuilding.getType().toString());
                for (ProductionOrder currentOrder : allOrders)
                {
                    if (currentOrder instanceof UnitBuildingOrder)
                    {
                        if (destroyedBuilding == ((UnitBuildingOrder) currentOrder).getStartedUnit()) {
                            currentOrder.setStatus(OrderStatus.commissioned);
                            debugMessage("ORDER RESET: " + currentOrder.toString());
                            return;
                        }
                    }
                }
            }

            if (destroyedBuilding.isResearching())
            {
                debugMessage("Building was researching: " + destroyedBuilding.getTech().toString());
                for (ProductionOrder currentOrder : allOrders)
                {
                    if (currentOrder instanceof ResearchOrder)
                    {
                        if (((ResearchOrder) currentOrder).toProduce == destroyedBuilding.getTech())
                        {
                            currentOrder.setStatus(OrderStatus.commissioned);
                            debugMessage("ORDER RESET: " + currentOrder.toString());
                            return;
                        }
                    }
                }
            }
            if (destroyedBuilding.isUpgrading())
            {
                debugMessage("Building was upgrading: " + destroyedBuilding.getUpgrade().toString());
                for (ProductionOrder currentOrder : allOrders)
                {
                    if (currentOrder instanceof UpgradeOrder)
                    {
                        if (((UpgradeOrder) currentOrder).toProduce == destroyedBuilding.getUpgrade())
                        {
                            currentOrder.setStatus(OrderStatus.commissioned);
                            debugMessage("ORDER RESET: " + currentOrder.toString());
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Sorts the Construction Queue by priority, then by time. Oldest first.
     */
    private void sortQueue()
    {
        Collections.sort(allOrders);
        debugMessage("Queue Sorted");
        for (ProductionOrder currentOrder : allOrders)
        {
            debugMessage(currentOrder.toString());
        }
    }

    /**
     * Runs a check to see if the player owns at least one of the given type of unit or building
     * @param toCheck The type to check if the player owns
     * @return Boolean : True if the player owns at least one. False if they don't own any.
     */
    private boolean checkIfPlayerHasUnitBuilding(UnitType toCheck)
    {
        for (Unit unit : BaseAPI.baseAPI.self.getUnits())
        {
            if ((unit.getType() == toCheck) && (unit.isCompleted()))
                return true;
        }
        return false;
    }


    /**
     * Runs a check to see if the player currently has one of this unit or building in the production queue
     * @param toCheck The unit or building to check
     * @return True if one is in the queue. False if it isn't.
     */
    public boolean checkIfHaveInProduction(UnitType toCheck)
    {
        for (ProductionOrder order : allOrders)
        {
            if (order instanceof UnitBuildingOrder)
            {
                if (((UnitBuildingOrder)order).getToProduce() == toCheck)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Runs a check of the production queue to see how many of a certain unit are currently in the queue
     * @param toCheck The unit type to check how many are there
     * @return The integer value of how many are in the queue
     */
    public int checkHowManyInProduction(UnitType toCheck)
    {
        int toReturn = 0;
        for (ProductionOrder order : allOrders) {
            if (order instanceof UnitBuildingOrder) {
                if (((UnitBuildingOrder) order).getToProduce() == toCheck) {
                    toReturn++;
                }
            }
        }
        return toReturn;
    }

    /**
     * Runs a check to see if the player currently has one of this upgrade in the production queue
     * @param toCheck The research to check
     * @return True if one is in the queue. False if it isn't.
     */
    public boolean checkIfHaveInProduction(TechType toCheck)
    {
        for (ProductionOrder order : allOrders)
        {
            if (order instanceof ResearchOrder)
            {
                if (((ResearchOrder)order).getToProduce() == toCheck)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Runs a check to see if the player currently has one of this upgrade in the production queue
     * @param toCheck The upgrade to check
     * @return True if one is in the queue. False if it isn't.
     */
    public boolean checkIfHaveInProduction(UpgradeType toCheck, int level)
    {
        for (ProductionOrder order : allOrders)
        {
            if (order instanceof UpgradeOrder)
            {
                UpgradeOrder tempUpgradeOrder = (UpgradeOrder)order;
                if ((tempUpgradeOrder.getToProduce() == toCheck) && (tempUpgradeOrder.getUpgradeLevel() == level))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets all the prerequisites for building a unit or building
     * @param toCheck the UnitType to ge the prerequisites for
     * @return ArrayList of UnitTypes containing all prerequisites
     */
    private ArrayList<UnitType> getAllPrereq(UnitType toCheck)
    {
        Set<UnitType> toReturn = new HashSet<>();
        if (toCheck.whatBuilds().first!= UnitType.None)
            toReturn.add(toCheck.whatBuilds().first);

        for (UnitType requirement : toCheck.requiredUnits().keySet())
        {
            if (requirement != UnitType.None)
                toReturn.add(requirement);
        }

        return new ArrayList<>(toReturn);
    }

    /**
     * Gets all the prerequisites for researching a technology
     * @param toCheck the TechType to ge the prerequisites for
     * @return ArrayList of UnitTypes containing all prerequisites
     */
    private ArrayList<UnitType> getAllPrereq(TechType toCheck)
    {
        Set<UnitType> toReturn = new HashSet<>();
        if (toCheck.whatResearches()!= UnitType.None)
            toReturn.add(toCheck.whatResearches());
        if (toCheck.requiredUnit()!= UnitType.None)
            toReturn.add(toCheck.requiredUnit());

        return new ArrayList<>(toReturn);
    }


    /**
     * Gets all the prerequisites for researching a technology
     * @param toCheck the TechType to ge the prerequisites for
     * @return ArrayList of UnitTypes containing all prerequisites
     */
    private ArrayList<UnitType> getAllPrereq(UpgradeType toCheck, int level)
    {
        Set<UnitType> toReturn = new HashSet<>();
        if (toCheck.whatUpgrades()!= UnitType.None)
            toReturn.add(toCheck.whatUpgrades());
        if (toCheck.whatsRequired()!= UnitType.None)
            toReturn.add(toCheck.whatsRequired(level));

        return new ArrayList<>(toReturn);
    }


    /**
     * Checks to see if the player has all the prerequisites listed
     * @param orderToCheck The Production order to check the player has the prerequisites for
     * @return true if player has them all. False if they don't
     */
    private boolean checkHaveAllPrereq(ProductionOrder orderToCheck)
    {

        if (orderToCheck instanceof UnitBuildingOrder)
        {
            for(UnitType currentReq : getAllPrereq(((UnitBuildingOrder) orderToCheck).getToProduce()))
            {
                if (currentReq != UnitType.None) {
                    if (!checkIfPlayerHasUnitBuilding(currentReq)) {
                        //System.out.println("Still need" + currentReq.toString());
                        return false;
                    }
                }
            }
            return true;
        }
        if (orderToCheck instanceof ResearchOrder)
        {
            for(UnitType currentReq : getAllPrereq(((ResearchOrder) orderToCheck).getToProduce()))
            {
                if (currentReq != UnitType.None) {
                    if (!checkIfPlayerHasUnitBuilding(currentReq)) {
                        //System.out.println("Still need" + currentReq.toString());

                        return false;
                    }
                }
            }
            return true;
        }
        if (orderToCheck instanceof UpgradeOrder)
        {
            UpgradeOrder tempOrder = (UpgradeOrder)orderToCheck;
            for(UnitType currentReq : getAllPrereq(tempOrder.getToProduce(),tempOrder.getUpgradeLevel()))
            {
                if (currentReq != UnitType.None) {
                    if (!checkIfPlayerHasUnitBuilding(currentReq)) {
                        //System.out.println("Still need" + currentReq.toString());
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void debugMessage(String message){
        if (debugMessagesOn)
            System.out.println("PRODUCTION QUEUE: " + message);
    }

}
