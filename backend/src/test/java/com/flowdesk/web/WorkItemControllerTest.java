package com.flowdesk.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowdesk.repository.WorkItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkItemControllerTest {

    @Autowired MockMvc mvc;
    @Autowired WorkItemRepository repository;
    @Autowired ObjectMapper mapper;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private long createItem(String title, String description) throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of("title", title, "description", description));
        String response = mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = mapper.readTree(response);
        return json.get("id").asLong();
    }

    @Test
    void createAndListItems() throws Exception {
        createItem("Printer broken", "The printer shows an error.");

        mvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("OPEN")))
                .andExpect(jsonPath("$[0].priority", is("MEDIUM")));
    }

    @Test
    void validationRejectsBlankTitle() throws Exception {
        mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    @Test
    void updateAndFilterByStatus() throws Exception {
        long id = createItem("Invoice", "Charged twice");
        mvc.perform(put("/api/items/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Invoice\",\"description\":\"Charged twice\",\"status\":\"RESOLVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")));

        mvc.perform(get("/api/items").param("status", "OPEN")).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/items").param("status", "RESOLVED")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void analyzeUsesFallbackProviderWithoutApiKey() throws Exception {
        long id = createItem("App crash", "The app crashes on startup.");

        mvc.perform(post("/api/items/" + id + "/ai/analyze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category", is("Bug")))
                .andExpect(jsonPath("$.aiSummary", notNullValue()))
                .andExpect(jsonPath("$.aiDraftReply", notNullValue()));
    }

    @Test
    void deleteAndMissingItem() throws Exception {
        long id = createItem("Temp", "Temp");
        mvc.perform(delete("/api/items/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/items/" + id)).andExpect(status().isNotFound());
    }
}
