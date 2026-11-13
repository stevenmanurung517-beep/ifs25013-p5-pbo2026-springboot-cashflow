package org.delcom.ifs25013springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25013springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25013springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25013springbootcashflow.usecase.UserUseCase;

import java.util.Comparator;
import java.util.Map;

/**
 * Daftar seluruh pengguna (khusus ADMIN). Role USER yang membuka halaman ini dialihkan ke /.
 */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    private final FinancePresenter presenter;
    private final User currentUser;
    private final Grid<User> grid = new Grid<>(User.class, false);

    public UsersView(UserUseCase userUseCase, FinancePresenter presenter, IUserRepository userRepository) {
        this.presenter = presenter;
        this.currentUser = AuthSession.currentUser(userRepository);

        if (currentUser == null || !currentUser.isAdmin()) {
            return;
        }

        setPadding(true);
        configureGrid();
        add(new PageCard("Daftar Pengguna", grid));
        grid.setItems(userUseCase.getAllUsers());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (currentUser != null && !currentUser.isAdmin()) {
            event.forwardTo(FinanceView.class);
        }
    }

    private void configureGrid() {
        grid.addColumn(User::getId)
                .setHeader("ID").setKey("id").setAutoWidth(true).setFlexGrow(0)
                .setSortable(true).setComparator(Comparator.comparing(User::getId));
        grid.addColumn(User::getUsername)
                .setHeader("Username").setKey("username").setSortable(true)
                .setComparator(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(User::getFullName)
                .setHeader("Nama lengkap").setKey("fullName").setFlexGrow(1).setSortable(true)
                .setComparator(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER));
        grid.addComponentColumn(user -> {
            Span badge = new Span(presenter.formatRole(user.getRole()));
            badge.getElement().getThemeList().add("badge");
            if (!user.isAdmin()) {
                badge.getElement().getThemeList().add("contrast");
            }
            return badge;
        }).setHeader("Peran").setKey("role").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(user -> {
            Button open = new Button("Lihat Transaksi", VaadinIcon.EYE.create(), event ->
                    UI.getCurrent().navigate("user-transactions",
                            QueryParameters.simple(Map.of("userId", String.valueOf(user.getId())))));
            open.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            return open;
        }).setHeader("Tindakan").setKey("tindakan").setAutoWidth(true).setFlexGrow(0);
        GridStyles.apply(grid);
    }
}