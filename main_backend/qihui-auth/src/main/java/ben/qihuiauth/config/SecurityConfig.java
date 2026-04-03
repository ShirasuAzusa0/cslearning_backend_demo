package ben.qihuiauth.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 添加自定义 header 认证 filter
        http.addFilterBefore((request, response, chain) -> {
            HttpServletRequest req = (HttpServletRequest) request;

            String userId = req.getHeader("X-User-Id");
            String userName = req.getHeader("X-User-Name");
            String authoritiesStr = req.getHeader("X-User-Authorities");

            if (userId != null && userName != null) {
                List<SimpleGrantedAuthority> authorities = authoritiesStr == null || authoritiesStr.isBlank() ?
                        List.of() :
                        Arrays.stream(authoritiesStr.split(","))
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userName, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            chain.doFilter(request, response);
        }, AbstractPreAuthenticatedProcessingFilter.class);

        http
                .csrf(AbstractHttpConfigurer::disable)                                     // 前后端分离项目一般禁用 CSRF
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()         // 放行 SSE 接口
                        .requestMatchers("/api/auth/register").permitAll()              // 允许匿名访问注册接口
                        .requestMatchers("/api/auth/login").permitAll()                 // 允许匿名访问登录接口
                        .requestMatchers("/api/auth/publicKey").permitAll()             // 允许匿名访问公钥获取接口
                        .requestMatchers("/api/auth/captcha").permitAll()               // 允许匿名访问验证码获取接口
                        .requestMatchers("/api/auth/popular/posts").permitAll()         // 允许匿名访问热门帖子列表获取接口
                        .anyRequest().authenticated()                                     // 其它请求需要认证
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            // 对 SSE 返回状态码
                            System.out.println("Unauthorized: " + req.getRequestURI());
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            // 对 SSE 返回状态码
                            System.out.println("Forbidden: " + req.getRequestURI());
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        })
                );
        return http.build();
    }

    // CORS config: adjust origins in production
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));                          // dev: allow all origins
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}