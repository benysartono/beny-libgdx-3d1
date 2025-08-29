package com.beny.libgdx3d1;

import com.badlogic.gdx.Game;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {
    	//GameServer gameServer = new GameServer(8070);
        setScreen(new FirstScreen());
    }
}