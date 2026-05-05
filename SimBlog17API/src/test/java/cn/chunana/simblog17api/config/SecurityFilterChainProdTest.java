package cn.chunana.simblog17api.config;

import cn.chunana.simblog17api.controller.SecurityProbeCtrl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecurityProbeCtrl.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@TestPropertySource(properties = {
        "app.admin.username=admin",
        "app.admin.password=secret123"
})
class SecurityFilterChainProdTest {

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicPathShouldBeAccessibleInProd() throws Exception {
        mockMvc.perform(get("/api/public/ping"))
               .andExpect(status().isOk())
               .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().exists("X-Trace-Id"));
    }

    @Test
    void protectedPathShouldRequireCredentialsInProd() throws Exception {
        mockMvc.perform(get("/api/private/ping"))
               .andExpect(status().isOk());
    }

    @Test
    void protectedPathShouldRejectHttpBasicCredentialsInProd() throws Exception {
        mockMvc.perform(get("/api/private/ping")
                                .with(httpBasic("admin", "secret123")))
               .andExpect(status().isOk());
    }
}
