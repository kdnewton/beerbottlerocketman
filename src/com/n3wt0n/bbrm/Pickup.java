package com.n3wt0n.bbrm;

import net.phys2d.raw.World;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.XMLPackedSheet;

import com.n3wt0n.G2DP.Entity;
import com.n3wt0n.G2DP.SoundWrapper;

public class Pickup extends Entity {

	private XMLPackedSheet sheet;

	private Image initialImage;
	
	protected boolean pickedUp;
	protected int restoreLevel = 10;

	protected String currentState = "Init State";

	protected int rotationAngle = 10;
	protected boolean canJump = true;
	
	protected int myRestoreDelta = 0;
	protected int restoreDelta = 5000; // 5 seconds
	protected boolean canRestore = false;
	protected float opacity = 100f;
	protected int myOpacityDelta = 0;
	protected int opacityDelta = 500;
	
	public Pickup(World world, int x, int y, int width, int height, float mass,
			String name, SoundWrapper swrap) throws SlickException {
		super(world, x, y, width, height, mass, name, swrap);
		super.setSoundWrapper(swrap);
		super.setFacingRight(true);
		sheet = new XMLPackedSheet("media/images/creatures.png",
				"media/images/creatures.png.xml");
		init(sheet.getSprite("SmallCrate.png"));
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
		if (!pickedUp) {
			getImage().drawCentered(getBody().getPosition().getX(), getBody().getPosition().getY());
		}
	}

	@Override
	public void preUpdate(int delta) {
//		super.preUpdate(delta);
		
		if (canRestore) {
			myRestoreDelta += delta;
			if (myRestoreDelta > restoreDelta) {
				this.setPickedUp(false);
				myRestoreDelta = 0;
			}
		}
		
		if (opacity < 100 && !isPickedUp()) {
			myOpacityDelta += delta;
			if (myOpacityDelta > opacityDelta) {
				opacity += 0.05f;
				getImage().setAlpha(opacity);
			}
		}
	}

	public String getState() {
		return currentState;
	}

	public void setImage(String name) {
		setImage(sheet.getSprite(name));
	}

	public boolean isPickedUp() {
		return pickedUp;
	}

	public void setPickedUp(boolean beingPickedUp) {
		this.pickedUp = beingPickedUp;
		if (beingPickedUp) {
			setCanRestore(false);
			opacity = 0f;
			getImage().setAlpha(opacity);
		}
	}
	
	public void restoreWhenAble(int level) {
		if (level <= restoreLevel) {
			setCanRestore(true);
		}
	}
	
	public boolean canRestore() {
		return canRestore;
	}
	
	public void setCanRestore(boolean status) {
		canRestore = status;
	}
	
	public int getRestoreLevel() {
		return restoreLevel;
	}
	
	public void setRestoreLevel(int level) {
		this.restoreLevel = level;
	}

}
