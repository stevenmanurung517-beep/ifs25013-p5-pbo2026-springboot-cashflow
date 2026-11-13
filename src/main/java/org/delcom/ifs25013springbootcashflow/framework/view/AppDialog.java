package org.delcom.ifs25013springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;

/**
 * Dialog standar aplikasi: header berwarna primer (#2563EB) dengan badge informatif,
 * footer abu-abu terang dengan tombol aksi di sisi kanan (gaya ada di styles.css, kelas app-dialog).
 */
class AppDialog extends Dialog {

    AppDialog(String title, String badgeText) {
        setHeaderTitle(title);
        addClassName("app-dialog");
        setWidth("460px");
        setMaxWidth("95vw");
        if (badgeText != null) {
            Span badge = new Span(badgeText);
            badge.getElement().getThemeList().add("badge");
            getHeader().add(badge);
        }
    }

    /** Menaruh tombol aksi di footer (rata kanan lewat CSS). */
    void setActions(Button... buttons) {
        getFooter().add(buttons);
    }
}
