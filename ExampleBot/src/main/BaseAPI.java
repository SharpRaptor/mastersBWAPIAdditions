package main;

import bwapi.*;
import bwta.BWTA;

import java.util.ArrayList;

public class BaseAPI extends DefaultBWListener {

    public static BaseAPI baseAPI;
    public Mirror mirror = new Mirror();
    public Game game;
    public Player self;

    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
        //System.out.println("New unit discovered " + unit.getType());
    }

    @Override
    public void onStart() {
        baseAPI = this;
        game = mirror.getGame();
        self = game.self();

        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
        game.enableFlag(1); //Allows player input to bots actions for testing purposes





    }

    @Override
    public void onFrame() {


    }


    /**
     * Gets the current number of frames passed since the start of the game.
     * @return int : Number of frames since start of gaeme
     */
    public static int getGameTime() {return baseAPI.game.getFrameCount();}

}