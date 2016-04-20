package gaia.cu9.ari.gaiaorbit.data.orbit;

import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitDataLoader.OrbitDataLoaderParameter;

public interface IOrbitDataProvider {

    /**
     * Loads the orbit data into the OrbitData object in the internal
     * units.
     * @param file The file path
     * @param source The parameters
     */
    public void load(String file, OrbitDataLoaderParameter source);

    public OrbitData getData();

}
