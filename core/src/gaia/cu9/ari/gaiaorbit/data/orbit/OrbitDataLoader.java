package gaia.cu9.ari.gaiaorbit.data.orbit;

import java.util.Date;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;

import gaia.cu9.ari.gaiaorbit.scenegraph.component.OrbitComponent;

/**
 * Abstract data loader to rule them all.
 * @author Toni Sagrista
 *
 */
public class OrbitDataLoader extends AsynchronousAssetLoader<OrbitData, OrbitDataLoader.OrbitDataLoaderParameter> {

    OrbitData data;

    public OrbitDataLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, OrbitDataLoaderParameter parameter) {
        return null;
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, OrbitDataLoaderParameter parameter) {
        IOrbitDataProvider provider;
        try {
            provider = ClassReflection.newInstance(parameter.providerClass);
            provider.load(fileName, parameter);
            data = provider.getData();
        } catch (Exception e) {
            Gdx.app.error(getClass().getSimpleName(), e.getMessage());
        }

    }

    /**
     * 
     */
    public OrbitData loadSync(AssetManager manager, String fileName, FileHandle file, OrbitDataLoaderParameter parameter) {
        return data;
    }

    static public class OrbitDataLoaderParameter extends AssetLoaderParameters<OrbitData> {

        Class<? extends IOrbitDataProvider> providerClass;
        Date ini;
        boolean forward;
        float orbitalPeriod;
        double multiplier;
        int numSamples;
        String name;
        OrbitComponent orbitalParamaters;

        public OrbitDataLoaderParameter(Class<? extends IOrbitDataProvider> providerClass) {
            this.providerClass = providerClass;
        }

        public OrbitDataLoaderParameter(String name, Class<? extends IOrbitDataProvider> providerClass, OrbitComponent orbitalParameters, double multiplier) {
            this(providerClass);
            this.name = name;
            this.orbitalParamaters = orbitalParameters;
            this.multiplier = multiplier;
        }

        public OrbitDataLoaderParameter(Class<? extends IOrbitDataProvider> providerClass, String name, Date ini, boolean forward, float orbitalPeriod, int numSamples) {
            this(providerClass);
            this.name = name;
            this.ini = ini;
            this.forward = forward;
            this.orbitalPeriod = orbitalPeriod;
            this.numSamples = numSamples;
        }

        public OrbitDataLoaderParameter(Class<? extends IOrbitDataProvider> providerClass, String name, Date ini, boolean forward, float orbitalPeriod) {
            this(providerClass, name, ini, forward, orbitalPeriod, -1);
        }

        public void setIni(Date date) {
            this.ini = date;
        }

        public void setForward(boolean fwd) {
            this.forward = fwd;
        }

        public void setOrbitalPeriod(float period) {
            this.orbitalPeriod = period;
        }
    }
}
