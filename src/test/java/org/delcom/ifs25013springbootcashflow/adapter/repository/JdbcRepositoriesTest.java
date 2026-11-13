package org.delcom.ifs25013springbootcashflow.adapter.repository;

import org.delcom.ifs25013springbootcashflow.domain.entity.Role;
import org.delcom.ifs25013springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tes repository JDBC memakai H2 (mode PostgreSQL), bukan PostgreSQL sungguhan. */
class JdbcRepositoriesTest {

    private DataSource dataSource;
    private JdbcUserRepository users;
    private JdbcTransactionRepository transactions;

    @BeforeEach
    void setUp() {
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        h2.setPassword("");
        dataSource = h2;
        SchemaInitializer.initialize(dataSource);
        SchemaInitializer.initialize(dataSource); // idempoten: CREATE TABLE IF NOT EXISTS
        users = new JdbcUserRepository(dataSource);
        transactions = new JdbcTransactionRepository(dataSource);
    }

    @Test
    void userCrudAndLookup() {
        User saved = users.save("steven", "hash", "Steven M", Role.USER);
        users.save("admin", "hash2", "Administrator", Role.ADMIN);

        assertTrue(saved.getId() > 0);
        assertEquals(2, users.findAll().size());
        assertEquals("Steven M", users.findById(saved.getId()).orElseThrow().getFullName());
        assertEquals(Role.ADMIN, users.findByUsername("admin").orElseThrow().getRole());
        assertTrue(users.findByUsername("tidak-ada").isEmpty());
        assertTrue(users.findById(999).isEmpty());
        assertEquals(1, users.findBy(User::isAdmin).size());

        saved.changeFullName("Nama Baru");
        saved.changePasswordHash("hash-baru");
        saved.changePhotoPath("foto.png");
        users.update(saved);

        User reloaded = users.findById(saved.getId()).orElseThrow();
        assertEquals("Nama Baru", reloaded.getFullName());
        assertEquals("hash-baru", reloaded.getPasswordHash());
        assertEquals("foto.png", reloaded.getPhotoPath());

        assertTrue(users.deleteById(saved.getId()));
        assertFalse(users.deleteById(saved.getId()));
    }

    @Test
    void usernameMustBeUnique() {
        users.save("u", "h", "N", Role.USER);
        assertThrows(IllegalStateException.class, () -> users.save("u", "h2", "N2", Role.USER));
    }

    @Test
    void transactionCrudScopedByUser() {
        User owner = users.save("a", "h", "A", Role.USER);
        User other = users.save("b", "h", "B", Role.USER);

        Transaction income = transactions.save(owner.getId(), "Gaji", 5_000_000L, TransactionType.INCOME, "b.png");
        Transaction expense = transactions.save(owner.getId(), "Makan", 25_000L, TransactionType.EXPENSE, null);
        transactions.save(other.getId(), "Lain", 1L, TransactionType.INCOME, null);

        assertTrue(income.getId() > 0);
        assertEquals(3, transactions.findAll().size());
        List<Transaction> mine = transactions.findByUserId(owner.getId());
        assertEquals(List.of("Gaji", "Makan"), mine.stream().map(Transaction::getDescription).toList());
        assertEquals("b.png", mine.get(0).getProofPath());
        assertNull(mine.get(1).getProofPath());
        assertEquals(TransactionType.EXPENSE, mine.get(1).getType());
        assertEquals(25_000L, mine.get(1).getAmount());
        assertEquals(expense.getId(), transactions.findById(expense.getId()).orElseThrow().getId());
        assertEquals(1, transactions.findBy(Transaction::isIncome).stream()
                .filter(t -> t.getUserId() == owner.getId()).count());

        income.changeDescription("Gaji Baru");
        income.changeAmount(6_000_000L);
        income.changeProofPath(null);
        transactions.update(income);
        Transaction reloaded = transactions.findById(income.getId()).orElseThrow();
        assertEquals("Gaji Baru", reloaded.getDescription());
        assertEquals(6_000_000L, reloaded.getAmount());
        assertNull(reloaded.getProofPath());

        assertTrue(transactions.deleteById(expense.getId()));
        assertFalse(transactions.deleteById(expense.getId()));
        assertEquals(1, transactions.findByUserId(owner.getId()).size());
    }

    @Test
    void saveEntityKeepsGivenCreatedAt() {
        User owner = users.save("a", "h", "A", Role.USER);
        LocalDateTime when = LocalDateTime.of(2026, 1, 2, 3, 4, 5);

        Transaction saved = transactions.save(new Transaction(0, owner.getId(), "X", 10L,
                TransactionType.EXPENSE, null, when));

        assertEquals(when, transactions.findById(saved.getId()).orElseThrow().getCreatedAt());
    }

    @Test
    void foreignKeyRejectsUnknownOwner() {
        assertThrows(IllegalStateException.class,
                () -> transactions.save(12345, "Yatim", 1L, TransactionType.INCOME, null));
    }

    @Test
    void databaseFailuresBecomeIllegalStateException() throws Exception {
        DataSource broken = mock(DataSource.class);
        when(broken.getConnection()).thenThrow(new SQLException("db mati"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new JdbcUserRepository(broken).findAll());
        assertTrue(ex.getMessage().contains("db mati"));
        assertThrows(IllegalStateException.class, () -> new JdbcTransactionRepository(broken).findByUserId(1));
        assertThrows(IllegalStateException.class, () -> SchemaInitializer.initialize(broken));
    }

    @Test
    void schemaInitializerHasPrivateConstructor() throws Exception {
        Constructor<SchemaInitializer> constructor = SchemaInitializer.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
