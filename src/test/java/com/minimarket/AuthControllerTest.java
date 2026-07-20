package com.minimarket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración del flujo de autenticación JWT: login exitoso
 * (con token emitido), credenciales inválidas y autoregistro de clientes.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginConCredencialesValidasDevuelveTokenJwt() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"clave-incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginConUsuarioInexistenteDevuelve401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"no-existe\",\"password\":\"cualquiera\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginSinCredencialesDevuelve400PorValidacion() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registroDeNuevoClienteEsExitoso() throws Exception {
        mockMvc.perform(post("/auth/registro")
                        .contentType("application/json")
                        .content("{\"username\":\"clienteNuevoTest\",\"password\":\"clave123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("clienteNuevoTest"));
    }

    @Test
    void registroConUsernameDuplicadoDevuelve409() throws Exception {
        mockMvc.perform(post("/auth/registro")
                        .contentType("application/json")
                        .content("{\"username\":\"clienteDuplicadoTest\",\"password\":\"clave123\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/registro")
                        .contentType("application/json")
                        .content("{\"username\":\"clienteDuplicadoTest\",\"password\":\"otraClave123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void registroConContrasenaMuyCortaDevuelve400() throws Exception {
        mockMvc.perform(post("/auth/registro")
                        .contentType("application/json")
                        .content("{\"username\":\"clienteInvalidoTest\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unTokenValidoPermiteAccederAUnRecursoProtegido() throws Exception {
        String respuesta = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = respuesta.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"nombre\":\"Producto de prueba\",\"precio\":100.0,\"stock\":10,\"categoria\":{\"id\":1}}"))
                .andExpect(status().isOk());
    }
}
