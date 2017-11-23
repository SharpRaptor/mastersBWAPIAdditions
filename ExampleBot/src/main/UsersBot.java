package main;

import BuilderManager.BuilderManager;
import EnemyBaseTracker.EnemyBaseTrackerManager;
import ProductionQueue.*;
import SquadManager.Squad;
import SquadManager.SquadManager;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

import java.util.HashMap;
import java.util.List;


public class UsersBot extends BaseAPI {


    public static void main(String[] args) {
        new UsersBot().run();
    }

    @Override
    public void onStart()
    {
        /**
         * The super class on start method initialises the Brood War Terrain Analyser
         * Along with enabling the flag that allows user input during a bots game. This is enabled to allow for easier bot testing.
         * This needs to be called for the Base API access functions to work later on down the line for access to the player and game data.
         */
        super.onStart();


    }

    @Override
    public void onFrame()
    {

    }


    @Override
    public void onUnitCreate(Unit unit)
    {

    }

    @Override
    public void onUnitMorph(Unit unit)
    {

    }

    @Override
    public void onUnitComplete(Unit unit)
    {

    }

    @Override
    public void onUnitDestroy(Unit unit)
    {

    }

    @Override
    public void onUnitDiscover(Unit unit)
    {

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Example BuildTile Function provided by http://sscaitournament.com/index.php?action=tutorial
    //This example is a simple and inefficient search based implementation, as creating a good building
    //placement algorithm is a complicated procedure

    //example has been edited to make static for easier use

    // Returns a suitable TilePosition to build a given building type near
    // specified TilePosition aroundTile, or null if not found. (builder parameter is our worker)
    public static TilePosition getBuildTile(UnitType buildingType, TilePosition aroundTile) {
        TilePosition ret = null;
        int maxDist = 3;
        int stopDist = 40;

        // Refinery, Assimilator, Extractor
        if (buildingType.isRefinery()) {
            for (Unit n : BaseAPI.baseAPI.game.neutral().getUnits()) {
                if ((n.getType() == UnitType.Resource_Vespene_Geyser) &&
                        ( Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist ) &&
                        ( Math.abs(n.getTilePosition().getY() - aroundTile.getY()) < stopDist )
                        ) return n.getTilePosition();
            }
        }

        while ((maxDist < stopDist) && (ret == null)) {
            for (int i=aroundTile.getX()-maxDist; i<=aroundTile.getX()+maxDist; i++) {
                for (int j=aroundTile.getY()-maxDist; j<=aroundTile.getY()+maxDist; j++) {
                    if (BaseAPI.baseAPI.game.canBuildHere(new TilePosition(i,j), buildingType)) {
                        // units that are blocking the tile
                        boolean unitsInWay = false;
                        for (Unit u : BaseAPI.baseAPI.game.getAllUnits()) {
                            //if (u.getID() == builder.getID()) continue;
                            if ((Math.abs(u.getTilePosition().getX()-i) < 4) && (Math.abs(u.getTilePosition().getY()-j) < 4)) unitsInWay = true;
                        }
                        if (!unitsInWay) {
                            return new TilePosition(i, j);
                        }
                        // creep for Zerg
                        if (buildingType.requiresCreep()) {
                            boolean creepMissing = false;
                            for (int k=i; k<=i+buildingType.tileWidth(); k++) {
                                for (int l=j; l<=j+buildingType.tileHeight(); l++) {
                                    if (!BaseAPI.baseAPI.game.hasCreep(k, l)) creepMissing = true;
                                    break;
                                }
                            }
                            if (creepMissing) continue;
                        }
                    }
                }
            }
            maxDist += 2;
        }

        if (ret == null) BaseAPI.baseAPI.game.printf("Unable to find suitable build position for "+buildingType.toString());
        return ret;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



}
