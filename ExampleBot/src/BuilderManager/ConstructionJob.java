package BuilderManager;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

/**
 * Storage for the different Construction jobs that are ordered. Holds the details of what to build, the builder assigned, and the building once started.
 */
public class ConstructionJob {

    private Unit assignedBuilder;
    private UnitType toBuild;
    private TilePosition location;
    private Unit startedBuilding;

    public ConstructionJob(Unit assignedBuilder, UnitType toBuild, TilePosition location)
    {
        startedBuilding = null;
        this.assignedBuilder = assignedBuilder;
        this.toBuild = toBuild;
        this.location = location;
    }

    public Unit getAssignedBuilder() {return assignedBuilder;}
    public UnitType getToBuild() {return toBuild;}
    public TilePosition getLocation() {return location;}
    public Unit getStartedBuilding() {return startedBuilding;}

    public void setStartedBuilding(Unit startedBuilding){this.startedBuilding = startedBuilding;}
    public void setAssignedBuilder(Unit builder) {this.assignedBuilder = builder;}


}
