package org.delcom.ifs25013springbootcashflow.config;

import org.delcom.ifs25013springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25013springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25013springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25013springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25013springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25013springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25013springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25013springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25013springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25013springbootcashflow.usecase.UserUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

/**
 * Composition root Spring: satu-satunya tempat yang memilih implementasi konkret
 * dan merakit dependency antar-layer (bukan {@code new} yang tersebar di view).
 */
@Configuration
public class AppConfig {

    @Bean
    public IUserRepository userRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(dataSource);
    }

    @Bean
    public ITransactionRepository transactionRepository(DataSource dataSource) {
        return new JdbcTransactionRepository(dataSource);
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new PasswordHasher();
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository userRepository, PasswordHasher passwordHasher) {
        return new AuthUseCase(userRepository, passwordHasher);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository userRepository, PasswordHasher passwordHasher) {
        return new UserUseCase(userRepository, passwordHasher);
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactionRepository) {
        return new FinanceUseCase(transactionRepository);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }

    @Bean
    public FileStorage fileStorage(@Value("${app.upload-dir}") String uploadDir) {
        return new FileStorage(uploadDir);
    }

    /** Membuat akun ADMIN dari ROOT_USERNAME / ROOT_PASSWORD saat aplikasi start. */
    @Bean
    public ApplicationRunner seedRootAdmin(
            AuthUseCase authUseCase,
            @Value("${app.root-username}") String rootUsername,
            @Value("${app.root-password}") String rootPassword
    ) {
        return args -> authUseCase.ensureRootAdmin(rootUsername, rootPassword);
    }

    /** Melayani berkas unggahan (foto profil dan bukti transaksi) di /uploads/**. */
    @Bean
    public WebMvcConfigurer uploadsResourceConfigurer(FileStorage fileStorage) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations(fileStorage.getRootUri());
            }
        };
    }
}
