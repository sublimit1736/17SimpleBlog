package cn.chunana.simblog17api.config;

import cn.chunana.simblog17api.controller.SecurityProbeCtrl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecurityProbeCtrl.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityFilterChainDevTest {

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedPathShouldBeAccessibleWithoutCredentialsInDev() throws Exception {
        mockMvc.perform(get("/api/private/ping"))
               .andExpect(status().isOk())
               .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().exists("X-Trace-Id"));
    }
}
