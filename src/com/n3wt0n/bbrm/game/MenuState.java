package com.n3wt0n.bbrm.game;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.XMLPackedSheet;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class MenuState extends BasicGameState {

	protected int stateID = -1;
	
	private XMLPackedSheet sheet;
	
	private int updateCounter = 0;
	
	private float minAlpha = 0.25f;
	private float alphaStep = 0.05f;

	private Image background;
	private Image overlay;
	
	private Image[] menuItem;
	private int[][] menuItemLocation;
	private boolean[] insideItem;
	private float[] transCount;
	
	private Image mousePointer;
	private int mouseX, mouseY;
	
	private Sound musicLoop;
	
	private boolean showingInfo;
	private int infoMenuX, infoMenuY;
	private String infoString;

	public MenuState(int stateID) {
		this.stateID = stateID;
	}

	@Override
	public int getID() {
		return stateID;
	}

	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		
		menuItem = new Image[3];
		insideItem = new boolean[menuItem.length];
		transCount = new float[menuItem.length];
		menuItemLocation = new int[menuItem.length][2];

		sheet = new XMLPackedSheet("media/images/bbrm_menu.png",
				"media/images/bbrm_menu.png.xml");

		menuItemLocation[0][0] = 10; // set image's x location
		menuItemLocation[0][1] = 175; // set image's y location
		menuItemLocation[1][0] = 10; // set next image's x location
		menuItemLocation[1][1] = 325; // etc
		menuItemLocation[2][0] = 10;
		menuItemLocation[2][1] = 450;
		
		background = new Image("media/images/NewMenu_background.png");
		overlay = new Image("media/images/NewMenu_Overlay.png");

		menuItem[0] = sheet.getSprite("play.png");
		menuItem[1] = sheet.getSprite("info.png");
		menuItem[2] = sheet.getSprite("quit.png");
		
		for (int i = 0; i < menuItem.length; i++) {
			insideItem[i] = false;
			transCount[i] = minAlpha;
		}

		mousePointer = sheet.getSprite("pointer_bottle.png");
		gc.setMouseCursor("media/images/blank_cursor.png",0,0);
		
		musicLoop = new Sound("media/audio/music/MujikAngel.ogg");
		musicLoop.loop();
		
		initInfoMenu(gc);
		this.moveInfo(325, 250);
	}

	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
		
		background.draw(0, 0);
		overlay.draw(0,0);
		
		for (int i = 0; i < menuItem.length; i++) {
			menuItem[i].draw(menuItemLocation[i][0], menuItemLocation[i][1]);
		}
		
		if (showingInfo) {
			g.setColor(Color.lightGray);
			g.fillRoundRect(infoMenuX, infoMenuY, 475, 300, 25);
			g.setColor(Color.black);
			g.drawString(this.infoString, infoMenuX+10, infoMenuY+10);
		}
		
		mousePointer.drawCentered(mouseX+(mousePointer.getWidth()/2), mouseY+(mousePointer.getHeight()/2));
	}

	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
		
		if (updateCounter < 50) {
			updateCounter += delta;
			return;
		} else {
			updateCounter -= delta;
		}
		
		Input input = gc.getInput();

		mouseX = input.getMouseX();
		mouseY = input.getMouseY();
		
		for (int i = 0; i < menuItem.length; i++) {
			insideItem[i] = false;
			// Check to see if the mouse is hovering over the "start game" item
			if ((mouseX >= menuItemLocation[i][0] && mouseX <= menuItemLocation[i][0] + menuItem[i].getWidth())
					&& (mouseY >= menuItemLocation[i][1] && mouseY <= menuItemLocation[i][1] + menuItem[i].getHeight())) {
				insideItem[i] = true;
			}

			if (insideItem[i]) {
				if (transCount[i] < 1f) {
					transCount[i] += alphaStep;
				}
			} else {
				if (transCount[i] > minAlpha) {
					transCount[i] -= alphaStep;
				}
			}
			menuItem[i].setAlpha(transCount[i]);
		}

		// Start
		if (insideItem[0] && input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
			this.startGame(sbg);
		}

		// Start
		if (insideItem[1] && input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
			showingInfo = (showingInfo) ? false: true;
		}

		// Exit
		if (insideItem[2] && input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
			musicLoop.stop();
			gc.exit();
		}

	}
	
	public void startGame(StateBasedGame sbg) {
		musicLoop.stop();
		sbg.enterState(TheGame.GAMEPLAYSTATE);
	}
	
	public void initInfoMenu(GameContainer gc) {
		// Hide the information menu offscreen
		infoMenuX = gc.getWidth()+10;
		infoMenuY = gc.getHeight()+10;
		showingInfo = false;
		infoString = "Background image by _Madolan_\n" +
				"http://www.flickr.com/photos/_madolan_/2619607267/\n\n" +
				"This game created for the month-long game\n" +
				"development contest at the Slick forums.\n\n" +
				"The theme: Beer\n\n" +
				"Slick site: http://slick.cokeandcode.com/\n\n" +
				"Slick forums: http://slick.javaunlimited.net/\n\n" +
				"Author: Kyle Newton\n" +
				"Author site: http://n3wt0n.com/";
	}
	
	public void moveInfo(int x, int y) {
		infoMenuX = x;
		infoMenuY = y;
	}

}
