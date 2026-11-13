# ifs25013-p5-pbo2026-springboot-cashflow

Aplikasi web pencatat keuangan pribadi (**Cashflow**) - Praktikum 5 PBO (JDBC). Memakai **Spring Boot**, **Vaadin**,
**Clean Architecture**, **JDBC PostgreSQL**, autentikasi, profil, role (ADMIN / USER), dan upload bukti transaksi.

- Group: `org.delcom`
- Package: `org.delcom.ifs25013springbootcashflow`
- Java 25, Spring Boot 4.1.2, Vaadin 25.3.4
- Database runtime: PostgreSQL `pbo2026_cashflow`. Tes JDBC memakai H2 (profil `test`).

## Fitur

- Register, login, logout; sesi bertahan setelah refresh (cookie bertanda tangan + `LOGGED_OUT_KEY`)
- Tambah **Pemasukan** / **Pengeluaran** dengan **bukti gambar opsional** (klik bukti untuk zoom)
- Cari keterangan (case-insensitive, hanya data milik sendiri), urutkan lewat `SortOption` atau header kolom grid
- Kartu ringkasan: jumlah transaksi, total pemasukan, total pengeluaran, **saldo**
- Hapus dengan konfirmasi (hanya milik sendiri)
- Profil: nama, password, foto
- ADMIN: daftar pengguna dan peninjauan transaksi pengguna lain (read-only); admin juga punya cashflow sendiri
- Validasi: keterangan kosong, jumlah non-positif / non-numerik, ID tidak valid
- Format uang: `Rp 1,000,000.00`; saldo `Saldo: Rp 4,975,000.00`

## Struktur

```text
Jenkinsfile                          # ambil dari halaman Jenkins Grading di Delcom, taruh di root
.env / .env.example                  # APP_PORT, DB_*, ROOT_*, UPLOAD_DIR
src/main/java/org/delcom/ifs25013springbootcashflow/
├── App.java                         # @StyleSheet(Lumo)+styles.css, @PWA, DotEnv.load()
├── DotEnv.java
├── config/AppConfig.java            # composition root (@Bean)
├── domain/
│   ├── PasswordHasher.java          # SHA-256
│   ├── entity/                      # Transaction, TransactionType, User, Role, SortOption
│   ├── exception/                   # DomainException, EntityNotFoundException, ValidationException
│   └── repository/                  # IRepository<T, ID>, ITransactionRepository, IUserRepository
├── usecase/                         # AuthUseCase, FinanceUseCase, UserUseCase
├── adapter/
│   ├── presenter/FinancePresenter.java
│   ├── repository/                  # InMemory*, Jdbc*, SchemaInitializer
│   └── storage/FileStorage.java
└── framework/
    ├── auth/AuthSession.java
    └── view/                        # Login, Register, MainLayout, Finance, Profile, Users,
                                     # UserTransactions, ImagePreviewDialog, dll.
src/test/...                         # AppTest + unit test per layer + JDBC (H2)
```

Dependency Rule: `domain <- usecase <- adapter / framework`.

## Hak akses rute

| Rute | View | Akses |
| --- | --- | --- |
| `/login`, `/register` | LoginView, RegisterView | Publik (sudah login -> `/`) |
| `/uploads/**`, `/icons/**`, `styles.css` | statis | Publik |
| `/` | FinanceView | USER & ADMIN |
| `/profile` | ProfileView | USER & ADMIN |
| `/users`, `/user-transactions` | UsersView, UserTransactionsView | ADMIN saja (USER -> `/`) |

## Menjalankan

1. Buat database sekali saja:

   ```powershell
   $env:PGPASSWORD="password_anda"; & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -c "CREATE DATABASE pbo2026_cashflow;"
   ```

2. Sesuaikan `.env` (`DB_USERNAME`, `DB_PASSWORD`), lalu jalankan:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

3. Buka `http://localhost:8080` (atau sesuai `APP_PORT`). Login admin: `ROOT_USERNAME` / `ROOT_PASSWORD` di `.env`.

Build JAR (wajib profil production Vaadin):

```powershell
.\mvnw.cmd clean package -Pproduction
java -jar target\ifs25013-p5-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar
```

Test + coverage Jacoco 100% (view dan auth dikecualikan):

```powershell
.\mvnw.cmd clean verify
```

Laporan: `target/site/jacoco/index.html`.
