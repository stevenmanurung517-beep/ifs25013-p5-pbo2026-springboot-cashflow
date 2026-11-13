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
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25013springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.delcom.ifs25013springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25013springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25013springbootcashflow.usecase.AuthUseCase;

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;
    private final FinancePresenter presenter;

    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final Button loginButton = new Button("Masuk");
    private final ProgressBar progressBar = new ProgressBar();

    public LoginView(AuthUseCase authUseCase, IUserRepository userRepository, FinancePresenter presenter) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;
        this.presenter = presenter;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        username.setId("login-username");
        password.setId("login-password");
        loginButton.setId("login-submit");

        username.setValueChangeMode(ValueChangeMode.EAGER);
        password.setValueChangeMode(ValueChangeMode.EAGER);

        username.setWidthFull();
        password.setWidthFull();
        loginButton.setWidthFull();
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.addClickShortcut(Key.ENTER);
        loginButton.addClickListener(event -> submit());

        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        Paragraph registerHint = new Paragraph("Belum punya akun? ");
        registerHint.add(new Anchor("register", "Daftar"));

        PageCard card = new PageCard("Masuk ke Cashflow",
                username, password, progressBar, loginButton, registerHint);
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
        loginButton.setEnabled(false);

        // Tarik nilai langsung dari DOM elemen Vaadin saat tombol diklik
        UI.getCurrent().getPage().executeJs(
            "const getVal = (id) => {" +
            "  const host = document.getElementById(id);" +
            "  if (!host) return '';" +
            "  if (host.value) return host.value;" +
            "  const lightInput = host.querySelector('input');" +
            "  if (lightInput && lightInput.value) return lightInput.value;" +
            "  const shadowInput = host.shadowRoot ? host.shadowRoot.querySelector('input') : null;" +
            "  return shadowInput ? shadowInput.value : '';" +
            "};" +
            "return JSON.stringify({ u: getVal('login-username'), p: getVal('login-password') });"
        ).then(String.class, json -> {
            try {
                String parsedUser = extractJsonField(json, "u");
                String parsedPass = extractJsonField(json, "p");

                String finalUser = (username.getValue() != null && !username.getValue().isBlank())
                        ? username.getValue() : parsedUser;
                String finalPass = (password.getValue() != null && !password.getValue().isBlank())
                        ? password.getValue() : parsedPass;

                User user = authUseCase.login(finalUser, finalPass);
                AuthSession.login(user);
                UI.getCurrent().navigate(FinanceView.class);
            } catch (ValidationException ex) {
                Toast.error(presenter.showValidationError(ex.getMessage()));
            } finally {
                progressBar.setVisible(false);
                loginButton.setEnabled(true);
            }
        });
    }

    private static String extractJsonField(String json, String key) {
        if (json == null) {
            return "";
        }
        String token = "\"" + key + "\":\"";
        int start = json.indexOf(token);
        if (start < 0) {
            return "";
        }
        start += token.length();
        int end = json.indexOf("\"", start);
        return end > start ? json.substring(start, end) : "";
    }
}