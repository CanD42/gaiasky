package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * Abstract class with the basic functionality of bodies represented by a 3D model.
 * @author Toni Sagrista
 *
 */
public abstract class ModelBody extends CelestialBody {
    protected static final double TH_ANGLE_POINT = Math.toRadians(0.30);

    /**
     * Angle limit for rendering as point. If angle is any bigger, we render with shader.
     */
    public double THRESHOLD_POINT() {
        return TH_ANGLE_POINT;
    }

    /** MODEL **/
    public ModelComponent mc;

    /** NAME FOR WIKIPEDIA **/
    public String wikiname;

    public ModelBody() {
        super();
        localTransform = new Matrix4();
        orientation = new Matrix4d();
    }

    public void initialize() {
        if (mc != null) {
            mc.initialize();
        }
        setColor2Data();
        setDerivedAttributes();
    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);
        if (mc != null) {
            mc.doneLoading(manager, localTransform, cc);
        }
    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        super.updateLocal(time, camera);
        // Update light with global position
        if (mc != null) {
            mc.dlight.direction.set(transform.getTranslationf());
            mc.dlight.direction.add((float) camera.getPos().x, (float) camera.getPos().y, (float) camera.getPos().z);
        }
        updateLocalTransform();
    }

    /**
     * Update the local transform with the transform and the rotations/scales necessary.
     * Override if your model contains more than just the position and size.
     */
    protected void updateLocalTransform() {
        setToLocalTransform(1, localTransform, true);
    }

    public void setToLocalTransform(float sizeFactor, Matrix4 localTransform, boolean forceUpdate) {
        if (sizeFactor != 1 || forceUpdate) {
            float[] trnsltn = transform.getTranslationf();
            localTransform.idt().translate(trnsltn[0], trnsltn[1], trnsltn[2]).scl(size * sizeFactor).rotate(0, 1, 0, (float) rc.ascendingNode).rotate(0, 0, 1, (float) (rc.inclination + rc.axialTilt)).rotate(0, 1, 0, (float) rc.angle);
            if (children != null)
                orientation.idt().rotate(0, 1, 0, (float) rc.ascendingNode).rotate(0, 0, 1, (float) (rc.inclination + rc.axialTilt));
        } else {
            localTransform.set(this.localTransform);
        }
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        camera.checkClosest(this);
        float thPoint = (float) (THRESHOLD_POINT() * camera.getFovFactor());
        if (viewAngle >= thPoint) {
            opacity = MathUtilsd.lint(viewAngle, thPoint, thPoint * 4, 0, 1);
            if (viewAngle < THRESHOLD_QUAD() * camera.getFovFactor()) {
                addToRender(this, RenderGroup.SHADER_F);
            } else {
                addToRender(this, RenderGroup.MODEL_F);
            }

            if (renderText()) {
                addToRender(this, RenderGroup.LABEL);
            }
        }
    }

    @Override
    public float getInnerRad() {
        return .5f;
        //return .02f;
    }

    protected void setDerivedAttributes() {
        this.flux = (float) Math.pow(10, -absmag / 2.5f);
    }

    public void dispose() {
        mc.dispose();
    }

    @Override
    public void render(ModelBatch modelBatch, float alpha) {
        mc.setTransparency(alpha * opacity);
        modelBatch.render(mc.instance, mc.env);
    }

    public boolean withinMagLimit() {
        return this.absmag <= GlobalConf.runtime.LIMIT_MAG_RUNTIME;
    }

    @Override
    protected float labelMax() {
        return 2e-4f;
    }

    public void setModel(ModelComponent mc) {
        this.mc = mc;
    }

    public float getFuzzyRenderSize(ICamera camera) {
        float thAngleQuad = (float) THRESHOLD_QUAD() * camera.getFovFactor();
        double size = 0f;
        if (viewAngle >= THRESHOLD_POINT() * camera.getFovFactor()) {
            float tanThShaderOverlapDist = (float) Math.tan(thAngleQuad) * distToCamera;
            size = tanThShaderOverlapDist;
        }
        return (float) size / camera.getFovFactor();
    }

    @Override
    public float textScale() {
        return Math.max(.5f, labelSizeConcrete());
    }

    protected float labelSizeConcrete() {
        return (float) Math.pow(this.size * .6e1f, .001f);
    }

    @Override
    public boolean renderText() {
        return name != null && GaiaSky.instance.isOn(ComponentType.Labels) && Math.pow(viewAngle, 1.14) > TH_OVER_FACTOR * 500;
    }

    public String getWikiname() {
        return wikiname;
    }

    public void setWikiname(String wikiname) {
        this.wikiname = wikiname;
    }

}
