package com.code.rental.controller;

import com.code.rental.controller.dto.response.BlockResponseDTO;
import com.code.rental.security.jwt.JwtProvider;
import com.code.rental.service.BlockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
public class BlockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @MockBean
    private BlockService blockService;

    private String accessToken;

    @BeforeEach
    public void setUp() {
        accessToken = jwtProvider.createToken("guest1@gmail.com");
    }

    @Test
    void getBlockByIdShouldReturn200() throws Exception {
        when(blockService.getBlockById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/blocks/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createBlockShouldReturn201() throws Exception {
        when(blockService.createBlock(any())).thenReturn(new BlockResponseDTO(1L));

        mockMvc.perform(post("/blocks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"propertyId\":1,\"startDate\":\"2026-01-01\",\"endDate\":\"2026-01-02\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createBlockWithInvalidDateRangeShouldReturn400() throws Exception {
        mockMvc.perform(post("/blocks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"propertyId\":1,\"startDate\":\"2026-01-02\",\"endDate\":\"2026-01-01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBlockWithStartDateInThePastShouldReturn400() throws Exception {
        mockMvc.perform(post("/blocks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"propertyId\":1,\"startDate\":\"2020-01-01\",\"endDate\":\"2026-01-01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBlockWithInvalidPropertyIdShouldReturn422() throws Exception {
        mockMvc.perform(post("/blocks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"propertyId\":0,\"startDate\":\"2026-01-01\",\"endDate\":\"2026-01-02\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProperyBlocksShouldReturn200() throws Exception {
        when(blockService.getBlocksByPropertyId(anyLong())).thenReturn(null);

        mockMvc.perform(get("/blocks?propertyId=1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getPropertyBlocksShouldReturn400WhenNotInformingPropertyId() throws Exception {
        mockMvc.perform(get("/blocks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBlockShouldReturn200() throws Exception {
        mockMvc.perform(put("/blocks/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"propertyId\":1,\"startDate\":\"2026-01-01\",\"endDate\":\"2026-01-02\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteBlockShouldReturn204() throws Exception {
        mockMvc.perform(delete("/blocks/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

}