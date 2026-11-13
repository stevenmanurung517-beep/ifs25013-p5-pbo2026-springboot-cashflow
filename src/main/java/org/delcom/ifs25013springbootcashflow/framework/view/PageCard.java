package org.delcom.ifs25013springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;

/**
 * Kartu konten halaman: judul dan isi dalam satu wadah dengan gaya seragam.
 */
class PageCard extends Div {

    PageCard(String title, Component... content) {
        addClassName("page-card");
        add(new H2(title));
        add(content);
    }
}
