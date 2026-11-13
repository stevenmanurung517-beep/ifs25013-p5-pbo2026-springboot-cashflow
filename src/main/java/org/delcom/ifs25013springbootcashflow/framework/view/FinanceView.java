package org.delcom.ifs25013springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25013springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25013springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25013springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25013springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.delcom.ifs25013springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25013springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25013springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25013springbootcashflow.usecase.FinanceUseCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Dashboard transaksi milik pengguna yang sedang login: kartu ringkasan, cari, urut,
 * tambah pemasukan/pengeluaran (dengan bukti opsional), zoom bukti, dan hapus dengan konfirmasi.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {

    /** Aksi penambahan transaksi (pemasukan atau pengeluaran) dari dialog form. */
    @FunctionalInterface
    private interface TransactionAdder {
        Transaction add(int userId, String description, String amount, String proofPath)
                throws ValidationException;
    }

    /** Berkas bukti yang sudah diunggah di dialog tetapi belum disimpan ke storage. */
    private static final class PendingProof {
        private String fileName;
        private byte[] content;
    }

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;
    private final FileStorage fileStorage;

    private final TransactionGrid grid;
    private final SummaryCards summaryCards;
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortBox = new ComboBox<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span();

    /** Pengguna yang sedang login; null bila belum login (MainLayout akan mengalihkan ke /login). */
    private final User user;

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter,
                       FileStorage fileStorage, IUserRepository userRepository) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;
        this.grid = new TransactionGrid(presenter, this::openDeleteDialog);
        this.summaryCards = new SummaryCards(presenter);
        this.user = AuthSession.currentUser(userRepository);

        if (user == null) {
            return;
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        grid.setEmptyStateComponent(emptyState);
        configureToolbar();

        add(new H2("Catatan Keuangan: " + user.getFullName()),
                summaryCards, buildToolbar(), statusLabel, grid);
        refreshAll();
    }

    // ------------------------------------------------------------------
    // Toolbar
    // ------------------------------------------------------------------

    private void configureToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        sortBox.setPlaceholder("Urutkan...");
        sortBox.setItems(SortOption.values());
        sortBox.setItemLabelGenerator(presenter::formatSortOption);
        sortBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                sortTransactions(event.getValue());
            }
        });
    }

    private HorizontalLayout buildToolbar() {
        Button incomeButton = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                event -> openAddDialog("Tambah Pemasukan", "Pemasukan", financeUseCase::addIncome));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button expenseButton = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                event -> openAddDialog("Tambah Pengeluaran", "Pengeluaran", financeUseCase::addExpense));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> searchTransactions());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, searchButton, sortBox, refreshButton, incomeButton, expenseButton);
        toolbar.setWidthFull();
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        toolbar.setFlexGrow(1, searchField);
        toolbar.getStyle().set("flex-wrap", "wrap");
        return toolbar;
    }

    // ------------------------------------------------------------------
    // Dialog
    // ------------------------------------------------------------------

    private void openAddDialog(String title, String badge, TransactionAdder adder) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();

        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setPlaceholder("contoh: 1000000");
        amountField.setWidthFull();

        PendingProof pending = new PendingProof();
        Span uploadInfo = new Span("Bukti transaksi (opsional): belum ada berkas");
        UI ui = UI.getCurrent();
        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            pending.fileName = metadata.fileName();
            pending.content = data;
            ui.access(() -> uploadInfo.setText("Bukti terpilih: " + metadata.fileName()));
        }));
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        AppDialog dialog = new AppDialog(title, badge);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());

        saveButton.addClickListener(event -> {
            progressBar.setVisible(true);
            saveButton.setEnabled(false);
            String storedName = null;
            try {
                if (pending.content != null) {
                    storedName = fileStorage.store(pending.fileName, new ByteArrayInputStream(pending.content));
                }
                Transaction created = adder.add(
                        user.getId(), descriptionField.getValue(), amountField.getValue(), storedName);
                Toast.success(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                discardStoredFile(storedName);
                Toast.error(presenter.showValidationError(ex.getMessage()));
            } catch (IOException ex) {
                Toast.error(presenter.showUploadFailed());
            } finally {
                progressBar.setVisible(false);
                saveButton.setEnabled(true);
            }
        });

        VerticalLayout form = new VerticalLayout(descriptionField, amountField, uploadInfo, upload, progressBar);
        form.setPadding(false);
        dialog.add(form);
        dialog.setActions(cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph(
                "Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                        + "\" (ID " + transaction.getId() + ")?");
        message.getStyle().set("margin", "0");

        AppDialog dialog = new AppDialog("Menghapus Transaksi", "Konfirmasi");
        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());

        dialog.add(message);
        dialog.setActions(cancelButton, confirmButton);
        dialog.open();
    }

    // ------------------------------------------------------------------
    // Aksi
    // ------------------------------------------------------------------

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransaction(user.getId(), transaction.getId());
            Toast.success(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            Toast.error(presenter.showRemoveFailed(ex.getId()));
        } catch (ValidationException ex) {
            Toast.error(presenter.showValidationError(ex.getMessage()));
        }
    }

    private void discardStoredFile(String storedName) {
        if (storedName == null) {
            return;
        }
        try {
            fileStorage.delete(storedName);
        } catch (IOException ignored) {
            // Berkas yatim tidak membahayakan data; abaikan.
        }
    }

    private void searchTransactions() {
        String keyword = searchField.getValue();
        if (keyword.isBlank()) {
            refreshAll();
            return;
        }
        showTransactions(
                financeUseCase.searchTransactions(user.getId(), keyword),
                presenter.showSearchHeader(keyword),
                presenter.showNotFoundMessage());
    }

    private void sortTransactions(SortOption option) {
        showTransactions(
                financeUseCase.sortTransactions(user.getId(), option),
                presenter.showSortHeader(option),
                presenter.showEmptyMessage());
    }

    private void refreshAll() {
        showTransactions(
                financeUseCase.getTransactions(user.getId()),
                presenter.showListHeader(),
                presenter.showEmptyMessage());
        sortBox.clear();
        searchField.clear();
    }

    private void showTransactions(List<Transaction> transactions, String title, String emptyMessage) {
        emptyState.setText(emptyMessage);
        grid.setItems(transactions);
        summaryCards.update(
                financeUseCase.getTransactions(user.getId()).size(),
                financeUseCase.getTotalIncome(user.getId()),
                financeUseCase.getTotalExpense(user.getId()),
                financeUseCase.getBalance(user.getId()));
        statusLabel.setText(transactions.isEmpty() ? title + " — " + emptyMessage : title);
    }
}
