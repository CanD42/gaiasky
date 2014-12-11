package gaia.cu9.ari.gaiaorbit.data.orbit;

import gaia.cu9.ari.gaiaorbit.util.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class OrbitDataWriter {
    /**
     * Dumps the current orbit data to the given file
     * @param filePath
     * @throws IOException
     */
    public static void writeOrbitData(String filePath, OrbitData data) throws IOException {
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

	File f = new File(filePath);
	if (f.exists() && f.isFile()) {
	    f.delete();
	}

	if (f.isDirectory()) {
	    throw new RuntimeException("File is directory: " + filePath);
	}

	f.createNewFile();

	FileWriter fw = new FileWriter(filePath);
	BufferedWriter bw = new BufferedWriter(fw);
	bw.write("#time X Y Z");
	bw.newLine();
	int n = data.x.size();

	for (int i = 0; i < n; i++) {
	    bw.write(df.format(data.time.get(i)) + " " + (data.x.get(i) * Constants.U_TO_KM) + " " + (data.y.get(i) * Constants.U_TO_KM) + " " + (data.z.get(i) * Constants.U_TO_KM));
	    bw.newLine();
	}

	bw.flush();
	bw.close();

    }
}
