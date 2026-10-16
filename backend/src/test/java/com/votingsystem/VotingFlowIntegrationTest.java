package com.votingsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.votingsystem.entity.AppUser;
import com.votingsystem.entity.Role;
import com.votingsystem.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VotingFlowIntegrationTest {

    private static final String PASSWORD = "Str0ng!Passw0rd";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AppUserRepository users;
    @Autowired PasswordEncoder encoder;

    // ---------- authentication & authorization ----------

    @Test
    void anonymousRequestsAreRejected() throws Exception {
        mvc.perform(get("/api/candidates")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/results")).andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTokenIsRejected() throws Exception {
        mvc.perform(get("/api/candidates").header("Authorization", "Bearer not.a.real.token"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void weakPasswordIsRejectedWithAFieldError() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                .content(body(Map.of("fullName", "Weak User", "email", email("weak"), "password", "password"))))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void passwordsAreStoredAsBcryptHashes() throws Exception {
        String email = email("hash");
        register(email);
        AppUser saved = users.findByEmail(email).orElseThrow();
        assertThat(saved.getPasswordHash()).startsWith("$2").isNotEqualTo(PASSWORD);
    }

    @Test
    void clientCannotChooseItsOwnRole() throws Exception {
        String email = email("sneaky");
        mvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                .content(body(Map.of("fullName", "Sneaky", "email", email, "password", PASSWORD, "role", "ADMIN"))))
           .andExpect(status().isCreated());
        String token = login(email);
        mvc.perform(get("/api/admin/stats").header("Authorization", bearer(token)))
           .andExpect(status().isForbidden());
    }

    @Test
    void accountIsLockedAfterRepeatedFailedLogins() throws Exception {
        String email = email("lock");
        register(email);
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                    .content(body(Map.of("email", email, "password", "Wrong!Passw0rd"))))
               .andExpect(status().isUnauthorized());
        }
        // Even the correct password is refused while locked
        mvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                .content(body(Map.of("email", email, "password", PASSWORD))))
           .andExpect(status().isTooManyRequests());
    }

    // ---------- voting rules ----------

    @Test
    void voterCanVoteExactlyOnce() throws Exception {
        String admin = adminToken();
        long candidateId = createCandidate(admin, "Candidate " + UUID.randomUUID());

        String email = email("voter");
        register(email);
        String token = login(email);

        mvc.perform(get("/api/votes/status").header("Authorization", bearer(token)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.hasVoted").value(false));

        mvc.perform(post("/api/votes").header("Authorization", bearer(token)).contentType(APPLICATION_JSON)
                .content(body(Map.of("candidateId", candidateId))))
           .andExpect(status().isCreated());

        mvc.perform(post("/api/votes").header("Authorization", bearer(token)).contentType(APPLICATION_JSON)
                .content(body(Map.of("candidateId", candidateId))))
           .andExpect(status().isConflict());

        mvc.perform(get("/api/admin/results").header("Authorization", bearer(admin)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.candidateId == " + candidateId + ")].votes").value(1));
    }

    @Test
    void votersCannotSeeResultsAndAdminsCannotVote() throws Exception {
        String admin = adminToken();
        long candidateId = createCandidate(admin, "Candidate " + UUID.randomUUID());

        String email = email("plain");
        register(email);
        String voter = login(email);

        mvc.perform(get("/api/admin/results").header("Authorization", bearer(voter)))
           .andExpect(status().isForbidden());

        mvc.perform(post("/api/votes").header("Authorization", bearer(admin)).contentType(APPLICATION_JSON)
                .content(body(Map.of("candidateId", candidateId))))
           .andExpect(status().isForbidden());
    }

    // ---------- helpers ----------

    private String body(Object o) throws Exception { return json.writeValueAsString(o); }

    private static String bearer(String token) { return "Bearer " + token; }

    private static String email(String prefix) { return prefix + UUID.randomUUID().toString().substring(0, 8) + "@example.com"; }

    private void register(String email) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                .content(body(Map.of("fullName", "Test User", "email", email, "password", PASSWORD))))
           .andExpect(status().isCreated());
    }

    private String login(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                .content(body(Map.of("email", email, "password", PASSWORD))))
           .andExpect(status().isOk()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String adminToken() throws Exception {
        String email = email("admin");
        users.save(new AppUser("Admin", email, encoder.encode(PASSWORD), Role.ADMIN));
        return login(email);
    }

    private long createCandidate(String adminToken, String name) throws Exception {
        MvcResult result = mvc.perform(post("/api/admin/candidates").header("Authorization", bearer(adminToken))
                .contentType(APPLICATION_JSON).content(body(Map.of("name", name, "party", "Independent"))))
           .andExpect(status().isCreated()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
