package org.delcom.ifs25013springbootcashflow;

import org.delcom.ifs25013springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25013springbootcashflow.domain.entity.Role;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25013springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25013springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25013springbootcashflow.usecase.UserUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/** Smoke test: seluruh konteks Spring naik memakai H2 (profil test), bukan PostgreSQL. */
@SpringBootTest
@ActiveProfiles("test")
class AppTest {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private AuthUseCase authUseCase;

    @Autowired
    private UserUseCase userUseCase;

    @Autowired
    private FinanceUseCase financeUseCase;

    @Autowired
    private FileStorage fileStorage;

    @Test
    void contextLoadsAndSeedsRootAdmin() {
        assertNotNull(authUseCase);
        assertNotNull(userUseCase);
        assertNotNull(financeUseCase);
        assertNotNull(fileStorage);
        assertEquals(Role.ADMIN, userRepository.findByUsername("admin").orElseThrow().getRole());
    }

    @Test
    void mainDelegatesToSpringApplicationRun() throws Exception {
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(eq(App.class), any(String[].class))).thenReturn(context);

            App.main(new String[]{"--server.port=0"});

            mocked.verify(() -> SpringApplication.run(eq(App.class), any(String[].class)));
        }
    }
}
