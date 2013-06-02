package com.n3wt0n.bbrm;

import net.phys2d.math.Vector2f;
import net.phys2d.raw.World;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;

import com.n3wt0n.G2DP.Backdrop;
import com.n3wt0n.G2DP.Camera;
import com.n3wt0n.G2DP.MapUtil;
import com.n3wt0n.G2DP.SoundWrapper;

public class GameTest extends BasicGame {

	private World world;
	private Backdrop backdrop;
	private InputManager inputManager;
	private Camera camera;
	private TiledMap map;
	private Player player;
	private MapUtil mapUtil;
	private SoundWrapper swrapper;

	private int myDelta = 0;
	private int controlDelta = 20;

	public GameTest() {
		super("Beer Bottle Rocket Man");
	}

	@Override
	public void init(GameContainer gc) throws SlickException {

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
	}
	
	private void initPlayer() throws SlickException {
		player = new Player(world, 50, 50, 0, 0, 1.0f, "BoB", swrapper);
		player = new Player(world, 0, 0, player.getImage().getWidth(), player.getImage().getHeight(), 1.0f, "BOB", swrapper);
		player.setWidth(10);
		player.setHeight(40);
		player.setWorld(world);
		player.setJumpPower(7000f);
		player.setVelocity(1000f);
		player.getBody().setMaxVelocity(100, 500);
	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {

		g.setColor(new Color(255, 0, 0));

		camera.render(gc, g);

		// Seems that step must be here and not in update
		world.step();
	}

	@Override
	public void update(GameContainer gc, int delta) throws SlickException {

		// Averages input to a set interval (controlDelta). For example,
		// a controlDelta of 1000 would limit input to once per second.
		myDelta += delta;
		if (myDelta < controlDelta) {
			return;
		} else {
			myDelta -= controlDelta;
		}

		player.preUpdate(delta);

		inputManager.update(gc, delta);

		camera.update(gc, delta);
	}

	public void reset(GameContainer gc) throws SlickException {
		init(gc);
	}

	public static void main(String[] args) throws SlickException {
		AppGameContainer app = new AppGameContainer(new GameTest());
		app.setDisplayMode(640, 480, false);
		app.setShowFPS(false);
		app.start();
	}

}