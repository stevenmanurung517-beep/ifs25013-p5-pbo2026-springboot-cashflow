package org.delcom.ifs25013springbootcashflow.framework.auth;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import jakarta.servlet.http.Cookie;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Menyimpan identitas pengguna yang sedang login di VaadinSession, HTTP session, dan cookie
 * {@code cashflow_uid}. Cookie memuat ID pengguna yang ditandatangani (HMAC-SHA256) agar tidak
 * bisa dipalsukan. Flag {@link #LOGGED_OUT_KEY} mencegah sesi dipulihkan dari cookie setelah logout.
 */
public final class AuthSession {

    /** Penanda bahwa pengguna sudah logout; mencegah pemulihan sesi dari cookie. */
    public static final String LOGGED_OUT_KEY = "cashflow.loggedOut";

    private static final String USER_ID_KEY = "cashflow.userId";
    private static final String COOKIE_NAME = "cashflow_uid";
    private static final int COOKIE_MAX_AGE_SECONDS = 8 * 60 * 60;
    private static final byte[] SECRET = randomSecret();

    private AuthSession() {
    }

    /** Menandai pengguna sebagai login (sesi + cookie). */
    public static void login(User user) {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(USER_ID_KEY, user.getId());
        session.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);
        WrappedSession httpSession = session.getSession();
        if (httpSession != null) {
            httpSession.setAttribute(USER_ID_KEY, user.getId());
            httpSession.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);
        }
        writeCookie(user.getId() + "." + sign(user.getId()), COOKIE_MAX_AGE_SECONDS);
    }

    /** Mengeluarkan pengguna: menghapus sesi dan cookie, lalu menandai LOGGED_OUT. */
    public static void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(USER_ID_KEY, null);
        session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        WrappedSession httpSession = session.getSession();
        if (httpSession != null) {
            httpSession.setAttribute(USER_ID_KEY, null);
            httpSession.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        }
        writeCookie("", 0);
    }

    /**
     * Mengembalikan pengguna yang sedang login, atau null bila belum login.
     * Setelah refresh/restart sesi dipulihkan dari cookie, kecuali pengguna sudah logout.
     */
// Bagian login() dan logout() di atasnya TETAP SAMA

    public static User currentUser(IUserRepository userRepository) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return null;
        }
        if (Boolean.TRUE.equals(session.getAttribute(LOGGED_OUT_KEY))) {
            return null;
        }
        Object storedId = session.getAttribute(USER_ID_KEY);
        if (storedId instanceof Integer userId) {
            return userRepository.findById(userId).orElse(null);
        }

        // Cek juga dari HTTP Session (WrappedSession) yang sudah diisi saat login()
        WrappedSession httpSession = session.getSession();
        if (httpSession != null) {
            if (Boolean.TRUE.equals(httpSession.getAttribute(LOGGED_OUT_KEY))) {
                return null;
            }
            Object httpStoredId = httpSession.getAttribute(USER_ID_KEY);
            if (httpStoredId instanceof Integer userId) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    session.setAttribute(USER_ID_KEY, user.getId());
                    return user;
                }
            }
        }

        Integer cookieUserId = readCookieUserId();
        if (cookieUserId == null) {
            return null;
        }
        User user = userRepository.findById(cookieUserId).orElse(null);
        if (user != null) {
            session.setAttribute(USER_ID_KEY, user.getId());
            if (httpSession != null) {
                httpSession.setAttribute(USER_ID_KEY, user.getId());
            }
        }
        return user;
    }

    // Bagian readCookieUserId(), parseToken(), dll di bawahnya TETAP SAMA

    // ------------------------------------------------------------------

    private static Integer readCookieUserId() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return parseToken(cookie.getValue());
            }
        }
        return null;
    }

    private static Integer parseToken(String token) {
        int dot = token.indexOf('.');
        if (dot <= 0) {
            return null;
        }
        try {
            int userId = Integer.parseInt(token.substring(0, dot));
            return sign(userId).equals(token.substring(dot + 1)) ? userId : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static void writeCookie(String value, int maxAgeSeconds) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }

    private static String sign(int userId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
            byte[] signature = mac.doFinal(String.valueOf(userId).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(signature);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("HMAC tidak tersedia", ex);
        }
    }

    private static byte[] randomSecret() {
        byte[] secret = new byte[32];
        new SecureRandom().nextBytes(secret);
        return secret;
    }
}
