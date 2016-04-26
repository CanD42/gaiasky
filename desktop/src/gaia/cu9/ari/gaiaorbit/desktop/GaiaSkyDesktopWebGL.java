package gaia.cu9.ari.gaiaorbit.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.data.SceneGraphImplementationProvider;
import gaia.cu9.ari.gaiaorbit.data.WebGLSceneGraphImplementationProvider;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.render.WebGLPostProcessorFactory;
import gaia.cu9.ari.gaiaorbit.desktop.util.DesktopNetworkChecker;
import gaia.cu9.ari.gaiaorbit.desktop.util.WebGLConfInitLite;
import gaia.cu9.ari.gaiaorbit.interfce.NetworkCheckerManager;
import gaia.cu9.ari.gaiaorbit.render.PostProcessorFactory;
import gaia.cu9.ari.gaiaorbit.script.DummyFactory;
import gaia.cu9.ari.gaiaorbit.script.ScriptingFactory;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.concurrent.SingleThreadIndexer;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadIndexer;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

public class GaiaSkyDesktopWebGL {

    public static void main(String[] args) throws Exception {
        NumberFormatFactory.initialize(new DesktopNumberFormatFactory());
        DateFormatFactory.initialize(new DesktopDateFormatFactory());
        ScriptingFactory.initialize(new DummyFactory());
        ConfInit.initialize(new WebGLConfInitLite());
        PostProcessorFactory.initialize(new WebGLPostProcessorFactory());
        ThreadIndexer.initialize(new SingleThreadIndexer());
        SceneGraphImplementationProvider.initialize(new WebGLSceneGraphImplementationProvider());
        NetworkCheckerManager.initialize(new DesktopNetworkChecker());

        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        LwjglApplicationConfiguration.disableAudio = true;
        cfg.title = GlobalConf.getFullApplicationName();
        cfg.fullscreen = false;
        cfg.resizable = false;
        cfg.width = 1024;
        cfg.height = 600;
        cfg.samples = MathUtilsd.clamp(GlobalConf.postprocess.POSTPROCESS_ANTIALIAS, 0, 16);
        cfg.vSyncEnabled = false;
        cfg.foregroundFPS = 0;
        cfg.backgroundFPS = 0;
        cfg.addIcon("icon/ic_launcher.png", Files.FileType.Internal);

        // Launch app
        new LwjglApplication(new GaiaSky(), cfg);
    }
}
