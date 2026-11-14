package org.delcom.ifs25013springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;

/**
 * Kartu konten halaman: judul dan isi dalam satu wadah dengan gaya seragam.
 */
class PageCard extends Div {

    /** Kartu biasa (judul H2), dipakai di halaman yang sudah punya H1 dari MainLayout. */
    PageCard(String title, Component... content) {
        this(title, false, content);
    }

    /** Bila pageHeading = true, judul memakai H1 (untuk halaman login/daftar tanpa MainLayout). */
    PageCard(String title, boolean pageHeading, Component... content) {
        addClassName("page-card");
        add(pageHeading ? new H1(title) : new H2(title));
        add(content);
    }
}