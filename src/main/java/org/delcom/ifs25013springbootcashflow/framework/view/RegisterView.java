package org.delcom.ifs25013springbootcashflow.framework.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25013springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25013springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25013springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25013springbootcashflow.usecase.AuthUseCase;

/**
 * Halaman registrasi pengguna baru (publik). Bila sudah login, dialihkan otomatis ke halaman utama.
 */
@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;
    private final FinancePresenter presenter;

    private final TextField fullName = new TextField("Nama lengkap");
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final Button registerButton = new Button("Daftar");
    private final ProgressBar progressBar = new ProgressBar();

    public RegisterView(AuthUseCase authUseCase, IUserRepository userRepository, FinancePresenter presenter) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;
        this.presenter = presenter;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        fullName.setWidthFull();
        username.setWidthFull();
        password.setWidthFull();
        registerButton.setWidthFull();
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickShortcut(Key.ENTER);
        registerButton.addClickListener(event -> submit());

        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        Paragraph loginHint = new Paragraph("Sudah punya akun? ");
        loginHint.add(new Anchor("login", "Masuk"));

        PageCard card = new PageCard("Daftar Akun Cashflow",
                fullName, username, password, progressBar, registerButton, loginHint);
        card.setWidth("380px");
        card.setMaxWidth("95vw");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository) != null) {
            event.forwardTo(FinanceView.class);
        }
    }

    private void submit() {
        progressBar.setVisible(true);
        registerButton.setEnabled(false);
        try {
            authUseCase.register(username.getValue(), password.getValue(), fullName.getValue());
            Toast.success("Registrasi berhasil. Silakan masuk.");
            UI.getCurrent().navigate(LoginView.class);
        } catch (ValidationException ex) {
            Toast.error(presenter.showValidationError(ex.getMessage()));
        } finally {
            progressBar.setVisible(false);
            registerButton.setEnabled(true);
        }
    }
}
