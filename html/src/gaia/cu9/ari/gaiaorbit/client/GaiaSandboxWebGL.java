package gaia.cu9.ari.gaiaorbit.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.google.gwt.dom.client.Element;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.client.data.WebGLSceneGraphImplementationProvider;
import gaia.cu9.ari.gaiaorbit.client.format.GwtDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.client.format.GwtNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.client.render.WebGLPostProcessorFactory;
import gaia.cu9.ari.gaiaorbit.client.script.DummyFactory;
import gaia.cu9.ari.gaiaorbit.client.util.WebGLConfInitLite;
import gaia.cu9.ari.gaiaorbit.data.SceneGraphImplementationProvider;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.PostProcessorFactory;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.script.ScriptingFactory;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.concurrent.SingleThreadIndexer;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadIndexer;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;

public class GaiaSandboxWebGL extends GwtApplication implements IObserver {

    private GaiaSky application;

    @Override
    public GwtApplicationConfiguration getConfig() {
        NumberFormatFactory.initialize(new GwtNumberFormatFactory());
        DateFormatFactory.initialize(new GwtDateFormatFactory());
        ScriptingFactory.initialize(new DummyFactory());
        ThreadIndexer.initialize(new SingleThreadIndexer());
        SceneGraphImplementationProvider.initialize(new WebGLSceneGraphImplementationProvider());

        GwtApplicationConfiguration config = new GwtApplicationConfiguration(1024, 600);

        try {
            ConfInit.initialize(new WebGLConfInitLite());
            ConfInit.instance.initGlobalConf();
            config.antialiasing = GlobalConf.postprocess.POSTPROCESS_ANTIALIAS != 0 ? true : false;
        } catch (Exception e) {
            System.err.println("Error initializing GlobalConf");
            e.printStackTrace(System.err);
        }

        PostProcessorFactory.initialize(new WebGLPostProcessorFactory());

        return config;
    }

    @Override
    public ApplicationListener getApplicationListener() {
        return application;
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case FOCUS_CHANGED:
            String name = "";
            if (data[0] instanceof String) {
                name = (String) data[0];
            } else {
                CelestialBody cb = (CelestialBody) data[0];
                name = cb.name;
            }

            //            Element iframe = DOM.getElementById("wikip-info-ifr");
            //            iframe.setAttribute("scr", "https://en.m.wikipedia.org/wiki/" + name);
            //            reloadIFrame(iframe);
        }

    }

    protected native void reloadIFrame(Element iframeEl) /*-{
		iframeEl.contentWindow.location.reload(true);
    }-*/;

    @Override
    public ApplicationListener createApplicationListener() {
        EventManager.instance.subscribe(this, Events.FOCUS_CHANGED);
        application = new GaiaSky();
        return application;
    }
}