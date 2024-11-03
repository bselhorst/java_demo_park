package com.mballem.demoparkapi;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.mballem.demoparkapi.web.dto.UsuarioCreateDto;
import com.mballem.demoparkapi.web.dto.UsuarioResponseDto;
import com.mballem.demoparkapi.web.dto.UsuarioSenhaDto;
import com.mballem.demoparkapi.web.exception.ErrorMessage;

// Anotação incluindo uma configuração, executando um tomcat em uma porta randômica de testes
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Anotação para executar um script de insert antes de começar os testes, adicionando os parâmetros script e executionPhase
@Sql(scripts = "/sql/usuarios/usuarios-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
// Anotação para executar um escript de delete depois dos testes, zerando o banco
@Sql(scripts = "/sql/usuarios/usuarios-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UsuarioIT {
    // Objeto injetado
    @Autowired
    WebTestClient testClient;

    // A annotation Test
    @Test
    // Cria uma função pública
    public void createUsuario_ComUsernameEPasswordValidos_RetornarUsuarioCriadoComStatus201(){
        // Como o post devolve um body, então a reposta vai ser um dto, é somente um conjunto de informações
        UsuarioResponseDto responseBody = testClient
            // Vai ser um post
            .post()
            // Na url específica
            .uri("/api/v1/usuarios")
            // Passando um json
            .contentType(MediaType.APPLICATION_JSON)
            // Com o seguinte Body
            .bodyValue(new UsuarioCreateDto("tody@email.com", "123456"))
            .exchange()
            // Com o status de criado
            .expectStatus().isCreated()
            // Recebe um body de resposta
            .expectBody(UsuarioResponseDto.class)
            // retorna o resultado com o responseBody
            .returnResult().getResponseBody();
        
        // Com a informação anterior, fazemos os testes, o primeiro é se não é nulo
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        // O segundo é se o ID não é nulo
        org.assertj.core.api.Assertions.assertThat(responseBody.getId()).isNotNull();
        // O terceiro é para saber se o username foi o mesmo que foi criado anteriormente
        org.assertj.core.api.Assertions.assertThat(responseBody.getUsername()).isEqualTo("tody@email.com");
        // E o último testa se ele criou a role com o nome de cliente (que foi definido no projeto)
        org.assertj.core.api.Assertions.assertThat(responseBody.getRole()).isEqualTo("CLIENTE");
    }

    @Test
    public void createUsuario_ComUsernameInvalido_RetornaErrorMessageStatus422(){
        // Deve retornar um erro
        ErrorMessage responseBody = testClient
            .post()
            .uri("/api/v1/usuarios")
            .contentType(MediaType.APPLICATION_JSON)
            // Passando informações inválidas
            .bodyValue(new UsuarioCreateDto("", "123456"))
            .exchange()
            // Status de erro
            .expectStatus().isEqualTo(422)
            // Retorna o erro
            .expectBody(ErrorMessage.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        // Verificando se ele está retornando o erro na validação
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
        
        // Outro exemplo de erro
        responseBody = testClient
            .post()
            .uri("/api/v1/usuarios")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new UsuarioCreateDto("tody@", "123456"))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody(ErrorMessage.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
        
        // Outro exemplo de erro
        responseBody = testClient
            .post()
            .uri("/api/v1/usuarios")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new UsuarioCreateDto("tody@email", "123456"))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody(ErrorMessage.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void createUsuario_ComPasswordInvalido_RetornaErrorMessageStatus422(){
        ErrorMessage responseBody = testClient
            .post()
            .uri("/api/v1/usuarios")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new UsuarioCreateDto("tody@email.com", ""))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody(ErrorMessage.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
        
        responseBody = testClient
            .post()
            .uri("/api/v1/usuarios")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new UsuarioCreateDto("tody@email.com", "12345"))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody(ErrorMessage.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
        
        responseBody = testClient
            .post()
            .uri("/api/v1/usuarios")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new UsuarioCreateDto("tody@email.com", "1234567"))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody(ErrorMessage.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void createUsuario_ComUsernameRepetido_RetornarErrorMessageComStatus409(){
        ErrorMessage responseBody = testClient
            .post()
            .uri("/api/v1/usuarios")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new UsuarioCreateDto("ana@email.com", "123456"))
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody(ErrorMessage.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test
    public void buscarUsuario_ComIdExistente_RetornarUsuarioComStatus200(){
        UsuarioResponseDto responseBody = testClient
            // Testar o get 
            .get()
            .uri("/api/v1/usuarios/100")
            // Como não é preciso passar um valor no Body e nem o tipo, não se tem aqui
            .exchange()
            // Verifica se deu tudo certo
            .expectStatus().isOk()
            .expectBody(UsuarioResponseDto.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getId()).isEqualTo(100);
        org.assertj.core.api.Assertions.assertThat(responseBody.getUsername()).isEqualTo("ana@email.com");
        org.assertj.core.api.Assertions.assertThat(responseBody.getRole()).isEqualTo("ADMIN");
    
    }

    @Test
    public void buscarUsuario_ComIdInexistente_RetornarUsuarioComStatus404(){
        ErrorMessage responseBody = testClient
            .get()
            .uri("/api/v1/usuarios/0")
            .exchange()
            .expectStatus().isNotFound()
            .expectBody(ErrorMessage.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void editarSenha_ComDadosValidos_RetornarStatus204(){
        testClient
            .patch()
            .uri("/api/v1/usuarios/100")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new UsuarioSenhaDto("123456", "123456", "123456"))
            .exchange()
            .expectStatus().isNoContent();
    }

    @Test
    public void editarSenha_ComIdInexistente_RetornarErrorMessageComStatus404(){
        ErrorMessage responseBody = testClient
            .patch()
            .uri("/api/v1/usuarios/0")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new UsuarioSenhaDto("123456", "123456", "123456"))
            .exchange()
            .expectStatus().isNotFound()
            .expectBody(ErrorMessage.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void editarSenha_ComIdInexistente_RetornarErrorMessageComStatus422(){
        ErrorMessage responseBody = testClient
            .patch()
            .uri("/api/v1/usuarios/100")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new UsuarioSenhaDto("", "", ""))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody(ErrorMessage.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
            .patch()
            .uri("/api/v1/usuarios/100")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new UsuarioSenhaDto("12345", "12345", "12345"))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody(ErrorMessage.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
            .patch()
            .uri("/api/v1/usuarios/100")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new UsuarioSenhaDto("1234567", "1234567", "1234567"))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody(ErrorMessage.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void editarSenha_ComSenhasInvalidas_RetornarErrorMessageComStatus400(){
        ErrorMessage responseBody = testClient
            .patch()
            .uri("/api/v1/usuarios/100")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new UsuarioSenhaDto("123456", "123456", "000000"))
            .exchange()
            .expectStatus().isEqualTo(400)
            .expectBody(ErrorMessage.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(400);

        responseBody = testClient
            .patch()
            .uri("/api/v1/usuarios/100")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new UsuarioSenhaDto("123457", "123456", "123456"))
            .exchange()
            .expectStatus().isEqualTo(400)
            .expectBody(ErrorMessage.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(400);
    }

    @Test
    public void buscarListaDeUsuarios_RetornarUsuariosComStatus200(){
        List<UsuarioResponseDto> responseBody = testClient
            .get()
            .uri("/api/v1/usuarios")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(UsuarioResponseDto.class)
            .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.size()).isEqualTo(3);
    
    }

}