package com.n3wt0n.bbrm;

import java.util.Random;

import net.phys2d.raw.World;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.XMLPackedSheet;

import com.n3wt0n.G2DP.Entity;
import com.n3wt0n.G2DP.SoundWrapper;

public class Crate extends Entity {

	private XMLPackedSheet sheet;

	private Image initialImage;
	protected int spriteNumber;
	
	protected float xRender, yRender;

	protected String currentState = "Init State";

	protected int rotationAngle = 10;
	protected boolean canJump = true;

	public Crate(World world, int x, int y, int width, int height, float mass,
			String name, SoundWrapper swrap) throws SlickException {
		super(world, x, y, width, height, mass, name, swrap);
		super.setSoundWrapper(swrap);
		super.setFacingRight(true);
		sheet = new XMLPackedSheet("media/images/creatures.png",
				"media/images/creatures.png.xml");

		Random rand = new Random(System.currentTimeMillis());
		spriteNumber = 1+rand.nextInt(3);
		init(sheet.getSprite("SmallCrate0" + spriteNumber + ".png"));
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
		// The method used for flipping the base image, so the hero can face
		// left.
		setImage(getImage().getFlippedCopy(true, false));
	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {
		getImage().drawCentered(xRender, yRender);
	}

	@Override
	public void preUpdate(int delta) {
		// super.preUpdate(delta);
		float theRot = getBody().getRotation();
		getImage().setRotation((float) Math.toDegrees(theRot));
		xRender = getBody().getPosition().getX();
		yRender = getBody().getPosition().getY();
	}

	public String getState() {
		return currentState;
	}

	public void setImage(String name) {
		setImage(sheet.getSprite(name));
	}

	public int getSpriteNumber() {
		return spriteNumber;
	}

}
