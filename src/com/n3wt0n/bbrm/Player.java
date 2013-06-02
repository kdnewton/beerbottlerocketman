package com.n3wt0n.bbrm;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import net.phys2d.raw.World;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.XMLPackedSheet;

import com.n3wt0n.G2DP.Entity;
import com.n3wt0n.G2DP.SoundWrapper;

public class Player extends Entity {

	private XMLPackedSheet sheet;

	private Image initialImage;
	
	private Image sprayImage;
	private float sprayX;
	private float sprayY;
	
	protected Sound spraySound;
	protected Sound jumpSound;

	private int animationStep = 0;
	private int animationDelta;
	private int animationControl = 100;
	
	private float rotationSpeed;

	protected String currentState = "Init State";

	protected boolean canJump = true;
	
	protected boolean flying = false;
	
	protected int numberOfBeers = 0;
	protected float remainingFuel = 0f;
	protected float fuelConsumptionRate = 0.8f;

	public Player(World world, int x, int y, int width, int height, float mass,
			String name, SoundWrapper swrap) throws SlickException {
		super(world, x, y, width, height, mass, name, swrap);
		super.setSoundWrapper(swrap);
		sheet = new XMLPackedSheet("media/images/little_robot_sheet.png",
				"media/images/little_robot_sheet.png.xml");
		Image image = sheet.getSprite("little_robot_00.png");
		super.setImage(image);
		this.setFacingRight(true);
		initialImage = image;

		sprayImage = new Image("media/images/Spray.png");
		spraySound = new Sound("media/audio/sfx/spray01.ogg");
		jumpSound = new Sound("media/audio/sfx/jump01.ogg");
		
		rotationSpeed = 2.0f;
	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {
		
		if (!isMoving()) {
			setImage(sheet.getSprite("little_robot_00.png"));
			initialImage = getImage();
		}

		if (isJumping()) {
			currentState = "Jumping";
		} else if (isFalling()) {
			currentState = "Falling";
		} else if (isOnGround()) {
			if (isMoving())
				currentState = "isMoving";
			else {
				currentState = "OnGround";
			}
		}

		setImage(initialImage);
		
		if (!isFacingRight()) {
			setImage(initialImage.getFlippedCopy(true, false));
		}

		if (isMoving() && isOnGround()) {
			currentState = "rotating -> " + getRotation();
		}
		getImage().setRotation(getRotation());
		getImage().drawCentered(getVisualX(), getVisualY());
		
		if (isFlying()) {
			sprayImage.setRotation(getRotation());
			sprayImage.drawCentered(sprayX, sprayY);
		}
	}

	@Override
	public void preUpdate(int delta) {
		super.preUpdate(delta);
		
		sprayX = this.getX();
		sprayY = this.getY()+60;
		if (isFlying()) {
			fly();
			return;
		} else if (!this.isOnGround()){
			// reduce the xVel smoothly, until they are hovering.
			this.setVelocity(this.getVelX()*0.98f, this.getVelY());
			this.steadyTilt();
		}
		else if (isOnGround()) {
			setRotation(0);
			if (getNumberOfBeers() > 0) {
				remainingFuel = 100f;
			}
		}
		
		if (animationDelta < animationControl) {
			animationDelta += delta;
			return;
		} else {
			animationDelta -= animationControl;
			animateWalk(animationStep);
		}
	}

	public void jump(float jumpVelocity) throws SlickException {
		super.jump(jumpVelocity);
		jumpSound.play();
		
		// int rand = (int) (Math.random() * 5) + 1;
		// super.playSound("audio/sfx/g2dp_jump_0" + rand + ".wav");
	}

	public void walkLeft() {
		if (isFlying()) {
			tiltLeft();
		} else {
			super.moveLeft(getVelocity());
		}
	}

	public void walkRight() {
		if (isFlying()) {
			tiltRight();
		} else {
			super.moveRight(getVelocity());
		}
	}

	public void animateWalk(int step) {
		if (this.isMoving()) {
			animationStep++;
			animationStep %= 5;
			setImage(sheet.getSprite("little_robot_0" + animationStep + ".png"));
			initialImage = getImage();
		}
	}

	public String getState() {
		return currentState;
	}

	public void tiltLeft() {
//		if (rotation > (maxRotate * -1)) {
			setRotation(getRotation() - rotationSpeed);
//		}
	}

	public void tiltRight() {
//		if (rotation < maxRotate) {
			setRotation(getRotation() + rotationSpeed);
//		}
	}

	public void steadyTilt() {
		if (getRotation() > 0) {
			setRotation(getRotation() - rotationSpeed);
		} else if (getRotation() < 0) {
			setRotation(getRotation() + rotationSpeed);
		}
		return;
	}
	
	public void fly() {
		if (!isFlying()) {
			return;
		}
		
		setRotation(getRotation() % 360);
		if (remainingFuel <= 0) {
			setFlying(false);
			return;
		}
		remainingFuel -= fuelConsumptionRate;
		remainingFuel = (remainingFuel < 0) ? 0 : remainingFuel;
		
		float theRot = (float) Math.toRadians(getRotation());
		float xVec, yVec;
		xVec = (float) Math.sin(theRot)*10000;
		yVec = (float) Math.cos(theRot)*(-800);
		
		if (getRotation() <= 180 || getRotation() >= 270) {
			yVec = (yVec > -600) ? -600 : yVec;
		}
		else {
			yVec -= 200;
		}
		this.applyForce(xVec, yVec-200);
		
		sprayImage = sprayImage.getFlippedCopy(true, false);
		theRot = (float) Math.toRadians(getRotation());
		sprayImage.setRotation(theRot);
		float oldX = getVisualX();
		float oldY = getVisualY();
		float newX;
		float newY;

		try {
			AffineTransform transformer = AffineTransform.getRotateInstance(
					theRot, oldX, oldY);
			Point2D before = new Point2D.Double(oldX, oldY+60);
			Point2D after = new Point2D.Double();
			after = transformer.transform(before, after);

			newX = (float) after.getX();
			newY = (float) after.getY();
			sprayX = newX;
			sprayY = newY;
		} catch (Exception e) {
			System.out.println(e);
		}
		
	}
	
	public boolean isFlying() {
		return flying;
	}
	
	public void setFlying(boolean goingToFly) {
		if (goingToFly) {
			spraySound.play();
			this.getBody().setMaxVelocity(500, 500);
			useBeer();
		}
		else {
//			rotation = 0;
			this.getBody().setMaxVelocity(100, 500);
		}
		this.flying = goingToFly;
	}
	
	public int pickupItem(Pickup p) {
		int value = -1;
		String pickupName = p.getName().split("_")[0];
		if (pickupName.equalsIgnoreCase("Beer") && !p.isPickedUp()) {
			p.setPickedUp(true);
			addBeer();
			value = 0;
		}
		
		if (pickupName.equalsIgnoreCase("Nozzle") && !p.isPickedUp()) {
			p.setPickedUp(true);
			fuelConsumptionRate = 0.55f;
			value = 1;
		}
		
		if (pickupName.equalsIgnoreCase("Compressor") && !p.isPickedUp()) {
			p.setPickedUp(true);
			fuelConsumptionRate = 0.33f;
			value = 2;
		}
		
		if (pickupName.equalsIgnoreCase("Goal") && !p.isPickedUp()) {
			value = 3;
		}
		return value;
	}
	
	public void addBeer() {
		numberOfBeers++;
	}
	
	public void useBeer() {
		numberOfBeers--;
		remainingFuel = 100;
	}
	
	public int getNumberOfBeers() {
		return numberOfBeers;
	}
	
	public float getRemainingFuel() {
		return remainingFuel;
	}

}
