package com.lokoproject.mailing.web.beens;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DevelopmentException;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.components.AbstractMainWindow;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.components.mainwindow.AppWorkArea;
import com.haulmont.cuba.gui.components.mainwindow.FoldersPane;
import com.haulmont.cuba.gui.components.mainwindow.TopLevelWindowAttachListener;
import com.haulmont.cuba.gui.components.mainwindow.UserIndicator;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.xml.layout.LayoutLoaderConfig;
import com.haulmont.cuba.web.WebWindowManager;
import com.lokoproject.mailing.web.beens.notification.CubaWebClientNotificationPerformer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Anton on 23.06.2019.
 */

public class WindowManagerExt extends WebWindowManager {



    @Override
    public void createTopLevelWindow(WindowInfo windowInfo) {
        ui.beforeTopLevelWindowInit();

        String template = windowInfo.getTemplate();

        Window.TopLevelWindow topLevelWindow;

        Map<String, Object> params = Collections.emptyMap();
        if (template != null) {
            //noinspection unchecked
            topLevelWindow = (Window.TopLevelWindow) createWindow(windowInfo, OpenType.NEW_TAB, params,
                    LayoutLoaderConfig.getWindowLoaders(), true);
        } else {
            Class screenClass = windowInfo.getScreenClass();
            if (screenClass != null) {
                //noinspection unchecked
                topLevelWindow = (Window.TopLevelWindow) createWindowByScreenClass(windowInfo, params);
            } else {
                throw new DevelopmentException("Unable to load top level window");
            }
        }

        // detect work area
        Window windowImpl = ((Window.Wrapper) topLevelWindow).getWrappedWindow();

        if (topLevelWindow instanceof AbstractMainWindow) {
            AbstractMainWindow mainWindow = (AbstractMainWindow) topLevelWindow;

            CubaWebClientNotificationPerformer cubaWebClientNotificationPerformer= AppBeans.get(CubaWebClientNotificationPerformer.class);
            cubaWebClientNotificationPerformer.initByMainWindow(mainWindow);

            // bind system UI components to AbstractMainWindow
            ComponentsHelper.walkComponents(windowImpl, component -> {
                if (component instanceof AppWorkArea) {
                    mainWindow.setWorkArea((AppWorkArea) component);
                } else if (component instanceof UserIndicator) {
                    mainWindow.setUserIndicator((UserIndicator) component);
                } else if (component instanceof FoldersPane) {
                    mainWindow.setFoldersPane((FoldersPane) component);
                }

                return false;
            });
        }

        ui.setTopLevelWindow(topLevelWindow);

        // load menu
        ComponentsHelper.walkComponents(windowImpl, component -> {
            if (component instanceof TopLevelWindowAttachListener) {
                ((TopLevelWindowAttachListener) component).topLevelWindowAttached(topLevelWindow);
            }

            return false;
        });

        if (topLevelWindow instanceof Window.HasWorkArea) {
            AppWorkArea workArea = ((Window.HasWorkArea) topLevelWindow).getWorkArea();
            if (workArea != null) {
                workArea.addStateChangeListener(new AppWorkArea.StateChangeListener() {
                    @Override
                    public void stateChanged(AppWorkArea.State newState) {
                        if (newState == AppWorkArea.State.WINDOW_CONTAINER) {
                            initTabShortcuts();

                            // listener used only once
                            getConfiguredWorkArea(createWorkAreaContext(topLevelWindow)).removeStateChangeListener(this);
                        }
                    }
                });
            }
        }

        afterShowWindow(topLevelWindow);
    }
}
