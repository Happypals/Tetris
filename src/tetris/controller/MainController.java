/* *****************************************
 * CSCI205 - Software Engineering and Design
 * Spring 2016
 *
 * Name: Andre Amirsaleh, Brooke Bullek, Daniel Vasquez, Xizhou Li
 * Date: Apr 18, 2016
 * Time: 7:51:51 PM
 *
 * Project: csci205FinalProject
 * Package: tetris.controller
 * File: MainController
 * Description: Primary controller class
 *
 * ****************************************
 */
package tetris.controller;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.state.StateBasedGame;
import tetris.model.Direction;
import tetris.model.MainModel;
import tetris.resources.Resources;
import tetris.view.GameStates.state;
import tetris.view.MainView;

/**
 * The controller of the Tetris program, which syncs the model and view.
 *
 * @author Brooke Bullek
 */
public class MainController {
    /**
     * Instance of the primary model class .
     */
    private MainModel theModel;

    /**
     * Instance of the primary view class.
     */
    private MainView theView;

    /**
     * Constructor to instantiate a new TetrisController instance.
     *
     * @author Brooke Bullek
     * @param theModel An instance of the primary model class
     * @param theView An instance of the primary view class
     */
    public MainController(MainModel theModel, MainView theView) {
        this.theModel = theModel;
        this.theView = theView;
        updateViewFromModel();
    }

    /**
     * Updates the graphical components of the MainView with the internal data
     * of the MainModel. This method merely updates the components; the render()
     * method actually draws these components in a window depending on the
     * active game state.
     *
     * @author Brooke Bullek
     */
    public void updateViewFromModel() {
        theView.getGameBoardComponent().setGameBoard(theModel.getMyBoard());
        theView.getScoreBoardComponent().setScoreBoard(
                theModel.getMyBoard().getScoreBoard());
        theView.getTetriminoComponent().setTetrimino(
                theModel.getActiveTetrimino());
        theView.getTetriminoComponent().setRelativeTetriminoLocation(
                theModel.getActiveTetriminoLocation());
    }

    /**
     * Renders various components of theView based on the game's state. Uses
     * delegation to redirect rendering functionality to the View.
     *
     * @author Brooke Bullek
     * @param gc A generic game container that handles the game loop
     * @param s A state based game isolated into different stages
     * @param g A graphics context used to render primitives to the canvas
     */
    public void render(GameContainer gc, StateBasedGame s, Graphics g) {
        // update components of the View first
        updateViewFromModel();

        // render the appropriate GUI elements depending on the active state
        if (s.getCurrentStateID() == state.GAME) {
            theView.renderGameState(gc, g);
        } else if (s.getCurrentStateID() == state.MENU) {
            theView.renderMenuState(gc, g);
        } else if (s.getCurrentStateID() == state.GAME_OVER) {
            theView.renderGameOverState(gc, g);
        }
    }

    /**
     * Executes logic of the main game loop, by using a timer and by checking
     * for user input.
     *
     * @author Brooke Bullek & Daniel Vasquez
     * @param gc A generic game container that handles the game loop
     * @param s A state based game isolated into different stages
     * @param delta An increment of elapsed time in milliseconds
     */
    public void update(GameContainer gc, StateBasedGame s, int delta) {
        // perform the appropriate updates depending on the active state
        if (s.getCurrentStateID() == state.GAME) {
            updateGameState(gc, s, delta);
        } else if (s.getCurrentStateID() == state.MENU) {
            updateMenuState(gc, s, delta);
        } else if (s.getCurrentStateID() == state.GAME_OVER) {
            updateGameOverState(gc, s, delta);
        }
    }

    /**
     * Checks for input and executes the appropriate command to update the game
     * while it's in the Game State.
     *
     * @author Brooke Bullek
     * @param gc A generic game container that handles the game loop
     * @param s A state based game isolated into different stages
     * @param delta An increment of elapsed time in milliseconds
     */
    public void updateGameState(GameContainer gc, StateBasedGame s, int delta) {
        Input input = gc.getInput(); // check user input

        // preserve game speed in case a soft drop was performed
        int oldGameSpeed = theModel.getDropSpeed();

        if (input.isKeyPressed(Keymap.ROTATE_TETRIMINO.getKey())) {
            theModel.rotateActiveTetrimino(1); // '1' denotes a clockwise rotation
        } else if (input.isKeyPressed(Keymap.MOVE_LEFT.getKey())) {
            theModel.moveActiveTetrimino(Direction.LEFT);
        } else if (input.isKeyPressed(Keymap.MOVE_RIGHT.getKey())) {
            theModel.moveActiveTetrimino(Direction.RIGHT);
        } else if (input.isKeyDown(Keymap.SOFT_DROP.getKey())) {
            theModel.setSoftDropActivated(true); // Tetrimino falls faster
            theModel.setDropSpeed(theModel.getDropSpeed() / 20);
        } else if (input.isKeyPressed(Keymap.HARD_DROP.getKey())) {
            theModel.instantDropTetrimino(); // Tetrimino instantly falls to the bottom
        } else if (input.isKeyPressed(Keymap.HOLD_TETRIMINO.getKey())) {
            theModel.holdActiveTetrimino();
        } else if (input.isKeyPressed(Keymap.PAUSE.getKey())) {
            s.enterState(state.MENU);
        }

        // updates that occur while in the game state
        updateActiveTetrimino(gc, s, delta);
        theModel.setSoftDropActivated(false);
        theModel.setDropSpeed(oldGameSpeed);

        // check for game over and update the state appropriately
        if (theModel.checkGameOver()) {
            theModel.setGameOver(true);
            s.enterState(2);
        }
    }

    /**
     * Checks for input and executes the appropriate command to update the game
     * while it's in the Menu State.
     *
     * @author Brooke Bullek
     * @param gc A generic game container that handles the game loop
     * @param s A state based game isolated into different stages
     * @param delta An increment of elapsed time in milliseconds
     */
    public void updateMenuState(GameContainer gc, StateBasedGame s, int delta) {
        Input input = gc.getInput(); // check user input

        // validate user mouse position and check for button presses
        int mouseXPos = input.getMouseX();
        int mouseYPos = input.getMouseY();
        if (mouseXPos > 178 && mouseXPos < 436 && mouseYPos > 263 && mouseYPos < 328) {
            theView.getMenuState().setBackground(Resources.getImages().get(
                    "menuHighlightPlay"));
            if (input.isMousePressed(0)) {
                s.enterState(state.GAME);
            }
        } else {
            theView.getMenuState().setBackground(Resources.getImages().get(
                    "menuNoHighlightPlay"));
        }
    }

    /**
     * Checks for input and executes the appropriate command to update the game
     * while it's in the Game Over State.
     *
     * @author Brooke Bullek
     * @param gc A generic game container that handles the game loop
     * @param s A state based game isolated into different stages
     * @param delta An increment of elapsed time in milliseconds
     */
    public void updateGameOverState(GameContainer gc, StateBasedGame s,
                                    int delta) {
        Input input = gc.getInput(); // check user input

        if (input.isKeyPressed(Keymap.START_NEW_GAME.getKey())) {
            theModel = new MainModel();
            s.enterState(state.MENU);
        }
    }

    /**
     * Allows the active Tetrimino to fall downward by 1 block if enough time
     * has passed since the last fall. This method is constantly called within
     * the main game loop and its timer threshold should scale with the current
     * difficulty level.
     *
     * @author Xizhou Li
     * @param gc A generic game container that handles the game loop
     * @param delta An increment of elapsed time in milliseconds
     */
    public void updateActiveTetrimino(GameContainer gc, StateBasedGame s,
                                      int delta) {
        int timer = theModel.getTimer();

        if (timer < theModel.getDropSpeed()) {
            theModel.setTimer(timer + delta);
        } else {
            theModel.setTimer(0); // reset timer event
            theModel.moveActiveTetrimino(Direction.DOWN); // drop Tetrimino by 1 space
        }
    }
}
