package tech.algofinserve.recommendation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
public class MarketIndicesControllerTest {

    @Autowired
    private MarketIndicesController marketIndicesController;

    @Test
    public void testGetMarketIndices() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(marketIndicesController).build();

        mockMvc.perform(get("/api/indices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nifty").exists())
                .andExpect(jsonPath("$.banknifty").exists())
                .andExpect(jsonPath("$.marketOpen").exists())
                .andExpect(jsonPath("$.lastUpdated").exists());
    }

    @Test
    public void testGetMarketStatus() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(marketIndicesController).build();

        mockMvc.perform(get("/api/indices/market-status"))
                .andExpect(status().isOk())
                .andExpect(content().string("true").or(content().string("false")));
    }
}
