package com.koubaibu.config;

import com.koubaibu.filter.RequestIdFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * セキュリティ設定
 * Security Configuration
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${security.mtls.enabled:false}")
    private boolean mtlsEnabled;

    @Value("${security.operator-token.enabled:false}")
    private boolean operatorTokenEnabled;

    private final RequestIdFilter requestIdFilter;

    public SecurityConfig(RequestIdFilter requestIdFilter) {
        this.requestIdFilter = requestIdFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRFを無効化（開発環境用、本番ではVaadinのCSRF保護を使用）
            .csrf(AbstractHttpConfigurer::disable)
            
            // H2コンソール用にフレームを許可
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            
            // 認証設定（ログイン不要のため全て許可）
            .authorizeHttpRequests(auth -> auth
                // Vaadinの内部リソース
                .requestMatchers("/VAADIN/**").permitAll()
                // H2コンソール（開発用）
                .requestMatchers("/h2-console/**").permitAll()
                // アクチュエータ
                .requestMatchers("/actuator/**").permitAll()
                // API
                .requestMatchers("/api/**").permitAll()
                // その他
                .anyRequest().permitAll()
            )
            
            // RequestIDフィルターを追加
            .addFilterBefore(requestIdFilter, BasicAuthenticationFilter.class);
        
        // mTLSが有効な場合の設定（スタブ）
        if (mtlsEnabled) {
            // 本番環境でmTLSを有効にする場合はここに設定を追加
            // http.x509(...)
        }
        
        return http.build();
    }
}
