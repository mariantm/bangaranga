package com.bangaranga.loginform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers the form-login flow configured in {@link SecurityConfig}: the public custom
 * login page, successful authentication routing to {@code /home}, failure handling,
 * CSRF protection, and access control on protected resources. The in-memory user is
 * {@code admin} / {@code password} with role {@code USER}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoginTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginPage_isPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(unauthenticated());
    }

    @Test
    void login_withValidCredentials_redirectsToHomeAndAuthenticates() throws Exception {
        mockMvc.perform(formLogin("/login").user("admin").password("password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(authenticated().withUsername("admin"));
    }

    @Test
    void login_withWrongPassword_redirectsToLoginErrorUnauthenticated() throws Exception {
        mockMvc.perform(formLogin("/login").user("admin").password("wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void login_withUnknownUser_redirectsToLoginErrorUnauthenticated() throws Exception {
        mockMvc.perform(formLogin("/login").user("nobody").password("password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void login_withoutCsrfToken_isForbidden() throws Exception {
        // formLogin() carries a CSRF token by default; post manually without one.
        // CSRF is enabled by default, so the login POST must be rejected.
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "password"))
                .andExpect(status().isForbidden())
                .andExpect(unauthenticated());
    }

    @Test
    void protectedResource_whenUnauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")))
                .andExpect(unauthenticated());
    }

    @Test
    void protectedResource_whenAuthenticated_isOk() throws Exception {
        mockMvc.perform(get("/home").with(user("admin").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername("admin"));
    }
}
