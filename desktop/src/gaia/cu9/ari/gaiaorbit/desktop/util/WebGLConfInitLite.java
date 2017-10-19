package gaia.cu9.ari.gaiaorbit.desktop.util;

import java.io.File;
import java.util.Date;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ControlsConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.DataConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.FrameConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.PerformanceConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.PostprocessConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.RuntimeConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.SceneConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ScreenConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ScreenshotConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.SpacecraftConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.VersionConf;

public class WebGLConfInitLite extends ConfInit {

    @Override
    public void initGlobalConf() throws Exception {
        this.webgl = true;

        GlobalConf.updateScaleFactor(1);
        Constants.focalplane = true;
        Constants.webgl = true;

        VersionConf vc = new VersionConf();
        vc.initialize("0.706b", new Date(), null, null, null);

        PerformanceConf pc = new PerformanceConf();
        pc.initialize(false, 1);

        PostprocessConf ppc = new PostprocessConf();
        ppc.initialize(4, 0, 0, false, false, false, 0, 1);

        RuntimeConf rc = new RuntimeConf();
        rc.initialize(true, false, true, false, false, false, 20, true, false);

        DataConf dc = new DataConf();
        dc.initialize("data/catalog-wgl-fp.json", "data/data-wgl-fp.json", true, 20f, true);

        ProgramConf prc = new ProgramConf();
        prc.initialize(false, false, "dark-green", "en-GB", false, StereoProfile.CROSSEYE);

        ComponentType[] cts = ComponentType.values();
        boolean[] VISIBILITY = new boolean[cts.length];
        VISIBILITY[ComponentType.Stars.ordinal()] = true;
        VISIBILITY[ComponentType.Atmospheres.ordinal()] = false;
        VISIBILITY[ComponentType.Planets.ordinal()] = true;
        VISIBILITY[ComponentType.Moons.ordinal()] = false;
        VISIBILITY[ComponentType.Orbits.ordinal()] = false;
        VISIBILITY[ComponentType.Satellites.ordinal()] = true;
        VISIBILITY[ComponentType.MilkyWay.ordinal()] = true;
        VISIBILITY[ComponentType.Asteroids.ordinal()] = false;
        VISIBILITY[ComponentType.Galaxies.ordinal()] = false;
        VISIBILITY[ComponentType.Labels.ordinal()] = true;
        VISIBILITY[ComponentType.Others.ordinal()] = true;

        SceneConf sc = new SceneConf();
        sc.initialize(1, 2500, 6f, 0f, 50, 2.1f, 1866f, 2286f, 13, true, false, 7.0f, VISIBILITY, 0, 0f, 1.6e-7f, 0f, 0.1f, 1f, false, 0.610865f, 1.0472f, false, 20f, 1e1f, -1f, true, 100, true, true, true, true, true, 512);

        FrameConf fc = new FrameConf();

        ScreenConf scrc = new ScreenConf();

        ScreenshotConf shc = new ScreenshotConf();

        ControlsConf cc = new ControlsConf();
        cc.initialize("mappings/xbox360.controller", true);

        SpacecraftConf scc = new SpacecraftConf();
        scc.initialize(.5e7f, true, 1f, false);

        GlobalConf.initialize(vc, prc, sc, dc, rc, ppc, pc, fc, scrc, shc, cc, scc);
    }

    @Override
    public void persistGlobalConf(File propsFile) {
    }

}
