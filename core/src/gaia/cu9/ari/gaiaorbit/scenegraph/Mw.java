package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class Mw extends AbstractPositionEntity implements IModelRenderable {

    private String transformName;
    public ModelComponent mc;

    public Mw() {
        super();
        localTransform = new Matrix4();
    }

    @Override
    public void initialize() {
        mc.initialize();
        mc.env.set(new ColorAttribute(ColorAttribute.AmbientLight, cc[0], cc[1], cc[2], 1));
    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);

        // Initialize transform.
        localTransform.scl(size);

        if (transformName != null) {
            Class<Coordinates> c = Coordinates.class;
            try {
                Method m = ClassReflection.getMethod(c, transformName);
                Matrix4d trf = (Matrix4d) m.invoke(null);
                Matrix4 aux = new Matrix4(trf.valuesf());
                localTransform.mul(aux);
            } catch (ReflectionException e) {
                Logger.error(Mw.class.getName(), "Error getting/invoking method Coordinates." + transformName + "()");
            }
        } else {
            // Equatorial, nothing
        }

        // Must rotate due to orientation of createCylinder
        localTransform.rotate(0, 1, 0, 90);

        // Model
        mc.doneLoading(manager, localTransform, null);

    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        // Render group never changes
        // Add to toRender list
        addToRender(this, RenderGroup.MODEL_B);
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

    @Override
    public void render(Object... params) {
        render((ModelBatch) params[0], (Float) params[1], (Float) params[2]);
    }

    @Override
    public void render(ModelBatch modelBatch, float alpha, float t) {
        mc.setTransparency(alpha * cc[3] * opacity);
        modelBatch.render(mc.instance, mc.env);
    }

    public void setTransformName(String transformName) {
        this.transformName = transformName;
    }

    @Override
    public boolean hasAtmosphere() {
        return false;
    }

    public void setModel(ModelComponent mc) {
        this.mc = mc;
    }

}
