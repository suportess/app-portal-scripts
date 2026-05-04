package br.tec.suportes.portalscripts.service;

import br.tec.suportes.portalscripts.domain.command.CommandRequest;
import br.tec.suportes.portalscripts.domain.migration.MigrationScript;
import br.tec.suportes.portalscripts.domain.migration.MigrationScript.ScriptType;
import br.tec.suportes.portalscripts.domain.route.RouteRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MigrationRunner")
class MigrationRunnerTest {

    @Mock
    private ScriptLoader scriptLoader;

    @Mock
    private HistoryService historyService;

    @Mock
    private PortalClient portalClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private MigrationRunner runner;

    private static final String COMMAND_JSON = """
            [
              {
                "chave": "produto-listar",
                "tipo": "QUERY",
                "tipoBanco": "postgres",
                "sql": "SELECT * FROM produto"
              }
            ]
            """;

    private static final String ROUTE_JSON = """
            [
              {
                "chave": "GET-produtos",
                "caminho": "/api/produtos",
                "metodo": "GET",
                "resposta": { "status": 200 },
                "servico": {
                  "threadUnica": false,
                  "resultadoUnico": false,
                  "passos": [
                    {
                      "alias": "produtos",
                      "abortarSemDados": false,
                      "comando": {
                        "tipo": "QUERY",
                        "database": "dbamv",
                        "nome": "produto-listar",
                        "retornarResultado": true,
                        "parametros": []
                      }
                    }
                  ]
                }
              }
            ]
            """;

    @BeforeEach
    void setUp() {
        when(historyService.loadExecutedVersions()).thenReturn(Set.of());
        when(historyService.checksumChanged(anyString(), anyString())).thenReturn(false);
    }

    @Test
    @DisplayName("Deve aplicar script de command com sucesso")
    void deveAplicarCommandScript() throws Exception {
        MigrationScript script = MigrationScript.builder()
                .version("V01")
                .filename("V01__commands_produto.json")
                .type(ScriptType.COMMAND)
                .content(COMMAND_JSON)
                .build();

        when(scriptLoader.loadAll()).thenReturn(List.of(script));

        runner.run();

        ArgumentCaptor<CommandRequest> captor = ArgumentCaptor.forClass(CommandRequest.class);
        verify(portalClient).createCommand(captor.capture());
        assertThat(captor.getValue().getChave()).isEqualTo("produto-listar");

        verify(historyService).recordSuccess(eq("V01"), eq("V01__commands_produto.json"), eq("COMMAND"), anyString());
    }

    @Test
    @DisplayName("Deve aplicar script de route com sucesso")
    void deveAplicarRouteScript() throws Exception {
        MigrationScript script = MigrationScript.builder()
                .version("V02")
                .filename("V02__routes_produto.json")
                .type(ScriptType.ROUTE)
                .content(ROUTE_JSON)
                .build();

        when(scriptLoader.loadAll()).thenReturn(List.of(script));

        runner.run();

        ArgumentCaptor<RouteRequest> captor = ArgumentCaptor.forClass(RouteRequest.class);
        verify(portalClient).createRoute(captor.capture());
        assertThat(captor.getValue().getCaminho()).isEqualTo("/api/produtos");

        verify(historyService).recordSuccess(eq("V02"), eq("V02__routes_produto.json"), eq("ROUTE"), anyString());
    }

    @Test
    @DisplayName("Deve pular script cuja versão já está no histórico")
    void devePularScriptJaExecutado() throws Exception {
        when(historyService.loadExecutedVersions()).thenReturn(Set.of("V01"));

        MigrationScript script = MigrationScript.builder()
                .version("V01")
                .filename("V01__commands_produto.json")
                .type(ScriptType.COMMAND)
                .content(COMMAND_JSON)
                .build();

        when(scriptLoader.loadAll()).thenReturn(List.of(script));

        runner.run();

        verify(portalClient, never()).createCommand(any());
        verify(historyService, never()).recordSuccess(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve registrar falha e lançar exceção quando portal retornar erro")
    void deveRegistrarFalhaQuandoPortalErrar() throws Exception {
        MigrationScript script = MigrationScript.builder()
                .version("V01")
                .filename("V01__commands_produto.json")
                .type(ScriptType.COMMAND)
                .content(COMMAND_JSON)
                .build();

        when(scriptLoader.loadAll()).thenReturn(List.of(script));
        doThrow(new PortalClient.PortalClientException("HTTP 500 — Internal Server Error"))
                .when(portalClient).createCommand(any());

        assertThatThrownBy(() -> runner.run())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("1 falha");

        verify(historyService).recordFailure(
                eq("V01"), eq("V01__commands_produto.json"), eq("COMMAND"), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve aplicar scripts em ordem crescente de versão")
    void deveAplicarEmOrdem() throws Exception {
        MigrationScript v02 = MigrationScript.builder()
                .version("V02")
                .filename("V02__commands_estoque.json")
                .type(ScriptType.COMMAND)
                .content("[{\"chave\":\"estoque-listar\",\"tipo\":\"QUERY\",\"tipoBanco\":\"postgres\",\"sql\":\"SELECT * FROM estoque\"}]")
                .build();

        MigrationScript v01 = MigrationScript.builder()
                .version("V01")
                .filename("V01__commands_produto.json")
                .type(ScriptType.COMMAND)
                .content(COMMAND_JSON)
                .build();

        when(scriptLoader.loadAll()).thenReturn(List.of(v01, v02));

        runner.run();

        verify(portalClient, times(2)).createCommand(any());
        verify(historyService).recordSuccess(eq("V01"), anyString(), anyString(), anyString());
        verify(historyService).recordSuccess(eq("V02"), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve interromper pipeline após primeira falha")
    void deveInterromperAposFalha() throws Exception {
        MigrationScript v01 = MigrationScript.builder()
                .version("V01")
                .filename("V01__commands_produto.json")
                .type(ScriptType.COMMAND)
                .content(COMMAND_JSON)
                .build();

        MigrationScript v02 = MigrationScript.builder()
                .version("V02")
                .filename("V02__commands_estoque.json")
                .type(ScriptType.COMMAND)
                .content("[{\"chave\":\"estoque-listar\",\"tipo\":\"QUERY\",\"tipoBanco\":\"postgres\",\"sql\":\"SELECT * FROM estoque\"}]")
                .build();

        when(scriptLoader.loadAll()).thenReturn(List.of(v01, v02));
        doThrow(new PortalClient.PortalClientException("erro"))
                .when(portalClient).createCommand(any());

        assertThatThrownBy(() -> runner.run()).isInstanceOf(RuntimeException.class);

        // Apenas V01 deve ter sido tentado; V02 não deve ser executado
        verify(portalClient, times(1)).createCommand(any());
        verify(historyService, times(1)).recordFailure(eq("V01"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve alertar sobre checksum alterado sem reexecutar")
    void deveAlertarChecksumAlterado() throws Exception {
        when(historyService.loadExecutedVersions()).thenReturn(Set.of("V01"));
        when(historyService.checksumChanged(eq("V01"), anyString())).thenReturn(true);

        MigrationScript script = MigrationScript.builder()
                .version("V01")
                .filename("V01__commands_produto.json")
                .type(ScriptType.COMMAND)
                .content(COMMAND_JSON)
                .build();

        when(scriptLoader.loadAll()).thenReturn(List.of(script));

        runner.run();

        // Não deve reexecutar mesmo com checksum alterado
        verify(portalClient, never()).createCommand(any());
    }

    @Test
    @DisplayName("Deve concluir normalmente quando não há scripts")
    void deveConcluirSemScripts() throws Exception {
        when(scriptLoader.loadAll()).thenReturn(List.of());

        runner.run();

        verify(portalClient, never()).createCommand(any());
        verify(portalClient, never()).createRoute(any());
    }
}
