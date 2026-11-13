package org.delcom.ifs25013springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25013springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25013springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.delcom.ifs25013springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25013springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25013springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25013springbootcashflow.usecase.UserUseCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Pengaturan profil: ubah nama, ubah password (opsional), dan unggah foto profil.
 */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout {

    private final UserUseCase userUseCase;
    private final FinancePresenter presenter;
    private final FileStorage fileStorage;
    private final User user;

    private final TextField fullName = new TextField("Nama lengkap");
    private final PasswordField newPassword = new PasswordField("Password baru (kosongkan bila tidak diubah)");
    private final ProgressBar progressBar = new ProgressBar();
    private final Avatar avatar = new Avatar();

    public ProfileView(UserUseCase userUseCase, FinancePresenter presenter,
                       FileStorage fileStorage, IUserRepository userRepository) {
        this.userUseCase = userUseCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;
        this.user = AuthSession.currentUser(userRepository);

        if (user == null) {
            return;
        }

        setPadding(true);

        avatar.setName(user.getFullName());
        avatar.setHeight("96px");
        avatar.setWidth("96px");
        showPhoto(user.getPhotoPath());

        Span usernameLabel = new Span("Username: " + user.getUsername()
                + " | Peran: " + presenter.formatRole(user.getRole()));

        fullName.setValue(user.getFullName());
        fullName.setWidthFull();
        newPassword.setWidthFull();

        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        Button saveButton = new Button("Simpan Profil", VaadinIcon.CHECK.create(), event -> saveProfile());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        PageCard card = new PageCard("Profil Saya",
                avatar, usernameLabel, fullName, newPassword, buildPhotoUpload(), progressBar, saveButton);
        card.setMaxWidth("520px");
        add(card);
    }

    private Upload buildPhotoUpload() {
        UI ui = UI.getCurrent();
        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            try {
                String storedName = fileStorage.store(metadata.fileName(), new ByteArrayInputStream(data));
                User updated = userUseCase.updatePhoto(user.getId(), storedName);
                ui.access(() -> {
                    showPhoto(updated.getPhotoPath());
                    Toast.success("Foto profil berhasil diperbarui.");
                    ui.getPage().reload();
                });
            } catch (IOException | EntityNotFoundException ex) {
                ui.access(() -> Toast.error(presenter.showUploadFailed()));
            }
        }));
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        return upload;
    }

    private void showPhoto(String photoPath) {
        if (photoPath != null && !photoPath.isBlank()) {
            avatar.setImage(TransactionGrid.UPLOADS_URL + photoPath);
        }
    }

    private void saveProfile() {
        progressBar.setVisible(true);
        try {
            userUseCase.updateProfile(user.getId(), fullName.getValue(), newPassword.getValue());
            newPassword.clear();
            Toast.success(presenter.showProfileSaved());
            UI.getCurrent().getPage().reload();
        } catch (ValidationException | EntityNotFoundException ex) {
            Toast.error(presenter.showValidationError(ex.getMessage()));
        } finally {
            progressBar.setVisible(false);
        }
    }
}
