package gaia.cu9.ari.gaiaorbit.interfce;

import java.time.Instant;

import com.badlogic.gdx.Gdx;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.IFocus;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory.DateType;
import gaia.cu9.ari.gaiaorbit.util.format.IDateFormat;

/**
 * Widget that captures and displays messages in a GUI.
 * 
 * @author Toni Sagrista
 *
 */
public class ConsoleLogger implements IObserver {
    private static final long DEFAULT_TIMEOUT = 5000;
    private static final String TAG_SEPARATOR = " - ";
    IDateFormat df;
    long msTimeout;
    boolean writeDates = true;

    /**
     * Initializes the notifications interface.
     * 
     * @param writeDates
     *            Log the date and time.
     */
    public ConsoleLogger(boolean writeDates) {
        this.msTimeout = DEFAULT_TIMEOUT;

        this.df = DateFormatFactory.getFormatter(I18n.locale, DateType.DATETIME);
        EventManager.instance.subscribe(this, Events.POST_NOTIFICATION, Events.FOCUS_CHANGED, Events.TOGGLE_TIME_CMD, Events.TOGGLE_VISIBILITY_CMD, Events.CAMERA_MODE_CMD, Events.PACE_CHANGED_INFO, Events.FOCUS_LOCK_CMD, Events.TOGGLE_AMBIENT_LIGHT, Events.FOV_CHANGE_NOTIFICATION, Events.JAVA_EXCEPTION, Events.ORBIT_DATA_LOADED, Events.SCREENSHOT_INFO, Events.COMPUTE_GAIA_SCAN_CMD, Events.ONLY_OBSERVED_STARS_CMD, Events.TRANSIT_COLOUR_CMD, Events.LIMIT_MAG_CMD, Events.STEREOSCOPIC_CMD, Events.DISPLAY_GUI_CMD, Events.FRAME_OUTPUT_CMD, Events.STEREO_PROFILE_CMD, Events.OCTREE_PARTICLE_FADE_CMD);
    }

    public void unsubscribe() {
        EventManager.instance.unsubscribe(this, Events.POST_NOTIFICATION, Events.FOCUS_CHANGED, Events.TOGGLE_TIME_CMD, Events.TOGGLE_VISIBILITY_CMD, Events.CAMERA_MODE_CMD, Events.PACE_CHANGED_INFO, Events.FOCUS_LOCK_CMD, Events.TOGGLE_AMBIENT_LIGHT, Events.FOV_CHANGE_NOTIFICATION, Events.JAVA_EXCEPTION, Events.ORBIT_DATA_LOADED, Events.SCREENSHOT_INFO, Events.COMPUTE_GAIA_SCAN_CMD, Events.ONLY_OBSERVED_STARS_CMD, Events.TRANSIT_COLOUR_CMD, Events.LIMIT_MAG_CMD, Events.STEREOSCOPIC_CMD, Events.DISPLAY_GUI_CMD, Events.FRAME_OUTPUT_CMD, Events.STEREO_PROFILE_CMD, Events.OCTREE_PARTICLE_FADE_CMD);
    }

    private void addMessage(String msg) {
        Gdx.app.log(df.format(Instant.now()), msg);
    }

    private void addMessage(String msg, boolean debug) {
        if (debug)
            Gdx.app.debug(df.format(Instant.now()), msg);
        else
            Gdx.app.log(df.format(Instant.now()), msg);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case POST_NOTIFICATION:
            String message = "";
            boolean perm = false;
            for (int i = 0; i < data.length; i++) {
                if (i == data.length - 1 && data[i] instanceof Boolean) {
                    perm = (Boolean) data[i];
                } else {
                    message += (String) data[i];
                    if (i < data.length - 1 && !(i == data.length - 2 && data[data.length - 1] instanceof Boolean)) {
                        message += TAG_SEPARATOR;
                    }
                }
            }
            addMessage(message);
            break;
        case FOCUS_CHANGED:
            if (data[0] != null) {
                IFocus sgn = null;
                if (data[0] instanceof String) {
                    sgn = GaiaSky.instance.sg.findFocus((String) data[0]);
                } else {
                    sgn = (IFocus) data[0];
                }
                addMessage(I18n.bundle.format("notif.camerafocus", sgn.getName()));
            }
            break;
        case TOGGLE_TIME_CMD:
            Boolean bool = (Boolean) data[0];
            if (bool == null) {
                addMessage(I18n.bundle.format("notif.toggle", I18n.bundle.format("gui.time")));
            } else {
                addMessage(I18n.bundle.format("notif.simulation." + (bool ? "resume" : "pause")));
            }
            break;
        case TOGGLE_VISIBILITY_CMD:
            if (data.length == 3)
                addMessage(I18n.bundle.format("notif.visibility." + (((Boolean) data[2]) ? "on" : "off"), I18n.bundle.get((String) data[0])));
            else
                addMessage(I18n.bundle.format("notif.visibility.toggle", I18n.bundle.get((String) data[0])));
            break;
        case FOCUS_LOCK_CMD:
        case ORIENTATION_LOCK_CMD:
        case TOGGLE_AMBIENT_LIGHT:
        case COMPUTE_GAIA_SCAN_CMD:
        case ONLY_OBSERVED_STARS_CMD:
        case TRANSIT_COLOUR_CMD:
        case OCTREE_PARTICLE_FADE_CMD:
            addMessage(data[0] + (((Boolean) data[1]) ? " on" : " off"));
            break;
        case CAMERA_MODE_CMD:
            CameraMode cm = (CameraMode) data[0];
            if (cm != CameraMode.Focus)
                addMessage(I18n.bundle.format("notif.cameramode.change", (CameraMode) data[0]));
            break;
        case PACE_CHANGED_INFO:
            addMessage(I18n.bundle.format("notif.timepace.change", data[0]));
            break;
        case LIMIT_MAG_CMD:
            addMessage(I18n.bundle.format("notif.limitmag", data[0]));
            break;
        case FOV_CHANGE_NOTIFICATION:
            // addMessage("Field of view changed to " + (float) data[0]);
            break;
        case JAVA_EXCEPTION:
            if (data.length == 1) {
                addMessage(I18n.bundle.format("notif.error", ((Throwable) data[0]).toString()));
            } else {
                addMessage(I18n.bundle.format("notif.error", data[1] + TAG_SEPARATOR + ((Throwable) data[0]).toString()));
            }
            break;
        case ORBIT_DATA_LOADED:
            addMessage(I18n.bundle.format("notif.orbitdata.loaded", data[1], ((OrbitData) data[0]).getNumPoints()), true);
            break;
        case SCREENSHOT_INFO:
            addMessage(I18n.bundle.format("notif.screenshot", data[0]));
            break;
        case STEREOSCOPIC_CMD:
            addMessage(I18n.bundle.format("notif.toggle", I18n.bundle.get("notif.stereoscopic")));
            break;
        case DISPLAY_GUI_CMD:
            addMessage(I18n.bundle.format("notif.toggle", data[0]));
            break;
        case STEREO_PROFILE_CMD:
            addMessage(I18n.bundle.format("notif.stereoscopic.profile", StereoProfile.values()[(Integer) data[0]].toString()));
            break;
        case FRAME_OUTPUT_CMD:
            boolean activated = (Boolean) data[0];
            if (activated) {
                addMessage(I18n.bundle.format("notif.activated", I18n.bundle.get("element.frameoutput")));
            } else {
                addMessage(I18n.bundle.format("notif.deactivated", I18n.bundle.get("element.frameoutput")));
            }
            break;
        default:
            break;
        }
    }

    public void dispose() {
        unsubscribe();
    }

}
