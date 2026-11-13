package org.delcom.ifs25013springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25013springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.delcom.ifs25013springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25013springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25013springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25013springbootcashflow.usecase.UserUseCase;

import java.util.Optional;

/**
 * Peninjauan transaksi pengguna lain (khusus ADMIN, read-only).
 * Dibuka dari halaman Pengguna lewat {@code /user-transactions?userId=<id>}.
 */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {

    private final FinanceUseCase financeUseCase;
    private final UserUseCase userUseCase;
    private final FinancePresenter presenter;
    private final User currentUser;

    public UserTransactionsView(FinanceUseCase financeUseCase, UserUseCase userUseCase,
                                FinancePresenter presenter, IUserRepository userRepository) {
        this.financeUseCase = financeUseCase;
        this.userUseCase = userUseCase;
        this.presenter = presenter;
        this.currentUser = AuthSession.currentUser(userRepository);
        setPadding(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (currentUser == null) {
            return; // MainLayout mengalihkan ke /login
        }
        if (!currentUser.isAdmin()) {
            event.forwardTo(FinanceView.class);
            return;
        }
        Optional<String> userIdParam = event.getLocation().getQueryParameters().getSingleParameter("userId");
        if (userIdParam.isEmpty()) {
            event.forwardTo(UsersView.class);
            return;
        }
        try {
            showTransactionsOf(userUseCase.getUser(Integer.parseInt(userIdParam.get())));
        } catch (NumberFormatException | EntityNotFoundException ex) {
            Toast.error(presenter.showValidationError("Pengguna tidak ditemukan"));
            event.forwardTo(UsersView.class);
        }
    }

    private void showTransactionsOf(User owner) {
        removeAll();

        Button back = new Button("Kembali ke daftar pengguna", VaadinIcon.ARROW_LEFT.create(),
                event -> getUI().ifPresent(ui -> ui.navigate(UsersView.class)));

        SummaryCards summaryCards = new SummaryCards(presenter);
        summaryCards.update(
                financeUseCase.getTransactions(owner.getId()).size(),
                financeUseCase.getTotalIncome(owner.getId()),
                financeUseCase.getTotalExpense(owner.getId()),
                financeUseCase.getBalance(owner.getId()));

        TransactionGrid grid = new TransactionGrid(presenter, null);
        grid.setItems(financeUseCase.getTransactions(owner.getId()));

        add(back,
                new H2("Transaksi " + owner.getFullName() + " (" + owner.getUsername() + ")"),
                new Paragraph("Mode baca saja: admin tidak dapat mengubah atau menghapus transaksi pengguna lain."),
                summaryCards,
                grid);
    }
}
