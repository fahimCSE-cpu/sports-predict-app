package com.sportspredict;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:testdb", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class WebSmokeTest {

    @Autowired MockMvc mvc;

    @Test void homeShowsSeededMatch() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.containsString("Australia")))
           .andExpect(content().string(org.hamcrest.Matchers.containsString("56.62%")));
    }

    @Test void publicPagesRender() throws Exception {
        mvc.perform(get("/faq")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/match/1")).andExpect(status().isOk());
        mvc.perform(get("/match/9999")).andExpect(status().isNotFound());
    }

    @Test void adminRequiresLogin() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test @WithMockUser(roles = "ADMIN") void adminPagesRender() throws Exception {
        for (String p : new String[]{"/admin", "/admin/matches", "/admin/matches/new", "/admin/matches/1/edit",
                "/admin/analytics", "/admin/inquiries", "/admin/mail", "/admin/tips"}) {
            mvc.perform(get(p)).andExpect(status().isOk());
        }
    }

    @Test void inquiryAndSubscribeWork() throws Exception {
        mvc.perform(post("/match/1/inquiry").with(csrf()).param("name", "Ann").param("email", "ann@x.com").param("message", "Why?"))
           .andExpect(status().is3xxRedirection());
        mvc.perform(post("/subscribe").with(csrf()).param("email", "new@x.com")).andExpect(status().is3xxRedirection());
        mvc.perform(post("/match/1/vote").with(csrf()).param("side", "A")).andExpect(status().is3xxRedirection());
    }
}
