package com.koubaibu.config;

import com.koubaibu.filter.RequestIdFilter;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * セキュリティ設定
 * Security Configuration
 * 
 * Extends VaadinWebSecurity to properly integrate with Vaadin's CSRF protection.
 * This ensures that Vaadin's internal CSRF mechanism is used while still
 * allowing proper security configuration for REST APIs.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    @Value("${security.mtls.enabled:false}")
    private boolean mtlsEnabled;

    @Value("${security.operator-token.enabled:false}")
    private boolean operatorTokenEnabled;

    private final RequestIdFilter requestIdFilter;

    public SecurityConfig(RequestIdFilter requestIdFilter) {
        this.requestIdFilter = requestIdFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Let Vaadin handle CSRF protection for Vaadin requests
        // Only disable CSRF for specific API endpoints if needed
        
        http
            // H2コンソール用にフレームを許可
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            
            // 認証設定（ログイン不要のため全て許可）
            .authorizeHttpRequests(auth -> auth
                // H2コンソール（開発用）- CSRF無効化が必要
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                // アクチュエータ
                .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
                // API - Vaadin以外のREST APIもCSRF保護される
                .requestMatchers(new AntPathRequestMatcher("/api/**")).permitAll()
            )
            
            // RequestIDフィルターを追加
            .addFilterBefore(requestIdFilter, BasicAuthenticationFilter.class);
        
        // H2コンソール用にCSRF無効化（開発環境のみ）
        http.csrf(csrf -> csrf
            .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
        );
        
        // Call parent to configure Vaadin security (including its CSRF handling)
        super.configure(http);
        
        // mTLSが有効な場合の設定（スタブ）
        if (mtlsEnabled) {
            // 本番環境でmTLSを有効にする場合はここに設定を追加
            // http.x509(...)
        }
    }
}
