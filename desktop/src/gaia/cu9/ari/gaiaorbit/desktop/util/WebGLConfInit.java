package gaia.cu9.ari.gaiaorbit.desktop.util;

import java.io.File;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
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

public class WebGLConfInit extends ConfInit {

    @Override
    public void initGlobalConf() throws Exception {
        this.webgl = true;

        GlobalConf.updateScaleFactor(1);

        VersionConf vc = new VersionConf();
        vc.initialize("0.706b", null, null, null, null, 0, 706, 0);

        PerformanceConf pc = new PerformanceConf();
        pc.initialize(false, 1);

        PostprocessConf ppc = new PostprocessConf();
        ppc.initialize(4, 0, 0, false, false, false, 0, 1);

        RuntimeConf rc = new RuntimeConf();
        rc.initialize(true, false, false, false, true, false, 8f, false, false);

        DataConf dc = new DataConf();
        dc.initialize("data/catalog-hyg.json", "data/data-wgl.json", true, 7.8f);

        ProgramConf prc = new ProgramConf();
        prc.initialize(false, false, "dark-green", "en-GB", false, StereoProfile.CROSSEYE);

        ComponentType[] cts = ComponentType.values();
        boolean[] VISIBILITY = new boolean[cts.length];
        VISIBILITY[ComponentType.Stars.ordinal()] = true;
        VISIBILITY[ComponentType.Atmospheres.ordinal()] = true;
        VISIBILITY[ComponentType.Planets.ordinal()] = true;
        VISIBILITY[ComponentType.Moons.ordinal()] = true;
        VISIBILITY[ComponentType.Orbits.ordinal()] = true;
        VISIBILITY[ComponentType.Satellites.ordinal()] = true;
        VISIBILITY[ComponentType.MilkyWay.ordinal()] = true;
        VISIBILITY[ComponentType.Asteroids.ordinal()] = true;
        VISIBILITY[ComponentType.Galaxies.ordinal()] = true;
        VISIBILITY[ComponentType.Labels.ordinal()] = false;
        VISIBILITY[ComponentType.Others.ordinal()] = true;
        SceneConf sc = new SceneConf();
        sc.initialize(1, 2500, 6f, 0f, 50, 2.1f, 1866f, 2286f, 13, true, false, 50.0f, VISIBILITY, 0, 0f, 1.6e-7f, 0f, 0.1f, 0.8f, false, 0.6f, 1.5f, false, 20f, 1e1f, -1f, true, 100, true, true, true, true);

        FrameConf fc = new FrameConf();

        ScreenConf scrc = new ScreenConf();

        ScreenshotConf shc = new ScreenshotConf();

        ControlsConf cc = new ControlsConf();
        cc.initialize("mappings/xbox360.controller", true);

        SpacecraftConf scc = new SpacecraftConf();
        scc.initialize(.5e7f, true, 1f);

        GlobalConf.initialize(vc, prc, sc, dc, rc, ppc, pc, fc, scrc, shc, cc, scc);

    }

    @Override
    public void persistGlobalConf(File propsFile) {
    }

}
