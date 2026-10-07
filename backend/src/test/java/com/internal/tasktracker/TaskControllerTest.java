package com.internal.tasktracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchExcludesArchivedTasksMatchedByDescription() throws Exception {
        mockMvc.perform(get("/api/tasks").param("q", "api").param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(8))
                .andExpect(jsonPath("$.items[*].archived", everyItem(is(false))))
                .andExpect(jsonPath("$.items[*].id", not(hasItem(20))))
                .andExpect(jsonPath("$.items[*].id", not(hasItem(21))));
    }

    @Test
    void statusFilterAppliesToTitleMatches() throws Exception {
        mockMvc.perform(get("/api/tasks").param("q", "api").param("status", "OPEN").param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(6))
                .andExpect(jsonPath("$.items[*].status", everyItem(is("OPEN"))));
    }

    @Test
    void statusFilterWithoutQuery() throws Exception {
        mockMvc.perform(get("/api/tasks").param("status", "done").param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.items[*].status", everyItem(is("DONE"))));
    }

    @Test
    void paginationReturnsRequestedSlice() throws Exception {
        mockMvc.perform(get("/api/tasks").param("page", "5").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(47))
                .andExpect(jsonPath("$.items.length()").value(7));
    }

    @Test
    void unknownStatusReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/tasks").param("status", "FOO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidPagingReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/tasks").param("page", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/tasks").param("pageSize", "-5"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/tasks").param("pageSize", "1000"))
                .andExpect(status().isBadRequest());
    }
}
