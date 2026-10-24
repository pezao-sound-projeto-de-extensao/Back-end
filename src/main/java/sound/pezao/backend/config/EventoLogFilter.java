package sound.pezao.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.util.Set;

@Component
public class EventoLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("eventos");
    private static final Set<String> METODOS = Set.of("POST", "PUT", "PATCH", "DELETE");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !METODOS.contains(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long inicio = System.currentTimeMillis();
        MDC.put("usuario", usuario());
        Throwable naoTratada = null;
        try {
            filterChain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException e) {
            naoTratada = e;
            throw e;
        } finally {
            Throwable erro = naoTratada != null ? naoTratada
                    : (Throwable) request.getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);
            int status = naoTratada != null ? 500 : response.getStatus();
            log.atLevel(status >= 500 ? Level.ERROR : status >= 400 ? Level.WARN : Level.INFO)
                    .log("metodo={} rota={} status={} ip={} duracao_ms={} erro=\"{}\"",
                            request.getMethod(), request.getRequestURI(), status, request.getRemoteAddr(),
                            System.currentTimeMillis() - inicio, erro == null ? "" : erro.getMessage());
            MDC.remove("usuario");
        }
    }

    private static String usuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return "anonimo";
        }
        return auth.getName();
    }
}
