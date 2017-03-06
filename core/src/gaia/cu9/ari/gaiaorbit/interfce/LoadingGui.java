package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.GSEnumSet;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;

/**
 * Displays the loading screen.
 * @author Toni Sagrista
 *
 */
public class LoadingGui implements IGui {
    private Skin skin;
    /**
     * The user interface stage
     */
    protected Stage ui;
    protected Table center;

    protected NotificationsInterface notificationsInterface;
    /** Lock object for synchronization **/
    private Object lock;

    public LoadingGui() {
        lock = new Object();
    }

    @Override
    public void initialize(AssetManager assetManager) {

        // User interface
        ui = new Stage(new ScreenViewport(), GlobalResources.spriteBatch);
        skin = GlobalResources.skin;

        center = new Table();
        center.setFillParent(true);
        center.center();

        Image logo = new Image(new Texture(Gdx.files.internal("img/gaiaskylogo.png")));

        center.add(logo).center();
        center.row().padBottom(30);
        center.add(new Label(I18n.bundle.get("notif.loading.wait"), skin, "header"));
        center.row();

        // MESSAGE INTERFACE - BOTTOM
        notificationsInterface = new NotificationsInterface(skin, lock, false, false);
        center.add(notificationsInterface);

        rebuildGui();

    }

    @Override
    public void doneLoading(AssetManager assetManager) {
    }

    public void rebuildGui() {
        if (ui != null) {
            ui.clear();
            ui.addActor(center);

        }
    }

    @Override
    public void dispose() {
        notificationsInterface.dispose();
        ui.dispose();
    }

    @Override
    public void update(float dt) {
        ui.act(dt);
    }

    @Override
    public void render(int rw, int rh) {
        synchronized (lock) {
            try {
                ui.draw();
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }

    @Override
    public void resize(final int width, final int height) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                resizeImmediate(width, height);
            }
        });
    }

    @Override
    public void resizeImmediate(final int width, final int height) {
        ui.getViewport().update(width, height, true);
        rebuildGui();
    }

    @Override
    public boolean cancelTouchFocus() {
        if (ui.getKeyboardFocus() != null || ui.getScrollFocus() != null) {
            ui.setScrollFocus(null);
            ui.setKeyboardFocus(null);
            return true;
        }
        return false;
    }

    @Override
    public Stage getGuiStage() {
        return ui;
    }

    @Override
    public void setSceneGraph(ISceneGraph sg) {
    }

    @Override
    public void setVisibilityToggles(ComponentType[] entities, GSEnumSet<ComponentType> visible) {
    }

    @Override
    public Actor findActor(String name) {
        return ui.getRoot().findActor(name);
    }

}
