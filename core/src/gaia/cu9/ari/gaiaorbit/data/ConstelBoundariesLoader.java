package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ConstellationBoundaries;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.badlogic.gdx.Gdx;

public class ConstelBoundariesLoader<T extends SceneGraphNode> implements ISceneGraphNodeProvider {
    private static final String separator = "\\t";
    private static final boolean LOAD_INTERPOLATED = true;
    private static final int INTERPOLATED_MOD = 3;
    private String dataPath;

    @Override
    public void initialize(Properties properties) {
	try {
	    dataPath = properties.getProperty("file");

	} catch (Exception e) {
	    Gdx.app.error(this.getClass().getSimpleName(), e.getLocalizedMessage());
	}
    }

    @Override
    public List<ConstellationBoundaries> loadObjects() {
	List<ConstellationBoundaries> boundaries = new ArrayList<ConstellationBoundaries>();
	try {
	    // load constellations
	    BufferedReader br = new BufferedReader(new InputStreamReader(FileLocator.getStream(dataPath)));
	    int n = 0;
	    try {
		//Skip first line
		String line;
		ConstellationBoundaries boundary = new ConstellationBoundaries();
		boundary.ct = ComponentType.Boundaries;
		List<List<Vector3d>> list = new ArrayList<List<Vector3d>>();
		List<Vector3d> buffer = new ArrayList<Vector3d>(4);
		String lastName = new String();
		int interp = 0;
		while ((line = br.readLine()) != null) {
		    if (!line.startsWith("#")) {
			String[] tokens = line.split(separator);

			String name = tokens[2];
			String type = tokens.length > 3 ? tokens[3] : "O";

			if (!name.equals(lastName)) {
			    // New line
			    list.add(buffer);
			    buffer = new ArrayList<Vector3d>(20);
			    lastName = name;
			}

			if (type.equals("I")) {
			    interp++;
			}

			if ((type.equals("I") && LOAD_INTERPOLATED && interp % INTERPOLATED_MOD == 0) || type.equals("O")) {
			    // Load the data
			    double ra = Double.parseDouble(tokens[0].trim()) * 15d;
			    double dec = Double.parseDouble(tokens[1].trim());

			    double dist = 1 * Constants.AU_TO_U;

			    Vector3d point = Coordinates.sphericalToCartesian(Math.toRadians(ra), Math.toRadians(dec), dist, new Vector3d());
			    buffer.add(point);
			    n++;
			}

		    }
		}
		list.add(buffer);
		boundary.setBoundaries(list);
		boundaries.add(boundary);
	    } catch (IOException e) {
		Gdx.app.error(this.getClass().getSimpleName(), e.getLocalizedMessage());
	    }

	    EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "Constellations boundaries initialized with " + n + " points");

	} catch (Exception e) {
	    Gdx.app.error(this.getClass().getSimpleName(), e.getLocalizedMessage());
	}
	return boundaries;
    }
}
