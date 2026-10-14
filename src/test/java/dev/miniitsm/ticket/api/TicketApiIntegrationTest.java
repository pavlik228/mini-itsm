package dev.miniitsm.ticket.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TicketApiIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-10-14T10:15:30.123456789Z");
    private static final String CREATED_AT = "2026-10-14T10:15:30.123456Z";

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createReturns201WithRelativeLocationAndTicket() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "  Printer is broken  ", "description": "Paper jam"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Printer is broken"))
                .andExpect(jsonPath("$.description").value("Paper jam"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.createdAt").value(CREATED_AT))
                .andReturn();

        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        assertThat(UUID.fromString(id)).isNotNull();
        assertThat(result.getResponse().getHeader("Location")).isEqualTo("/api/tickets/" + id);
    }

    @Test
    void getReturnsCreatedTicketWithSameFields() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "VPN does not connect", "description": "   "}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/tickets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(created.getResponse().getContentAsString(), JsonCompareMode.STRICT))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("VPN does not connect"))
                .andExpect(jsonPath("$.description").value(nullValue()))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.createdAt").value(CREATED_AT));
    }

    @Test
    void getUnknownIdReturns404TicketNotFound() throws Exception {
        UUID unknown = UUID.randomUUID();

        mockMvc.perform(get("/api/tickets/{id}", unknown))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Ticket not found"))
                .andExpect(jsonPath("$.detail").value(containsString(unknown.toString())));
    }

    @Test
    void createWithBlankTitleReturns400WithTitleFieldError() throws Exception {
        expectTitleValidationError("""
                {"title": "   "}
                """);
    }

    @Test
    void createWithTitleOf201CharsReturns400WithTitleFieldError() throws Exception {
        expectTitleValidationError("""
                {"title": "%s"}
                """.formatted("a".repeat(201)));
    }

    @Test
    void createWithoutTitleReturns400WithTitleFieldError() throws Exception {
        expectTitleValidationError("{}");
    }

    @Test
    void createWithTitleOf200CharsSurroundedBySpacesReturns201() throws Exception {
        String title = "a".repeat(200);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": " %s "}
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title));
    }

    @Test
    void getWithNonUuidIdReturns400ProblemDetail() throws Exception {
        mockMvc.perform(get("/api/tickets/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/api/tickets/not-a-uuid"));
    }

    @Test
    void createWithoutTitleAndRussianAcceptLanguageReturnsEnglishMessage() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .header("Accept-Language", "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[0].message").value("must not be null"));
    }

    @Test
    void createWithoutTitleAndWithoutAcceptLanguageReturnsEnglishMessage() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[0].message").value("must not be null"));
    }

    private void expectTitleValidationError(String body) throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.instance").value("/api/tickets"))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("title")));
    }
}
