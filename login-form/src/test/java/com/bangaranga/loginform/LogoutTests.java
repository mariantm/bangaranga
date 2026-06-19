package com.bangaranga.loginform;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers every behaviour configured in {@link SecurityConfig}'s logout block:
 * POST-only logout, CSRF protection, redirect target, session invalidation,
 * authentication clearing, and JSESSIONID cookie deletion.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LogoutTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void logout_authenticatedUser_redirectsToLoginLogout() throws Exception {
        mockMvc.perform(post("/logout").with(user("admin").roles("USER")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void logout_authenticatedUser_clearsAuthentication() throws Exception {
        mockMvc.perform(post("/logout").with(user("admin").roles("USER")).with(csrf()))
                .andExpect(unauthenticated());
    }

    @Test
    void logout_invalidatesHttpSession() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/logout").session(session)
                        .with(user("admin").roles("USER")).with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void logout_deletesJsessionidCookie() throws Exception {
        mockMvc.perform(post("/logout").session(new MockHttpSession())
                        .with(user("admin").roles("USER")).with(csrf()))
                // deleteCookies("JSESSIONID") emits a Set-Cookie that expires it (Max-Age 0)
                .andExpect(cookie().maxAge("JSESSIONID", 0));
    }

    @Test
    void logout_withoutCsrfToken_isForbidden() throws Exception {
        // CSRF is enabled by default; logout must carry a valid token.
        mockMvc.perform(post("/logout").with(user("admin").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_viaGet_doesNotLogOut() throws Exception {
        // logoutUrl matches POST only when CSRF is on; GET /logout is not the logout
        // filter, so an authenticated user stays authenticated (no /login?logout redirect).
        mockMvc.perform(get("/logout").with(user("admin").roles("USER")))
                .andExpect(authenticated())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
                        .isNotEqualTo("/login?logout"));
    }

    @Test
    void logout_unauthenticatedUser_stillRedirectsToLoginLogout() throws Exception {
        // The logout filter runs regardless of an existing authentication.
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void protectedResource_afterLogout_redirectsToLogin() throws Exception {
        // Log out, then reuse the (now invalidated) session to hit a protected page.
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/logout").session(session)
                        .with(user("admin").roles("USER")).with(csrf()))
                .andExpect(status().is3xxRedirection());

        MvcResult result = mockMvc.perform(get("/home").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.containsString("/login")))
                .andReturn();

        // sanity: the JSESSIONID we logged out with is gone / unauthenticated
        Cookie jsessionid = result.getRequest().getCookies() == null ? null
                : java.util.Arrays.stream(result.getRequest().getCookies())
                .filter(c -> "JSESSIONID".equals(c.getName())).findFirst().orElse(null);
        assertThat(jsessionid).isNull();
    }
}
