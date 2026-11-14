package org.delcom.ifs25013springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.delcom.ifs25013springbootcashflow.adapter.presenter.FinancePresenter;

/**
 * Empat kartu ringkasan: jumlah transaksi, total pemasukan, total pengeluaran, dan saldo.
 */
class SummaryCards extends HorizontalLayout {

    private final FinancePresenter presenter;
    private final Span totalCount = new Span("0");
    private final Span totalIncome = new Span();
    private final Span totalExpense = new Span();
    private final Span balance = new Span();

    SummaryCards(FinancePresenter presenter) {
        this.presenter = presenter;
        add(
                createCard("Jumlah Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                createCard("Total Pemasukan", totalIncome, "#DCFCE7", "#16A34A", "#166534"),
                createCard("Total Pengeluaran", totalExpense, "#FEE2E2", "#DC2626", "#991B1B"),
                createCard("Saldo", balance, "#FEF3C7", "#D97706", "#92400E")
        );
        setWidthFull();
        setSpacing(true);
        getStyle().set("align-items", "stretch");
        getStyle().set("flex-wrap", "wrap");
    }

    void update(int count, long income, long expense, long currentBalance) {
        totalCount.setText(String.valueOf(count));
        totalIncome.setText(presenter.formatMoney(income));
        totalExpense.setText(presenter.formatMoney(expense));
        balance.setText(presenter.formatBalance(currentBalance));
    }

    private Div createCard(String label, Span value, String background, String accent, String valueColor) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                // Warna gelap agar kontras teks lolos Axe (minimal 4.5:1)
                .set("color", valueColor);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("line-height", "1.1")
                .set("color", valueColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "180px")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }
}