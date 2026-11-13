package org.delcom.ifs25013springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Modal pratinjau gambar dengan zoom interaktif: klik gambar untuk toggle 1.0x dan 1.8x,
 * atau pakai tombol Perkecil / Perbesar / Tutup di footer (rata kanan).
 */
class ImagePreviewDialog extends AppDialog {

    private static final double MIN_ZOOM = 0.6;
    private static final double MAX_ZOOM = 3.0;
    private static final double STEP = 0.3;
    private static final double CLICK_ZOOM = 1.8;

    private final Image image;
    private double zoom = 1.0;

    ImagePreviewDialog(String title, String imageUrl) {
        super(title, "Pratinjau");
        setWidth("720px");

        image = new Image(imageUrl, title);
        image.getStyle()
                .set("width", "100%")
                .set("cursor", "zoom-in");
        image.addClickListener(event -> setZoom(zoom == 1.0 ? CLICK_ZOOM : 1.0));

        Div viewport = new Div(image);
        viewport.getStyle()
                .set("overflow", "auto")
                .set("max-height", "65vh")
                .set("text-align", "center");
        add(viewport);

        Button zoomOut = new Button("Perkecil", VaadinIcon.SEARCH_MINUS.create(),
                event -> setZoom(zoom - STEP));
        Button zoomIn = new Button("Perbesar", VaadinIcon.SEARCH_PLUS.create(),
                event -> setZoom(zoom + STEP));
        Button close = new Button("Tutup", VaadinIcon.CLOSE.create(), event -> close());
        setActions(zoomOut, zoomIn, close);
    }

    private void setZoom(double value) {
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, value));
        image.getStyle().set("width", (zoom * 100) + "%");
        image.getStyle().set("cursor", zoom == 1.0 ? "zoom-in" : "zoom-out");
    }
}
