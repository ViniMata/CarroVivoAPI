package com.carrovivo.api.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;

// [SEC-23] FILTRO XSS — PONTO DE ENTRADA DA SANITIZAÇÃO
// Intercepta todas as requisições HTTP e as envolve com
// o XssRequestWrapper antes de chegarem aos controllers.
@Component
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // [SEC-24] TODA REQUISIÇÃO É SANITIZADA
        // Substitui a requisição original pelo wrapper sanitizado.
        // A partir daqui, qualquer leitura de parâmetro, header ou body
        // retornará dados já sanitizados.
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }
}
