package com.n3wt0n.bbrm;

import java.util.Random;

import net.phys2d.raw.World;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.XMLPackedSheet;

import com.n3wt0n.G2DP.Entity;
import com.n3wt0n.G2DP.MapUtil;
import com.n3wt0n.G2DP.SoundWrapper;

public class Mob extends Entity {

	private XMLPackedSheet sheet;

	private Image initialImage;
	
	private float xRender, yRender;
	private Random rand;
	private int walkDelta = 0;
	private int walkControl = 500;
	protected boolean walkingRight;

	private MapUtil mapUtil;

	protected String currentState = "Init State";

	private float myJumpVelocity = 9000;

	protected int rotationAngle = 3;
	protected boolean canJump = true;

	private int wiggleDelta = 0;
	private int wiggleControl = 750; // speed of wiggle (higher == slower)

	public Mob(World world, int x, int y, int width, int height, float mass, String name,
			SoundWrapper swrap) throws SlickException {
		super(world, x, y, width, height, mass, name, swrap);
		super.setSoundWrapper(swrap);
		super.setFacingRight(true);
		sheet = new XMLPackedSheet("media/images/creatures.png",
				"media/images/creatures.png.xml");
		
		rand = new Random(System.currentTimeMillis());
		int number = rand.nextInt(3);
		String spriteName = "zombie01_0";
		switch (number) {
		case 0:
			spriteName += "1";
			break;
		case 1:
			spriteName += "2";
			break;
		case 2:
			spriteName += "3";
			break;
		default:
			spriteName += "1";
			break;
		}
		spriteName += ".png";
		init(sheet.getSprite(spriteName));
		
		super.setVelocity(650f);
		walkDelta = 0;
		walkControl = 1000;
	}

	public void init(Image image) throws SlickException {
		super.setImage(image);
		initialImage = image;
	}

	public void flipBaseImage() {
		// The static image that the hero is initialised as.
		// It's important that the image is a "right facing" image.
		// If it's not, then flip it using this method.
		initialImage = initialImage.getFlippedCopy(true, false);
	}

	public void flipImage() {
		// The method used for flipping the base image, so the hero can face left.
		setImage(getImage().getFlippedCopy(true, false));
	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {

		setImage(initialImage);

		if (isJumping()) {
			currentState = "Jumping";
		} else if (isFalling()) {
			currentState = "Falling";
		} else if (isOnGround()) {
			if (isMoving())
				currentState = "isMoving";
			else {
				currentState = "OnGround";
				getImage().setRotation(0);
			}
		}

		if (!isFacingRight()) {
			setImage(initialImage.getFlippedCopy(true, false));
		}

		if (isMoving() && isOnGround()) {
			currentState = "rotating -> " + getRotation();
		}
		getImage().setRotation(getRotation());
		// adding 2 to the y-position so that the feet fall a little below the surface.
		getImage().drawCentered(xRender, yRender);
	}

	@Override
	public void preUpdate(int delta) {
		super.preUpdate(delta);

		wiggleDelta += delta;
		if (wiggleDelta > wiggleControl) {
			wiggleDelta -= wiggleControl;
		}
		xRender = this.getX();
		yRender = 2+this.getY()-((getImage().getHeight()-getHeight())/2);
		if (yRender < 0) yRender *= -1;
		
		// Walk for only a second
		if (walkDelta > walkControl) {
			setRotation(0);
			getImage().setRotation(getRotation());
			// stand still after walking most of the time, but choose to walk sometimes.
			if (rand.nextFloat() < 0.005) {
				// pick a direction, left or right
				if (rand.nextInt(2) == 1) { 
					walkingRight = true;
				}
				else {
					walkingRight = false;
				}
				walkDelta = 0;
			}
		}
		else {
			// mapUtil here so mob can make sure he doesn't walk off a ledge
			if (walkingRight && mapUtil.isTileTypeAt((int)(this.getX()+16), (int)(this.getY()+32), "PLATFORMS")) {
				walkRight(delta);
			}
			else if (!walkingRight && mapUtil.isTileTypeAt((int)(this.getX()-16), (int)(this.getY()+32), "PLATFORMS")) {
				walkLeft(delta);
			}
		}
		walkDelta += delta;
	}

	public void jump(float jumpVelocity) throws SlickException {
		super.jump(jumpVelocity);
	}

	public void walkLeft(int delta) {
		wiggleWalk(walkDelta);
		super.moveLeft(getVelocity());
	}

	public void walkRight(int delta) {
		wiggleWalk(walkDelta);
		super.moveRight(getVelocity());
	}

	public void wiggleWalk(int delta) {
		// wiggleControl is one full period of
		// "rotate forward, backward, center".
		// Halve the wiggleControl
		float theValue = (float) (wiggleDelta / (wiggleControl * 0.5));
		// System.out.print ("delta: " + wiggleDelta + ", control: " +
		// (wiggleControl*0.5));
		// System.out.print (" value: " + theValue);
		theValue = (float) (theValue * Math.PI);

		// We've now normalised our period (wiggleControl) with the help of sin
		// because sin wraps from 0 to 1 to 0 to -1 back to 0
		// All we have to do is multiply our desired angle by the current sin
		// value
		// and that will give us a nice "wiggle walk" for our hero.

		theValue = (float) Math.sin(theValue);
		// System.out.println (" sin: " + theValue);
		setRotation((float) (rotationAngle * theValue));
	}

	public float getJumpVelocity() {
		return myJumpVelocity;
	}

	public void setJumpVelocity(float value) {
		myJumpVelocity = value;
	}

	public String getState() {
		return currentState;
	}

	public void setImage(String name) {
		setImage(sheet.getSprite(name));
		initialImage = getImage();
	}
	
	@Override
	public void setImage(Image image) {
		super.setImage(image);
		initialImage = image;
	}

	public void setMapUtil(MapUtil mapUtil) {
		this.mapUtil = mapUtil;
	}

}
