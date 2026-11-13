package org.delcom.ifs25013springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.delcom.ifs25013springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25013springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25013springbootcashflow.domain.entity.Transaction;

import java.util.Comparator;
import java.util.function.Consumer;

/**
 * Grid transaksi yang dipakai bersama oleh halaman pengguna (ada tombol hapus)
 * dan halaman admin (read-only, tanpa kolom tindakan).
 * Kolom Jumlah dan Tipe diurutkan memakai comparator dari {@link SortOption}.
 */
class TransactionGrid extends Grid<Transaction> {

    /** Awalan URL berkas unggahan yang dilayani Spring MVC. */
    static final String UPLOADS_URL = "/uploads/";

    private final FinancePresenter presenter;

    /**
     * @param onDelete aksi tombol Hapus; null untuk grid read-only
     */
    TransactionGrid(FinancePresenter presenter, Consumer<Transaction> onDelete) {
        super(Transaction.class, false);
        this.presenter = presenter;

        addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getId));

        addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        addColumn(presenter::formatAmount)
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END)
                .setSortable(true)
                .setComparator(SortOption.AMOUNT_ASC.comparator());

        addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe")
                .setKey("type")
                .setWidth("160px")
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(SortOption.INCOME_FIRST.comparator());

        addComponentColumn(this::createProofThumbnail)
                .setHeader("Bukti")
                .setKey("proof")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        if (onDelete != null) {
            addComponentColumn(transaction -> createActions(transaction, onDelete))
                    .setHeader("Tindakan")
                    .setKey("tindakan")
                    .setAutoWidth(true)
                    .setFlexGrow(0)
                    .setSortable(false);
        }

        GridStyles.apply(this);
    }

    private Span createTypeBadge(Transaction transaction) {
        Span badge = new Span(presenter.formatType(transaction));
        badge.getElement().getThemeList().add("badge");
        badge.getElement().getThemeList().add(transaction.isIncome() ? "success" : "error");
        return badge;
    }

    /** Thumbnail bukti; klik membuka {@link ImagePreviewDialog}. Tanpa bukti menampilkan "-". */
    private Component createProofThumbnail(Transaction transaction) {
        String proofPath = transaction.getProofPath();
        if (proofPath == null || proofPath.isBlank()) {
            return new Span("-");
        }
        String url = UPLOADS_URL + proofPath;
        Image thumbnail = new Image(url, "Bukti transaksi " + transaction.getId());
        thumbnail.setHeight("40px");
        thumbnail.getStyle().set("cursor", "pointer").set("border-radius", "6px");
        thumbnail.addClickListener(event ->
                new ImagePreviewDialog("Bukti Transaksi #" + transaction.getId(), url).open());
        return thumbnail;
    }

    private HorizontalLayout createActions(Transaction transaction, Consumer<Transaction> onDelete) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(),
                event -> onDelete.accept(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        return new HorizontalLayout(deleteButton);
    }
}