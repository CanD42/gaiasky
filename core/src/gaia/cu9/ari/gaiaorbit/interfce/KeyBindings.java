package gaia.cu9.ari.gaiaorbit.interfce;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;

/**
 * Contains the key mappings and the actions. This should be persisted somehow
 * in the future.
 * 
 * @author Toni Sagrista
 *
 */
public class KeyBindings {
    public Map<TreeSet<Integer>, ProgramAction> mappings;

    public static KeyBindings instance;

    public static void initialize() {
        if (instance == null) {
            instance = new KeyBindings();
        }
    }

    private int SPECIAL1, SPECIAL2;

    /**
     * Creates a key mappings instance.
     */
    public KeyBindings() {
        mappings = new HashMap<TreeSet<Integer>, ProgramAction>();
        // Init special keys
        SPECIAL1 = Constants.webgl ? Keys.SHIFT_LEFT : Keys.CONTROL_LEFT;
        SPECIAL2 = Constants.webgl ? Keys.SHIFT_RIGHT : Keys.SHIFT_LEFT;
        // For now this will do
        initDefault();
    }

    public void addMapping(ProgramAction action, int... keyCodes) {
        TreeSet<Integer> keys = new TreeSet<Integer>();
        for (int key : keyCodes) {
            keys.add(key);
        }
        mappings.put(keys, action);
    }

    /**
     * Initializes the default keyboard mappings. In the future these should be
     * read from a configuration file.
     */
    public void initDefault() {

        // ESCAPE -> Exit
        addMapping(new ProgramAction(txt("action.exit"), new Runnable() {
            @Override
            public void run() {
                Gdx.app.exit();
            }
        }), Keys.ESCAPE);

        // O -> Toggle orbits
        addMapping(new ProgramAction(txt("action.toggle", txt("element.orbits")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, txt("element.orbits"), false);
            }
        }), Keys.O);

        // P -> Toggle planets
        addMapping(new ProgramAction(txt("action.toggle", txt("element.planets")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, txt("element.planets"), false);
            }
        }), Keys.P);

        // M -> Toggle moons
        addMapping(new ProgramAction(txt("action.toggle", txt("element.moons")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, txt("element.moons"), false);
            }
        }), Keys.M);

        // S -> Toggle stars
        addMapping(new ProgramAction(txt("action.toggle", txt("element.stars")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, txt("element.stars"), false);
            }
        }), Keys.S);

        // T -> Toggle satellites
        addMapping(new ProgramAction(txt("action.toggle", txt("element.satellites")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, txt("element.satellites"), false);
            }
        }), Keys.T);

        // L -> Toggle labels
        addMapping(new ProgramAction(txt("action.toggle", txt("element.labels")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, txt("element.labels"), false);
            }
        }), Keys.L);

        // C -> Toggle constellations
        addMapping(new ProgramAction(txt("action.toggle", txt("element.constellations")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, txt("element.constellations"), false);
            }
        }), Keys.C);

        // B -> Toggle boundaries
        addMapping(new ProgramAction(txt("action.toggle", txt("element.boundaries")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, txt("element.boundaries"), false);
            }
        }), Keys.B);

        // Q -> Toggle equatorial
        addMapping(new ProgramAction(txt("action.toggle", txt("element.equatorial")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, txt("element.equatorial"), false);
            }
        }), Keys.Q);

        // E -> Toggle ecliptic
        addMapping(new ProgramAction(txt("action.toggle", txt("element.ecliptic")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, txt("element.ecliptic"), false);
            }
        }), Keys.E);

        // G -> Toggle galactic
        addMapping(new ProgramAction(txt("action.toggle", txt("element.galactic")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, txt("element.galactic"), false);
            }
        }), Keys.G);

        // Left bracket -> divide speed
        addMapping(new ProgramAction(txt("action.dividetime"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TIME_WARP_DECREASE_CMD);
            }
        }), Keys.COMMA);

        // Right bracket -> double speed
        addMapping(new ProgramAction(txt("action.doubletime"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TIME_WARP_INCREASE_CMD);
            }
        }), Keys.PERIOD);

        // SPACE -> toggle time
        addMapping(new ProgramAction(txt("action.pauseresume"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TOGGLE_TIME_CMD, null, false);
            }
        }), Keys.SPACE);

        // Plus -> increase limit magnitude
        addMapping(new ProgramAction(txt("action.incmag"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.LIMIT_MAG_CMD, GlobalConf.runtime.LIMIT_MAG_RUNTIME + 0.1f);
            }
        }), Keys.PLUS);

        // Minus -> decrease limit magnitude
        addMapping(new ProgramAction(txt("action.decmag"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.LIMIT_MAG_CMD, GlobalConf.runtime.LIMIT_MAG_RUNTIME - 0.1f);
            }
        }), Keys.MINUS);

        // Star -> reset limit mag
        addMapping(new ProgramAction(txt("action.resetmag"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.LIMIT_MAG_CMD, GlobalConf.data.LIMIT_MAG_LOAD);
            }
        }), Keys.STAR);

        // F11 -> fullscreen
        addMapping(new ProgramAction(txt("action.togglefs"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.FULLSCREEN_CMD);
            }
        }), Keys.F11);

        // F4 -> toggle fisheye effect
        addMapping(new ProgramAction(txt("action.fisheye"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.FISHEYE_CMD, !GlobalConf.postprocess.POSTPROCESS_FISHEYE);
            }
        }), Keys.F4);

        // F5 -> take screenshot
        addMapping(new ProgramAction(txt("action.screenshot"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.SCREENSHOT_CMD, GlobalConf.screenshot.SCREENSHOT_WIDTH, GlobalConf.screenshot.SCREENSHOT_HEIGHT, GlobalConf.screenshot.SCREENSHOT_FOLDER);
            }
        }), Keys.F5);

        // F6 -> toggle frame output
        addMapping(new ProgramAction(txt("action.toggle", txt("element.frameoutput")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.FRAME_OUTPUT_CMD);
            }
        }), Keys.F6);

        // U -> toggle UI collapse/expand
        addMapping(new ProgramAction(txt("action.toggle", txt("element.controls")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.GUI_FOLD_CMD);
            }
        }), Keys.U);

        // CTRL + SHIFT + UP -> increase star point size by 0.5
        addMapping(new ProgramAction(txt("action.starpointsize.inc"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.STAR_POINT_SIZE_INCREASE_CMD);
            }
        }), SPECIAL1, SPECIAL2, Keys.UP);

        // CTRL + SHIFT + DOWN -> decrease star point size by 0.5
        addMapping(new ProgramAction(txt("action.starpointsize.dec"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.STAR_POINT_SIZE_DECREASE_CMD);
            }
        }), SPECIAL1, SPECIAL2, Keys.DOWN);

        // CTRL + SHIFT + R -> reset star point size
        addMapping(new ProgramAction(txt("action.starpointsize.reset"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.STAR_POINT_SIZE_RESET_CMD);
            }
        }), SPECIAL1, SPECIAL2, Keys.R);

        // Camera modes (NUMERIC KEYPAD)
        for (int i = 144; i <= 153; i++) {
            // Camera mode
            int m = i - 144;
            final CameraMode mode = CameraMode.getMode(m);
            if (mode != null) {
                addMapping(new ProgramAction(mode.name(), new Runnable() {
                    @Override
                    public void run() {
                        EventManager.instance.post(Events.CAMERA_MODE_CMD, mode);
                    }
                }), i);
            }
        }

        // CTRL + D -> Toggle debug information
        addMapping(new ProgramAction(txt("action.toggle", txt("element.debugmode")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.SHOW_DEBUG_CMD);
            }
        }), SPECIAL1, Keys.D);

        // CTRL + F -> Search dialog
        addMapping(new ProgramAction(txt("action.search"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.SHOW_SEARCH_ACTION);
            }
        }), SPECIAL1, Keys.F);

        // CTRL + SHIFT + O -> Toggle particle fade
        addMapping(new ProgramAction(txt("action.toggle", txt("element.octreeparticlefade")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.OCTREE_PARTICLE_FADE_CMD, txt("element.octreeparticlefade"), !GlobalConf.scene.OCTREE_PARTICLE_FADE);
            }
        }), SPECIAL1, SPECIAL2, Keys.O);

        // CTRL + S -> Toggle stereoscopic mode
        addMapping(new ProgramAction(txt("action.toggle", txt("element.stereomode")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TOGGLE_STEREOSCOPIC_CMD, txt("notif.stereoscopic"));
            }
        }), SPECIAL1, Keys.S);

        // CTRL + SHIFT + S -> Switch stereoscopic profile
        addMapping(new ProgramAction(txt("action.switchstereoprofile"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.TOGGLE_STEREO_PROFILE_CMD);
            }
        }), SPECIAL1, SPECIAL2, Keys.S);

        // CTRL + U -> Toggle clean (no GUI) mode
        addMapping(new ProgramAction(txt("action.toggle", txt("element.cleanmode")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.DISPLAY_GUI_CMD, txt("notif.cleanmode"));
            }
        }), SPECIAL1, Keys.U);

        // CTRL + P -> Change pixel renderer
        //        addMapping(new ProgramAction(txt("action.toggle", txt("element.pixelrenderer")), new Runnable() {
        //            @Override
        //            public void run() {
        //                EventManager.instance.post(Events.PIXEL_RENDERER_CMD, (GlobalConf.scene.PIXEL_RENDERER + 1) % 3);
        //                EventManager.instance.post(Events.PIXEL_RENDERER_UPDATE);
        //            }
        //        }), SPECIAL1, Keys.P);

        // CTRL + G -> Travel to focus object
        addMapping(new ProgramAction(txt("action.gotoobject"), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.GO_TO_OBJECT_CMD);
            }
        }), SPECIAL1, Keys.G);

        // CTRL + SHIFT + G -> Galaxy 2D - 3D
        addMapping(new ProgramAction(txt("action.toggle", txt("element.galaxy3d")), new Runnable() {
            @Override
            public void run() {
                EventManager.instance.post(Events.GALAXY_3D_CMD, !GlobalConf.scene.GALAXY_3D);
            }
        }), SPECIAL1, SPECIAL2, Keys.G);

    }

    /**
     * A simple program action.
     * 
     * @author Toni Sagrista
     *
     */
    public class ProgramAction implements Runnable, Comparable<ProgramAction> {
        public final String actionName;
        private final Runnable action;

        public ProgramAction(String actionName, Runnable action) {
            this.actionName = actionName;
            this.action = action;
        }

        @Override
        public void run() {
            action.run();
        }

        @Override
        public int compareTo(ProgramAction other) {
            return actionName.toLowerCase().compareTo(other.actionName.toLowerCase());
        }

    }

    protected static String txt(String key) {
        return I18n.bundle.get(key);
    }

    protected static String txt(String key, Object... args) {
        return I18n.bundle.format(key, args);
    }
}
