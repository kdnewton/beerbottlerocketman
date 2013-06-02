package com.n3wt0n.bbrm.game;

import java.util.LinkedList;

import net.phys2d.math.Vector2f;
import net.phys2d.raw.World;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.XMLPackedSheet;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.tiled.TiledMap;

import com.n3wt0n.G2DP.Backdrop;
import com.n3wt0n.G2DP.Camera;
import com.n3wt0n.G2DP.MapUtil;
import com.n3wt0n.G2DP.SoundWrapper;
import com.n3wt0n.bbrm.Crate;
import com.n3wt0n.bbrm.InputManager;
import com.n3wt0n.bbrm.Mob;
import com.n3wt0n.bbrm.Pickup;
import com.n3wt0n.bbrm.Player;

public class GameState extends BasicGameState {

	protected int stateID = -1;

	private World world;
	private Backdrop backdrop;
	private InputManager inputManager;
	private Camera camera;
	private TiledMap map;
	private Player player;
	private MapUtil mapUtil;
	private SoundWrapper swrapper;
	
	private int tileWidth;
	private int tileHeight;

	private LinkedList<Mob> mob;
	private LinkedList<Crate> crate;
	private LinkedList<Pickup> pickup;
	
	private XMLPackedSheet creatureSheet;
	private Image beerIcon;
	private int beersLeft;
	private float fuelLeft;
	
	private boolean gameOver = false;
	private int myGameOverDelta = 0;
	private int gameOverDelta = 2500;
	private boolean showCongrats = false;

	private int myDelta = 0;
	private int controlDelta = 20;

	public GameState(int stateID) {
		this.stateID = stateID;
	}

	@Override
	public int getID() {
		return stateID;
	}

	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {

		mob = new LinkedList<Mob>();
		crate = new LinkedList<Crate>();
		pickup = new LinkedList<Pickup>();

		world = new World(new Vector2f(0, 750), 20);
		swrapper = new SoundWrapper();
		map = new TiledMap("media/levels/bbrm_01.tmx");
		mapUtil = new MapUtil(map, world);
		backdrop = new Backdrop(mapUtil.getMapWidth(), mapUtil.getMapHeight(), gc.getWidth(), gc.getHeight());

		gc.setTargetFrameRate(60);
		gc.setVSync(true);
		initPlayer();
		world.add(player.getBody());

		inputManager = new InputManager(player);
		camera = new Camera(gc, map, mapUtil, player, backdrop);
		initObjects(map); // must come after camera is created.
		
		tileWidth = map.getTileWidth();
		tileHeight = map.getTileHeight();
		
		creatureSheet = new XMLPackedSheet("media/images/creatures.png",
			"media/images/creatures.png.xml");
		beerIcon = creatureSheet.getSprite("Beer.png");
	}
	
	private void initPlayer() throws SlickException {
		player = new Player(world, 0, 0, 26, 60, 1.0f, "BOB", swrapper);
		player.setWorld(world);
		player.setJumpPower(7000f);
		player.setVelocity(1000f);
		player.getBody().setMaxVelocity(100, 500);
	}

	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {

		g.setColor(new Color(255, 0, 0));

		camera.render(gc, g);
		
		if (showCongrats) {
			g.setColor(new Color(128,128,128));
			g.fillRoundRect(97, 50, 606, 64, 25);
			g.setColor(new Color(255,255,255));
			g.fillRoundRect(100, 53, 600, 58, 25);
			g.setColor(new Color(0,0,0));
			g.drawString("A WINNER IS YOU!!!", 320, 72);
			
			return;
		}

		g.setColor(new Color(128,128,128));
		g.fillRoundRect(97, -19, 606, 64, 10);
		g.setColor(new Color(255,255,255));
		g.fillRoundRect(100, -16, 600, 58, 10);
		g.setColor(new Color(0,0,0));
		beerIcon.drawCentered(120, 21);
		g.drawString(" x " + beersLeft, 125, 10);
		g.drawString("FUEL x " + (int)fuelLeft + " %", 350, 10);
		g.drawString("S = 'shake beer'", 550, 10);
		
		world.step();
	}

	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {

		// Averages input to a set interval (controlDelta). For example,
		// a controlDelta of 1000 would limit input to once per second.
		myDelta += delta;
		if (myDelta < controlDelta) {
			return;
		} else {
			myDelta -= controlDelta;
		}
		
		if (gameOver) {
			if (myGameOverDelta < gameOverDelta) {
				myGameOverDelta += controlDelta;
			}
			else {
				showCongrats = true;
				return;
			}
		}
		
		beersLeft = player.getNumberOfBeers();
		fuelLeft = (float) (Math.round(player.getRemainingFuel()*100.0) / 100.0);
		
		for(Crate c : crate) {
			c.preUpdate(delta);
		}
		
		for(Mob m : mob) {
			m.preUpdate(delta);
		}

		// Player's update is called in InputManager
		inputManager.update(gc, delta);
		for (Pickup p : pickup) {
			
			// The following block checks if the player is occupying the same location as a pickup item
			int tileX = (int)(p.getX() / tileWidth);
			int tileY = (int)(p.getY() / tileHeight);
			int pickupWidth = p.getWidth() / tileWidth;
			int pickupHeight = p.getHeight() / tileHeight;
			int playerPosX = (int)(player.getX() / tileWidth);
			int playerPosY = (int)(player.getY() / tileHeight);
			
			if (playerPosX >= tileX && playerPosX < (tileX+pickupWidth) &&
					playerPosY >= tileY && playerPosY < (tileY+pickupHeight)) {
				if (player.pickupItem(p) == 3) {
					gameOver = true;
				}
			}
			
			// Beers re-appear in the game world but only when a certain condition is met.
			// With BBRM, this prevents the player from running out of fuel (though they
			// will have to re-do some portions of the level)
			if (p.getName().split("_")[0].equalsIgnoreCase("Beer")) {
				p.restoreWhenAble(player.getNumberOfBeers());
				p.preUpdate(delta);
			}
		}

		camera.update(gc, delta);
	}

	public void initObjects(TiledMap map) throws SlickException {
		int curIndex = 0;

		for (int i = 0; i < map.getObjectGroupCount(); i++) {
			for (int j = 0; j < map.getObjectCount(i); j++) {
				
				if (map.getObjectName(i, j).equalsIgnoreCase("Mob")) {
					int height = 62;
					int width = 40;
					Mob m;
					if (map.getObjectType(i, j).equalsIgnoreCase("large")) {
						width = 40;
						height = 62;
						m = new Mob(world, map.getObjectX(i, j)+(width/2), map.getObjectY(i, j), width, height, 1.0f, "Mob_" + curIndex, swrapper);
						m.setImage("Mummy.png");
					} else {
						m = new Mob(world, map.getObjectX(i, j)+(width/2), map.getObjectY(i, j), width, height, 1.0f, "Mob_" + curIndex, swrapper);
					}
					m.getBody().setFriction(0.5f);
					m.getBody().setRotatable(false);
					m.setWorld(world);
					m.setMapUtil(mapUtil);
					world.add(m.getBody());
					
					mob.add(m);
					camera.addEntity(m);
				}
				
				else if (map.getObjectName(i, j).equalsIgnoreCase("Scientist")) {
					int height = 62;
					int width = 40;
					Mob m = new Mob(world, map.getObjectX(i, j)+(width/2), map.getObjectY(i, j), width, height, 1.0f, "Mob_" + curIndex, swrapper);
					m.setImage(new Image("media/images/EvilDr.png"));
					m.flipBaseImage(); // the scientist image I use faces left by default, so I reverse it to face right
					m.getBody().setFriction(0.5f);
					m.getBody().setRotatable(false);
					m.setWorld(world);
					m.setMapUtil(mapUtil);
					world.add(m.getBody());
					
					mob.add(m);
					camera.addEntity(m);
				}
				
				else if (map.getObjectName(i, j).equalsIgnoreCase("Box")) {
					int size = 30;
					String imageName = "SmallCrate";
					if (map.getObjectType(i, j).equalsIgnoreCase("large")) {
						size = 85;
						imageName = "LargeCrate";
					}
					else if (map.getObjectType(i, j).equalsIgnoreCase("medium")) {
						size = 62;
						imageName = "MediumCrate";
					}
					Crate c = new Crate(world, map.getObjectX(i, j)+(size/2), map.getObjectY(i, j), size, size, 1.5f, "Box_" + curIndex, swrapper);
					c.getBody().setFriction(1f);
					// Check for the "canRotate" property in the crate.
					// If it's not assigned in the map, it returns a "1" string and can rotate.
					if (map.getObjectProperty(i, j, "canRotate", "1").equalsIgnoreCase("1")) {
						c.getBody().setRotatable(true);
					} else {
						c.getBody().setRotatable(false);
					}
					c.setWorld(world);
					c.setImage(imageName  + "0" + c.getSpriteNumber() + ".png");
					world.add(c.getBody());
					
					crate.add(c);
					camera.addEntity(c);
				}
				
				else if (map.getObjectType(i, j).equalsIgnoreCase("pickup")) {
					String itemImage = "";
					String itemType = "";
					int restoreLevel = -1;
					int yOffset = 18;
					if (map.getObjectName(i, j).equalsIgnoreCase("Nozzle")) {
						itemImage = "Nozzle.png";
						itemType = "Nozzle";
					}
					else if (map.getObjectName(i, j).equalsIgnoreCase("Compressor")) {
						itemImage = "Compressor.png";
						itemType = "Compressor";
					}
					else if (map.getObjectName(i, j).equalsIgnoreCase("Beer")) {
						itemImage = "Beer.png";
						itemType = "Beer";
						yOffset = 14;
						restoreLevel = Integer.parseInt(map.getObjectProperty(i, j, "restoreLevel", "-1"));
					}
					Pickup p = new Pickup(world, map.getObjectX(i, j)+16, map.getObjectY(i, j)+yOffset, 32, 32, 1.0f, itemType + "_" + curIndex, swrapper);
					p.setImage(itemImage);
					p.setRestoreLevel(restoreLevel);
					pickup.add(p);
					camera.addEntity(p);
				}
				else if (map.getObjectName(i, j).equalsIgnoreCase("Goal")) {
					// "Hack" the goal into a pickup
					Pickup p = new Pickup(world, map.getObjectX(i, j), map.getObjectY(i, j), map.getObjectWidth(i, j), map.getObjectHeight(i, j), 1.0f, "Goal_" + curIndex, swrapper);
					p.setImage(new Image("media/images/blank_cursor.png"));
					pickup.add(p);
				}
			}
		}
	}

	public void reset(GameContainer gc, StateBasedGame sbg) throws SlickException {
		init(gc, sbg);
	}
}
