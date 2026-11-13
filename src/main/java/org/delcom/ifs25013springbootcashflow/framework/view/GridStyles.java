package org.delcom.ifs25013springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

/**
 * Gaya grid yang konsisten di semua halaman.
 */
final class GridStyles {

    private GridStyles() {
    }

    static void apply(Grid<?> grid) {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setMinHeight("320px");
        grid.setWidthFull();
    }
}
