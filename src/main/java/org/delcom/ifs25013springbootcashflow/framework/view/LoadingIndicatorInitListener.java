package org.delcom.ifs25013springbootcashflow.framework.view;

import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Mengatur indikator loading global Vaadin agar responsif:
 * muncul setelah 150 ms, berubah setelah 600 ms, dan 1500 ms.
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            LoadingIndicatorConfiguration configuration = uiEvent.getUI().getLoadingIndicatorConfiguration();
            configuration.setFirstDelay(150);
            configuration.setSecondDelay(600);
            configuration.setThirdDelay(1500);
        });
    }
}