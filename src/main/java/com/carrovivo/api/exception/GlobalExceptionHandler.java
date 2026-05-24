package com.carrovivo.api.exception;

import com.carrovivo.api.security.SecurityException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// TRATAMENTO SEGURO DE ERROS — PONTO CENTRAL
// Captura todas as exceptions da aplicação e retorna respostas
// padronizadas sem expor stack traces, tecnologias ou estrutura interna.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // MENSAGEM GENÉRICA PARA RECURSO NÃO ENCONTRADO
    // Nunca retorna "Veículo não encontrado com id: 42" —
    // isso expõe estrutura interna e facilita enumeração de recursos.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody("Recurso não encontrado", 404));
    }

    // 401 GENÉRICO PARA FALHAS DE AUTENTICAÇÃO
    // SecurityException é lançada pelo AuthService tanto para usuário inexistente
    // quanto para senha errada — a mesma mensagem impede user enumeration.
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurity(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody("Credenciais inválidas", 401));
    }

    // ERROS DE VALIDAÇÃO — RETORNA MENSAGENS DOS @Constraints
    // Expõe apenas as mensagens definidas nas annotations de validação
    // (ex: "Placa contém caracteres inválidos"), nunca detalhes técnicos.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).toList();
        Map<String, Object> body = errorBody("Erro de validação nos campos informados", 400);
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    // FALLBACK GENÉRICO — NENHUM DETALHE INTERNO VAZA
    // Cobre qualquer exception não tratada. A mensagem é sempre genérica.
    // Stack trace, nome da classe e tecnologia nunca aparecem na resposta.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("Ocorreu um erro interno. Tente novamente mais tarde.", 500));
    }

    private Map<String, Object> errorBody(String message, int status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status);
        body.put("message", message);
        return body;
    }
}
