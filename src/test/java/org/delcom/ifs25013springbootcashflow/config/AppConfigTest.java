package org.delcom.ifs25013springbootcashflow.config;

import org.delcom.ifs25013springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25013springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25013springbootcashflow.domain.entity.Role;
import org.delcom.ifs25013springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25013springbootcashflow.usecase.AuthUseCase;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.ApplicationRunner;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AppConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void wiresAllBeansAndSeedsRootAdminOnce() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        AppConfig config = new AppConfig();
        IUserRepository users = config.userRepository(dataSource);
        ITransactionRepository transactions = config.transactionRepository(dataSource);
        PasswordHasher hasher = config.passwordHasher();
        AuthUseCase auth = config.authUseCase(users, hasher);

        assertNotNull(config.userUseCase(users, hasher));
        assertNotNull(config.financeUseCase(transactions));
        assertNotNull(config.financePresenter());

        ApplicationRunner runner = config.seedRootAdmin(auth, "root", "rahasia");
        runner.run(null);
        runner.run(null);

        assertEquals(1, users.findAll().size());
        assertEquals(Role.ADMIN, users.findByUsername("root").orElseThrow().getRole());
    }

    @Test
    void mapsUploadsPathToUploadFolder() {
        AppConfig config = new AppConfig();
        FileStorage storage = config.fileStorage(tempDir.toString());
        WebMvcConfigurer configurer = config.uploadsResourceConfigurer(storage);
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class, RETURNS_DEEP_STUBS);

        configurer.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/uploads/**");
    }
}
