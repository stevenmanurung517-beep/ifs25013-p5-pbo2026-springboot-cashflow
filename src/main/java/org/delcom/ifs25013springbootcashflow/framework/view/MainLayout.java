package org.delcom.ifs25013springbootcashflow.framework.view;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25013springbootcashflow.framework.auth.AuthSession;

/**
 * Layout utama untuk semua halaman terproteksi: sidebar navigasi dan menu avatar (Profil / Keluar).
 * Juga menjadi penjaga login global: tanpa sesi aktif, navigasi dialihkan ke /login.
 */
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;

    public MainLayout(IUserRepository userRepository) {
        this.userRepository = userRepository;
        User user = AuthSession.currentUser(userRepository);
        if (user != null) {
            build(user);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository) == null) {
            event.forwardTo(LoginView.class);
        }
    }

    /** Aksesibilitas: konten halaman aktif menjadi landmark <main>. */
    @Override
    public void showRouterLayoutContent(HasElement content) {
        super.showRouterLayoutContent(content);
        if (content != null) {
            content.getElement().setAttribute("role", "main");
        }
    }

    private void build(User user) {
        H1 title = new H1("Cashflow");
        title.addClassName("app-title");

        Span spacer = new Span();
        spacer.getStyle().set("flex", "1");

        // Aksesibilitas: header dibungkus landmark banner
        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title, spacer, buildAvatarMenu(user));
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(false);
        header.getElement().setAttribute("role", "banner");

        addToNavbar(header);
        addToDrawer(buildNavigation(user));
        setPrimarySection(Section.DRAWER);
    }

    private SideNav buildNavigation(User user) {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Transaksi", FinanceView.class, VaadinIcon.WALLET.create()));
        nav.addItem(new SideNavItem("Profil", ProfileView.class, VaadinIcon.USER.create()));
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Pengguna", UsersView.class, VaadinIcon.USERS.create()));
        }
        return nav;
    }

    private MenuBar buildAvatarMenu(User user) {
        Avatar avatar = new Avatar(user.getFullName());
        String photoPath = user.getPhotoPath();
        if (photoPath != null && !photoPath.isBlank()) {
            avatar.setImage(TransactionGrid.UPLOADS_URL + photoPath);
        }

        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        var root = menuBar.addItem(avatar);
        root.getSubMenu().addItem("Profil", event -> UI.getCurrent().navigate(ProfileView.class));
        root.getSubMenu().addItem("Keluar", event -> logout());
        return menuBar;
    }

    private void logout() {
        AuthSession.logout();
        UI.getCurrent().getPage().setLocation("/login");
    }
}