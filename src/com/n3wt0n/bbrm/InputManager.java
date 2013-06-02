package com.n3wt0n.bbrm;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

public class InputManager {

	protected Player player;

	public InputManager(Player player) {
		this.player = player;
	}

	public void init() {
	}

	public void update(GameContainer gc, int delta) throws SlickException {

		Input input = gc.getInput();

		player.preUpdate(delta);

		if (input.isKeyPressed(Input.KEY_F)) {
			// Decided against allowing full screen.
//			gc.setFullscreen(!gc.isFullscreen());
		}

		if (input.isKeyPressed(Input.KEY_S)) {
			if (player.isOnGround() && player.getNumberOfBeers() > 0) {
				player.setFlying(true);
			}
		}

		player.setMoving(false);

		// Then update player state based on key input
		if (input.isKeyPressed(Input.KEY_UP) && !player.isJumping()
				&& !player.isFalling()) {
			player.jump(player.getJumpPower());
		}
		if (!input.isKeyDown(Input.KEY_UP)) {
			if (player.isFlying()) {

			} else if (player.isJumping()) {
				player.setVelocity(player.getVelX(), player.getVelY() * 0.55f);
			}
		}

		if (input.isKeyDown(Input.KEY_RIGHT)) {
			if (!player.isFlying())
				player.walkRight();
			else
				player.tiltRight();
		}
		if (input.isKeyDown(Input.KEY_LEFT)) {
			if (!player.isFlying())
				player.walkLeft();
			else
				player.tiltLeft();
		}
	}
}
