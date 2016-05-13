package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.MyPools;
import gaia.cu9.ari.gaiaorbit.util.gaia.GaiaAttitudeServer;
import gaia.cu9.ari.gaiaorbit.util.gaia.Satellite;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Quaterniond;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.GlobalClock;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * The field of view cameras.
 * @author Toni Sagrista
 *
 */
public class FovCamera extends AbstractCamera implements IObserver {
    private static final double CAM_NEAR = 1e6 * Constants.KM_TO_U;
    private static final double CAM_FAR = 1e22 * Constants.KM_TO_U;
    private static final float FOV_CORR = 0.2f;
    private static final float FOV = (float) Satellite.FOV_AC + FOV_CORR;
    private static final float BAM_2 = (float) Satellite.BASICANGLE_DEGREE / 2f;
    private static final double GAIA_ASPECT_RATIO = (Satellite.FOV_AL + FOV_CORR) / FOV;

    /** time that has to pass with the current scan rate so that we scan to the
     * edge of the current field of view.
     **/
    public long MAX_OVERLAP_TIME = 0l;
    public float MAX_OVERLAP_ANGLE = 0;

    private PerspectiveCamera camera2;

    public Gaia gaia;

    Vector3d dirMiddle, up;
    public Vector3d[] directions;
    public List<Vector3d[]> interpolatedDirections;
    private Matrix4d trf;

    public long currentTime, lastTime;
    private Pool<Vector3d> vectorPool;
    private Pool<Matrix4d> matrixPool;

    Viewport viewport, viewport2;

    Stage[] fpstages;
    Drawable fp, fp_fov1, fp_fov2;

    @SuppressWarnings("unchecked")
    public FovCamera(AssetManager assetManager, CameraManager parent) {
        super(parent);
        System.out.println("FOV: " + FOV);
        initialize(assetManager);
        directions = new Vector3d[] { new Vector3d(), new Vector3d() };
        interpolatedDirections = new ArrayList<Vector3d[]>();
        dirMiddle = new Vector3d();
        up = new Vector3d();

        currentTime = 0l;
        lastTime = 0l;
        vectorPool = MyPools.get(Vector3d.class);
        matrixPool = MyPools.get(Matrix4d.class);
    }

    public void initialize(AssetManager assetManager) {
        camera = new PerspectiveCamera(FOV, (float) (Gdx.graphics.getHeight() * GAIA_ASPECT_RATIO), Gdx.graphics.getHeight());
        camera.near = (float) CAM_NEAR;
        camera.far = (float) CAM_FAR;

        camera2 = new PerspectiveCamera(FOV, (float) (Gdx.graphics.getHeight() * GAIA_ASPECT_RATIO), Gdx.graphics.getHeight());
        camera2.near = (float) CAM_NEAR;
        camera2.far = (float) CAM_FAR;

        fovFactor = FOV / 5f;

        /**
         * Fit viewport ensures a fixed aspect ratio. We set the camera field of view equal to the
         * satelltie's AC FOV and calculate the satellite aspect ratio as FOV_AL/FOV_AC. With it we
         * set the width of the viewport to ensure we have the same vision as Gaia.
         */
        viewport = new FitViewport((float) (Gdx.graphics.getHeight() * GAIA_ASPECT_RATIO), Gdx.graphics.getHeight(), camera);
        viewport2 = new FitViewport((float) (Gdx.graphics.getHeight() * GAIA_ASPECT_RATIO), Gdx.graphics.getHeight(), camera2);

        /** Prepare stage with FP image **/
        fp = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("img/gaia-focalplane.png"))));
        fp_fov1 = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("img/gaia-focalplane-fov1.png"))));
        fp_fov2 = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("img/gaia-focalplane-fov2.png"))));

        fpstages = new Stage[3];

        Stage fov12 = new Stage(new ScalingViewport(Scaling.stretch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new OrthographicCamera()), GlobalResources.spriteBatch);
        Image i = new Image(fp);
        i.setFillParent(true);
        i.setAlign(Align.center);
        i.setColor(0.3f, 0.8f, 0.3f, .9f);
        fov12.addActor(i);

        Stage fov1 = new Stage(new ScalingViewport(Scaling.stretch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new OrthographicCamera()), GlobalResources.spriteBatch);
        i = new Image(fp_fov1);
        i.setFillParent(true);
        i.setAlign(Align.center);
        i.setColor(0.3f, 0.8f, 0.3f, .9f);
        fov1.addActor(i);

        Stage fov2 = new Stage(new ScalingViewport(Scaling.stretch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new OrthographicCamera()), GlobalResources.spriteBatch);
        i = new Image(fp_fov2);
        i.setFillParent(true);
        i.setAlign(Align.center);
        i.setColor(0.3f, 0.8f, 0.3f, .9f);
        fov2.addActor(i);

        fpstages[0] = fov1;
        fpstages[1] = fov2;
        fpstages[2] = fov12;

        EventManager.instance.subscribe(this, Events.GAIA_LOADED, Events.COMPUTE_GAIA_SCAN_CMD);
    }

    public void update(float dt, ITimeFrameProvider time) {
        distance = pos.len();

        up.set(0, 1, 0);

        /** POSITION **/
        AbstractPositionEntity fccopy = gaia.getLineCopy();
        fccopy.getRoot().transform.position.set(0f, 0f, 0f);
        fccopy.getRoot().update(time, null, this);

        this.pos.set(fccopy.transform.getTranslation());
        this.posinv.set(this.pos).scl(-1);

        /** ORIENTATION - directions and up **/
        updateDirections(time);
        trf = matrixPool.obtain();
        up.mul(trf).nor();

        // Update cameras
        updateCamera(directions[0], up, camera);

        updateCamera(directions[1], up, camera2);

        dirMiddle.set(0, 0, 1).mul(trf);
        matrixPool.free(trf);

    }

    /**
     * Updates both FOVs' directions applying the right transformation.
     * @param time
     */
    public void updateDirections(ITimeFrameProvider time) {
        lastTime = currentTime;
        currentTime = time.getTime().getTime();
        trf = matrixPool.obtain();
        trf.idt();
        Quaterniond quat = GaiaAttitudeServer.instance.getAttitude(time.getTime()).getQuaternion();
        trf.rotate(quat).rotate(0, 0, 1, 180);
        directions[0].set(0, 0, 1).rotate(BAM_2, 0, 1, 0).mul(trf).nor();
        directions[1].set(0, 0, 1).rotate(-BAM_2, 0, 1, 0).mul(trf).nor();
        matrixPool.free(trf);

        /** WORK OUT INTERPOLATED DIRECTIONS IN THE CASE OF FAST SCANNING **/
        for (Vector3d[] directions : interpolatedDirections) {
            vectorPool.free(directions[0]);
            vectorPool.free(directions[1]);
        }
        interpolatedDirections.clear();
        if (GlobalConf.scene.COMPUTE_GAIA_SCAN) {
            if (lastTime != 0 && currentTime - lastTime > MAX_OVERLAP_TIME) {
                if (((GlobalClock) time).fps < 0) {
                    ((GlobalClock) time).fps = 10;
                    Logger.info(this.getClass().getSimpleName(), I18n.bundle.get("notif.timeprovider.fixed"));
                }
                for (long t = lastTime + MAX_OVERLAP_TIME; t < currentTime; t += MAX_OVERLAP_TIME) {
                    interpolatedDirections.add(getDirections(new Date(t)));
                }
            } else {
                if (((GlobalClock) time).fps > 0) {
                    ((GlobalClock) time).fps = -1;
                    Logger.info(this.getClass().getSimpleName(), I18n.bundle.get("notif.timeprovider.real"));
                }
            }
        }
    }

    public Vector3d[] getDirections(Date d) {
        trf = matrixPool.obtain();
        trf.idt();
        Quaterniond quat = GaiaAttitudeServer.instance.getAttitude(d).getQuaternion();
        trf.rotate(quat).rotate(0, 0, 1, 180);
        Vector3d dir1 = vectorPool.obtain().set(0, 0, 1).rotate(BAM_2, 0, 1, 0).mul(trf).nor();
        Vector3d dir2 = vectorPool.obtain().set(0, 0, 1).rotate(-BAM_2, 0, 1, 0).mul(trf).nor();
        matrixPool.free(trf);
        return new Vector3d[] { dir1, dir2 };
    }

    /**
     * Updates the given camera using the given direction and up vectors. Sets the
     * position to zero.
     * @param dir
     * @param up
     * @param cam
     */
    private void updateCamera(Vector3d dir, Vector3d up, PerspectiveCamera cam) {
        cam.position.set(0f, 0f, 0f);
        cam.direction.set(dir.valuesf());
        cam.up.set(up.valuesf());
        cam.update();
    }

    @Override
    public PerspectiveCamera[] getFrontCameras() {
        switch (parent.mode) {
        case Gaia_FOV1:
        default:
            return new PerspectiveCamera[] { camera };
        case Gaia_FOV2:

            return new PerspectiveCamera[] { camera2 };
        case Gaia_FOV1and2:
            return new PerspectiveCamera[] { camera, camera2 };
        }
    }

    @Override
    public PerspectiveCamera getCamera() {
        switch (parent.mode) {
        case Gaia_FOV1:
            return camera;
        case Gaia_FOV2:
            return camera2;
        default:
            return camera;
        }
    }

    @Override
    public float getFovFactor() {
        return this.fovFactor;
    }

    @Override
    public Viewport getViewport() {
        if (parent.mode.equals(CameraMode.Gaia_FOV2)) {
            return viewport2;
        }
        return viewport;
    }

    @Override
    public void setViewport(Viewport viewport) {
        // Viewports are managed by the camera!
    }

    @Override
    public Vector3d getDirection() {
        int idx = parent.mode.ordinal() - 3;
        idx = Math.min(idx, 1);
        return directions[idx];
    }

    @Override
    public Vector3d getUp() {
        return up;
    }

    @Override
    public final Vector3d[] getDirections() {
        return directions;
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case GAIA_LOADED:
            this.gaia = (Gaia) data[0];
            break;
        case COMPUTE_GAIA_SCAN_CMD:
            lastTime = 0;
            currentTime = 0;
            break;
        }

    }

    @Override
    public void updateMode(CameraMode mode, boolean postEvent) {
    }

    @Override
    public int getNCameras() {
        switch (parent.mode) {
        case Gaia_FOV1:
        case Gaia_FOV2:
            return 1;
        case Gaia_FOV1and2:
            return 2;
        default:
            return 0;
        }
    }

    @Override
    public CameraMode getMode() {
        return parent.mode;
    }

    /**
     * We have fixed field of view angles and thus fixed aspect ratio.
     */
    public void updateAngleEdge(int width, int height) {
        float h = (float) Satellite.FOV_AC_ACTIVE;
        float w = (float) Satellite.FOV_AL;
        angleEdgeRad = (float) (Satellite.FOV_AL * Math.PI / 180);
        // Update max overlap time
        MAX_OVERLAP_TIME = (long) (angleEdgeRad / (Satellite.SCANRATE * (Math.PI / (3600 * 180)))) * 1000;
        MAX_OVERLAP_ANGLE = angleEdgeRad;
    }

    @Override
    public void render() {
        // Renders the focal plane CCDs
        fpstages[parent.mode.ordinal() - 3].draw();
    }

    @Override
    public float getMotionMagnitude() {
        return 0;
    }

    @Override
    public void saveState() {
    }

    @Override
    public void restoreState() {
    }

    @Override
    public double getVelocity() {
        return parent.getVelocity();
    }

    @Override
    public boolean superVelocity() {
        return parent.superVelocity();
    }

    @Override
    public boolean isFocus(CelestialBody cb) {
        return false;
    }

    @Override
    public void checkClosest(CelestialBody cb) {
    }

    @Override
    public CelestialBody getFocus() {
        return null;
    }

    public void computeGaiaScan(ITimeFrameProvider time, CelestialBody cb) {
        boolean visible = computeVisibleFovs(cb.pos, this, cb.pos.len());
        cb.updateTransitNumber(visible && time.getDt() != 0, time, this);
    }

    @Override
    public boolean isVisible(ITimeFrameProvider time, CelestialBody cb) {
        switch (parent.mode) {
        case Gaia_FOV1:
        case Gaia_FOV2:
            return super.isVisible(time, cb);
        case Gaia_FOV1and2:
            return computeVisibleFovs(cb.pos, this, cb.pos.len());
        default:
            return false;
        }
    }

	@Override
	public void setCamera(PerspectiveCamera cam) {
		// Nothing to do
	}

	@Override
	public void setCameraStereoLeft(PerspectiveCamera cam) {
	}

	@Override
	public void setCameraStereoRight(PerspectiveCamera cam) {
	}

	@Override
	public PerspectiveCamera getCameraStereoLeft() {
		return null;
	}

	@Override
	public PerspectiveCamera getCameraStereoRight() {
		return null;
	}

}
