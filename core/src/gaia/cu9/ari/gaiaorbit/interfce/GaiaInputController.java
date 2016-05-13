package gaia.cu9.ari.gaiaorbit.interfce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.interfce.KeyBindings.ProgramAction;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.NaturalCamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.MyPools;
import gaia.cu9.ari.gaiaorbit.util.comp.ViewAngleComparator;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

/**
 * Ripoff of libgdx's CameraInputController, for now.
 * 
 * @author Toni Sagrista
 *
 */
public class GaiaInputController extends GestureDetector {
    public static final float WASD_MOVEMENT_SENSITIVITY = .5f;
    protected static Pool<Vector3d> v3dpool = MyPools.get(Vector3d.class);
    protected static Pool<Vector3> v3pool = MyPools.get(Vector3.class);

    public KeyBindings mappings;

    /**
    	 * The button for rotating the camera either around its center or around the
    	 * focus.
    	 */
    public int leftMouseButton = Buttons.LEFT;
    /**
    	 * The angle to rotate when moved the full width or height of the screen.
    	 */
    public float rotateAngle = 360f;
    /** The button for panning the camera along the up/right plane */
    public int rightMouseButton = Buttons.RIGHT;
    /**
    	 * The units to translate the camera when moved the full width or height of
    	 * the screen.
    	 */
    public final float translateUnits = 1000f;
    /** The button for moving the camera along the direction axis */
    public int middleMouseButton = Buttons.MIDDLE;
    /**
    	 * Whether scrolling requires the activeKey to be pressed (false) or always
    	 * allow scrolling (true).
    	 */
    public boolean alwaysScroll = true;
    /** The weight for each scrolled amount. */
    public float scrollFactor = -0.1f;
    /** Whether to update the camera after it has been changed. */
    public boolean autoUpdate = true;
    /** The key for rolling the camera **/
    public int rollKey = Keys.SHIFT_LEFT;
    /** The key for looking around in focus mode **/
    public int lookFocusKey = Keys.CONTROL_LEFT;
    /** Whether to update the target on forward */
    public boolean forwardTarget = true;
    /** Whether to update the target on scroll */
    public boolean scrollTarget = false;
    protected boolean rotateLeftPressed;
    /** The Gaia camera */
    public CameraManager cam;
    private IGui gui;
    /** Celestial body comparator **/
    private Comparator<CelestialBody> comp;

    /** Holds the pressed keys at any moment **/
    public static Set<Integer> pressedKeys = new HashSet<Integer>();

    /** The current (first) button being pressed. */
    protected int button = -1;

    private float startX, startY;
    /** Max pixel distance to be considered a click **/
    private float MOVE_PX_DIST;
    /** Max distance from the click to the actual selected star **/
    private int MAX_PX_DIST;
    private Vector2 gesture = new Vector2();
    private Vector3d aux;

    protected static class GaiaGestureListener extends GestureAdapter {
        public GaiaInputController controller;
        private float previousZoom;

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            previousZoom = 0;
            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            return false;
        }

        @Override
        public boolean longPress(float x, float y) {
            return false;
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            return false;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            float newZoom = distance - initialDistance;
            float amount = newZoom - previousZoom;
            previousZoom = newZoom;
            float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
            return controller.zoom(amount / ((w > h) ? h : w));
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            return false;
        }
    };

    protected final GaiaGestureListener gestureListener;

    protected GaiaInputController(final GaiaGestureListener gestureListener, final CameraManager camera, IGui gui) {
        super(gestureListener);
        this.gui = gui;
        this.gestureListener = gestureListener;
        this.gestureListener.controller = this;
        this.cam = camera;
        this.aux = new Vector3d();
        this.comp = new ViewAngleComparator<CelestialBody>();
        // 1% of width
        this.MOVE_PX_DIST = !Constants.mobile ? (float) Math.max(5, Gdx.graphics.getWidth() * 0.01) : (float) Math.max(80, Gdx.graphics.getWidth() * 0.05);
        this.MAX_PX_DIST = !Constants.mobile ? 5 : 150;

        KeyBindings.initialize();
        mappings = KeyBindings.instance;
    }

    public GaiaInputController(final CameraManager camera, IGui gui) {
        this(new GaiaGestureListener(), camera, gui);
    }

    private int touched;
    private boolean multiTouch;

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (GlobalConf.runtime.INPUT_ENABLED) {
            touched |= (1 << pointer);
            multiTouch = !MathUtils.isPowerOfTwo(touched);
            if (multiTouch)
                this.button = -1;
            else if (this.button < 0) {
                startX = screenX;
                startY = screenY;
                gesture.set(startX, startY);
                this.button = button;
            }
        }
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
        EventManager.instance.post(Events.INPUT_EVENT, button);
        if (GlobalConf.runtime.INPUT_ENABLED) {
            touched &= -1 ^ (1 << pointer);
            if (cam.isNatural()) {
                final NaturalCamera camera = cam.naturalCamera;
                multiTouch = !MathUtils.isPowerOfTwo(touched);
                if (button == this.button && button == Input.Buttons.LEFT) {
                    // Ensure Octants observed property is computed
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            // 5% of width pixels distance
                            if (gesture.dst(screenX, screenY) < MOVE_PX_DIST) {
                                boolean stopped = camera.stopMovement();
                                boolean focusRemoved = gui != null && gui.cancelTouchFocus();
                                gesture.set(0, 0);

                                if (!stopped && !focusRemoved) {
                                    // Select star, if any
                                    List<CelestialBody> l = GaiaSky.instance.getFocusableEntities();

                                    List<CelestialBody> hits = new ArrayList<CelestialBody>();

                                    Iterator<CelestialBody> it = l.iterator();
                                    Vector3 pos = new Vector3();
                                    while (it.hasNext()) {
                                        CelestialBody s = it.next();
                                        if (s.withinMagLimit() && (!(s instanceof Particle) || (s instanceof Particle && ((Particle) s).octant == null) || (s instanceof Particle && ((Particle) s).octant != null && ((Particle) s).octant.observed))) {
                                            Vector3d posd = s.getPosition(aux);
                                            pos.set(posd.valuesf());

                                            if (camera.direction.dot(posd) > 0) {
                                                // The star is in front of us
                                                // Diminish the size of the star
                                                // when we are close by
                                                float angle = s.viewAngle;
                                                if (s instanceof Star && s.viewAngle > Constants.THRESHOLD_DOWN / camera.getFovFactor() && s.viewAngle < Constants.THRESHOLD_UP / camera.getFovFactor()) {
                                                    angle = 20f * (float) Constants.THRESHOLD_DOWN / camera.getFovFactor();
                                                }

                                                PerspectiveCamera pcamera;
                                                int screenXaux = screenX;
                                                if (GlobalConf.program.STEREOSCOPIC_MODE) {
                                                    if (screenY < Gdx.graphics.getWidth() / 2f) {
                                                        pcamera = camera.getCameraStereoLeft();
                                                        pcamera.update();
                                                    } else {
                                                        pcamera = camera.getCameraStereoRight();
                                                        pcamera.update();
                                                        screenXaux -= pcamera.viewportWidth;
                                                    }
                                                } else {
                                                    pcamera = camera.camera;
                                                }

                                                angle = (float) Math.toDegrees(angle * camera.fovFactor) * (40f / pcamera.fieldOfView);
                                                float pixelSize = Math.max(MAX_PX_DIST, ((angle * pcamera.viewportHeight) / pcamera.fieldOfView) / 2);
                                                pcamera.project(pos);
                                                pos.y = pcamera.viewportHeight - pos.y;
                                                if (GlobalConf.program.STEREOSCOPIC_MODE) {
                                                    pos.x /= 2;
                                                    if (screenX != screenXaux)
                                                        pos.x -= pcamera.viewportWidth;
                                                }
                                                // Check click distance
                                                if (pos.dst(screenXaux, screenY, pos.z) <= pixelSize) {
                                                    // Hit
                                                    hits.add(s);
                                                }
                                            }
                                        }
                                    }
                                    if (!hits.isEmpty()) {
                                        // Sort using distance
                                        Collections.sort(hits, comp);
                                        // Get closest
                                        CelestialBody hit = hits.get(hits.size() - 1);

                                        EventManager.instance.post(Events.FOCUS_CHANGE_CMD, hit);
                                        EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Focus);

                                    }
                                }
                            }
                        }
                    });
                } else if (button == this.button && button == Input.Buttons.RIGHT) {
                    // Ensure Octants observed property is computed
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            // 5% of width pixels distance
                            if (gesture.dst(screenX, screenY) < MOVE_PX_DIST) {
                                // Stop
                                camera.setYaw(0);
                                camera.setPitch(0);
                            }
                        }
                    });
                }
            }

            // Remove keyboard focus from GUI elements
            EventManager.instance.notify(Events.REMOVE_KEYBOARD_FOCUS);

            this.button = -1;
        }
        return super.touchUp(screenX, screenY, pointer, button);
    }

    protected boolean processDrag(float deltaX, float deltaY, int button) {
        if (button == leftMouseButton) {
            if (isKeyPressed(rollKey)) {
                // camera.rotate(camera.direction, deltaX * rotateAngle);
                if (deltaX != 0)
                    cam.naturalCamera.addRoll(deltaX);
            } else {
                cam.naturalCamera.addRotateMovement(deltaX, deltaY, false);
            }
        } else if (button == rightMouseButton) {
            // cam.naturalCamera.addPanMovement(deltaX, deltaY);
            cam.naturalCamera.addRotateMovement(deltaX, deltaY, true);
        } else if (button == middleMouseButton) {
            if (deltaX != 0)
                cam.naturalCamera.addForwardForce(deltaX);
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (GlobalConf.runtime.INPUT_ENABLED) {
            if (cam.isNatural()) {
                boolean result = super.touchDragged(screenX, screenY, pointer);
                if (result || this.button < 0)
                    return result;
                final float deltaX = (screenX - startX) / Gdx.graphics.getWidth();
                final float deltaY = (startY - screenY) / Gdx.graphics.getHeight();
                startX = screenX;
                startY = screenY;
                return processDrag(deltaX, deltaY, button);
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        if (GlobalConf.runtime.INPUT_ENABLED) {
            if (cam.isNatural()) {
                return zoom(amount * scrollFactor);
            }
        }
        return false;
    }

    public boolean zoom(float amount) {
        if (!alwaysScroll)
            return false;
        cam.naturalCamera.addForwardForce(amount);
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (GlobalConf.runtime.INPUT_ENABLED) {
            pressedKeys.add(keycode);
            return true;
        }
        return false;

    }

    @Override
    public boolean keyUp(int keycode) {
        EventManager.instance.post(Events.INPUT_EVENT, keycode);
        if (GlobalConf.runtime.INPUT_ENABLED) {
            // Use key mappings
            ProgramAction action = mappings.mappings.get(pressedKeys);
            if (action != null) {
                action.run();
            }

            pressedKeys.remove(keycode);
            return true;
        } else if (keycode == Keys.ESCAPE) {
            // If input is not enabled, only escape works
            Gdx.app.exit();
            pressedKeys.remove(keycode);
            return true;
        }
        pressedKeys.remove(keycode);
        return false;

    }

    public boolean isKeyPressed(int keycode) {
        return pressedKeys.contains(keycode);
    }

}
