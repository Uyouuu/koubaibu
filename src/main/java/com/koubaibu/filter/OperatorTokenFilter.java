package com.koubaibu.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * オペレータートークン検証フィルター（スタブ）
 * Operator Token Validation Filter - Validates X-Operator-Token for update operations
 * 
 * 本番環境では物理カードリーダーと連携して検証を行う
 */
@Component
public class OperatorTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(OperatorTokenFilter.class);
    private static final String OPERATOR_TOKEN_HEADER = "X-Operator-Token";

    @Value("${security.operator-token.enabled:false}")
    private boolean tokenValidationEnabled;

    // 検証が必要なHTTPメソッド
    private static final Set<String> PROTECTED_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    // 検証をスキップするパス
    private static final Set<String> EXCLUDED_PATHS = Set.of(
        "/actuator",
        "/h2-console",
        "/VAADIN"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        if (!tokenValidationEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();
        String path = request.getRequestURI();

        // 保護対象外のメソッドやパスはスキップ
        if (!PROTECTED_METHODS.contains(method) || isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // オペレータートークンの検証
        String operatorToken = request.getHeader(OPERATOR_TOKEN_HEADER);
        
        if (operatorToken == null || operatorToken.isEmpty()) {
            logger.warn("Missing operator token for {} {}", method, path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Operator token required\"}");
            return;
        }

        // トークン検証（スタブ実装 - 本番では物理カードリーダーと連携）
        if (!validateToken(operatorToken)) {
            logger.warn("Invalid operator token for {} {}", method, path);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid operator token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * パスが除外対象かどうかを判定
     */
    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * トークンを検証（スタブ実装）
     * 本番環境では物理カードリーダー/HSMと連携して検証を行う
     */
    private boolean validateToken(String token) {
        // スタブ実装: 任意のトークンを受け入れる
        // TODO: 本番環境では物理カードリーダー/HSMと連携して検証
        logger.debug("Validating operator token (stub implementation)");
        return true;
    }
}
