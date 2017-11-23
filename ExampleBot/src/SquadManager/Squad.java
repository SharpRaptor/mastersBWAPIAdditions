package SquadManager;


import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import main.BaseAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * SQUAD
 * Holds a list of desired squad make up and a list of assigned units. Allows for mass giving of orders to all units in the squad.
 */
public class Squad {

    private int ID;
    private HashMap<UnitType, Integer> targetSquadMakeup;
    private ArrayList<Unit> assignedUnits;
    private Position targetPosition;
    private Unit targetUnit;
    private Unit rightClickTarget;

    private boolean wasComplete;
    private boolean isDead;

    private boolean debugMessagesOn;

    public Squad(HashMap<UnitType, Integer> squadMakeup, int ID, boolean debugMessagesOn)
    {
        this.debugMessagesOn = debugMessagesOn;
        assignedUnits = new ArrayList<>();
        this.targetSquadMakeup = squadMakeup;
        this.ID = ID;
        wasComplete = false;
        isDead = false;
    }

    /**
     * Get the Unique ID of the squad
     * @return Unique ID number of the squad
     */
    public int getID() {return ID;}

    /**
     * Gets the target squad make up
     * @return HashMap of Target Squad Make up
     */
    public HashMap<UnitType, Integer> getTargetSquadMakeup() {return targetSquadMakeup;}

    /**
     * Gets all the units assigned to the squad
     * @return ArrayList of assigned units
     */
    public ArrayList<Unit> getAssignedUnits() {return assignedUnits;}

    /**
     * Gets the position that the squad was last sent to. Null if the last target was a unit or a right click target
     * @return The position the squad was last sent to
     */
    public Position getTargetPosition() {return targetPosition;}

    /**
     * Gets the unit that the squad was last sent to attack. Null if the last target was a position or a right click target
     * @return The unit the squad was last sent to attack
     */
    public Unit getTargetUnit() {return targetUnit;}

    /**
     * Gets the unit that the squad was to act as if a player had right clicked on them to. Null if the last target was a position or a unit
     * @return The unit that the squad was to act as if a player had right clicked on them to
     * */
    public Unit getRightClickTarget() {return rightClickTarget;}

     /**
     * If the squad was ever complete at some point then this returns as true.
     * @return True if all the squad goals were met. False if not.
     */
    public boolean getWasComplete() {return wasComplete;}

    /**
     * Returns if the squad is dead or not. A squad is considered dead if it currently has no members and was once complete.
     * @return True if squad is dead. False if not
     */
    public boolean getIsDead() {return isDead;}

    /**
     * Gets the HashMap of the disparity between assigned units and the squad target make up.
     * @return HashMap UnitType: Integer of the types of units missing and the amound that are still needed.
     */
    public HashMap<UnitType, Integer> getSquadNeeds()
    {
        int currentCount;
        HashMap<UnitType, Integer> toReturn = new HashMap<>();

        for (Map.Entry<UnitType, Integer> entry : targetSquadMakeup.entrySet())
        {
            currentCount = 0;
            for (Unit currentUnit : assignedUnits) {
                if (currentUnit.getType() == entry.getKey()) {
                    currentCount++;
                }
            }
            if (currentCount < entry.getValue()) {
                debugMessage("Not Enough " + entry.getKey() + ". Needed: " + entry.getValue() + "Have: " + currentCount + ", Requesting: " + (entry.getValue() - currentCount));
                toReturn.put(entry.getKey(), (entry.getValue()-currentCount));
            }
        }

        return toReturn;
    }

    /**
     * Assign a unit to the squad
     * @param unitToAdd The unit to be assigned
     */
    public void addUnitToSquad(Unit unitToAdd)
    {
        assignedUnits.add(unitToAdd);
        debugMessage("Unit Added. Still Need size = " + getSquadNeeds().size());
        if (getSquadNeeds().size() == 0)
            wasComplete = true;
    }

    /**
     * Will Un assign a unit from the squad if it was in there. The unit will no longer receive squad commands
     * @param unitToRemove The unit to remove from the squad.
     */
    public void removeUnitFromSquad(Unit unitToRemove)
    {
        Unit toRemove = null;

        for (Unit currentUnit : assignedUnits) {
            if (currentUnit.getID() == unitToRemove.getID()) {
                toRemove = currentUnit;
                break;
            }
        }

        if(toRemove!=null)
        {
            assignedUnits.remove(toRemove);
            if (assignedUnits.size() == 0){
                if (wasComplete)
                    isDead = true;
            }

        }
    }

    /**
     * Sets the squad move target to a given map position, all units in the squad will move there or as close as they can get. If the attack move option is given then the units will attack move instead.
     * To set the squad target to a unit without making it attack, use this function and pass in the units position.
     * @param movePosition the position to move the squad to.
     * @param attackMove Whether the units should attack enemies they encounter along the way.
     */
    public void setSquadMoveTarget(Position movePosition, boolean attackMove)
    {
        targetPosition = movePosition;
        targetUnit = null;
        rightClickTarget = null;
        for (Unit currentUnit : assignedUnits)
        {
            if (attackMove)
                currentUnit.attack(movePosition);
            else
                currentUnit.move(movePosition);
        }
    }

    /**
     * Set the target of the squad to a unit. They will then go and attack that unit.
     * @param toAttack The unit the squad will attack
     */
    public void setAttackTarget(Unit toAttack)
    {
        targetUnit = toAttack;
        targetPosition = null;
        rightClickTarget = null;
        for (Unit currentUnit : assignedUnits) {
            currentUnit.attack(toAttack);
        }
    }

    /**
     * For each unit give it the command of right clicking on a target
     * @param rightClickTarget The target to right click on
     */
    public void setSquadRightClick(Unit rightClickTarget)
    {
        targetUnit = null;
        targetPosition = null;
        this.rightClickTarget = rightClickTarget;
        for (Unit currentUnit : assignedUnits)
            currentUnit.rightClick(rightClickTarget);
    }





    /**
     * Informs the squad a unit has been destroyed. If it was one from this squad then it will be removed from the squad assigned unit list.
     * @param destroyedUnit The unit that was destroyed
     */
    public void onUnitDestroy(Unit destroyedUnit)
    {
        Unit toRemove = null;
        if (destroyedUnit.getPlayer().getID() == BaseAPI.baseAPI.self.getID()) {
            for (Unit currentUnit : assignedUnits) {
                if (currentUnit.getID() == destroyedUnit.getID()) {
                    toRemove = currentUnit;
                    break;
                }
            }
        }
        if(toRemove!=null)
        {
            assignedUnits.remove(toRemove);
            debugMessage("Unit Killed - " + destroyedUnit.getType());
            if (assignedUnits.size() == 0){
                if (wasComplete)
                    isDead = true;
            }

        }
    }

    private void debugMessage(String message)
    {
        if (debugMessagesOn)
            System.out.println("SQUAD " + ID + ": " + message);
    }

}
