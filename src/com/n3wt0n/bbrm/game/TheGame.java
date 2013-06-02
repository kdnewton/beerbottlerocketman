package com.n3wt0n.bbrm.game;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

public class TheGame extends StateBasedGame {

	public static final int MAINMENUSTATE = 0;
	public static final int GAMEPLAYSTATE = 1;

	public TheGame() {
		super("Beer Bottle Rocket Man");
	}

	public void initStatesList(GameContainer gameContainer)
			throws SlickException {

		gameContainer.setTargetFrameRate(60);
		gameContainer.setVSync(true);

		this.addState(new MenuState(MAINMENUSTATE));
		this.addState(new GameState(GAMEPLAYSTATE));
	}

	public static void main(String[] args) throws SlickException {
		AppGameContainer app = new AppGameContainer(new TheGame());
		app.setDisplayMode(800, 600, false);
		app.setShowFPS(false);
		app.start();
	}
}