package ben.qihuigateway.filter;

import ben.qihuigateway.entity.RestBean;
import ben.qihuigateway.util.JwtUtils;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JWTAuthorizeFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;

    public JWTAuthorizeFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    // 接口白名单
    private static final List<String> WHITELIST = List.of(
            "/api/auth/login",
            "/api/auth/admin/login",
            "/api/auth/register",
            "/api/auth/publicKey",
            "/api/auth/captcha",
            "/api/auth/popular/posts"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // 只放行白名单中的接口
        boolean allowed = WHITELIST.stream().anyMatch(path::startsWith);
        if (allowed) {
            return chain.filter(exchange);
        }

        // 除白名单外，只要是 /api/** 都必须校验 token
        if (path.startsWith("/api/")) {

            String header = exchange.getRequest()
                    .getHeaders()
                    .getFirst("Authorization");

            // 没带 token 直接拒绝
            if (header == null || header.isBlank()) {
                return writeUnauthorized(exchange);
            }

            DecodedJWT decodedJWT = jwtUtils.verifyJWT(header);

            // token 无效
            if (decodedJWT == null) {
                return writeForbidden(exchange);
            }

            // ===== 解析 JWT =====
            String userName = decodedJWT.getClaim("userName").asString();
            String userId = String.valueOf(decodedJWT.getClaim("userId").asLong());

            String[] authoritiesArray =
                    decodedJWT.getClaim("authorities").asArray(String.class);

            List<SimpleGrantedAuthority> authorities =
                    authoritiesArray == null
                            ? List.of()
                            : Arrays.stream(authoritiesArray)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userName,
                            null,
                            authorities
                    );

            String authoritiesStr = authorities.stream()
                    .map(SimpleGrantedAuthority::getAuthority)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Name", userName)
                    .header("X-User-Authorities", authoritiesStr)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            return chain.filter(mutatedExchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                            Mono.just(new SecurityContextImpl(authentication))
                    ));
        }

        // 其他非 /api 请求直接放行（静态资源等）
        return chain.filter(exchange);
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String body = RestBean.failure("未登录或Token缺失").toString();

        return response.writeWith(
                Mono.just(response.bufferFactory()
                        .wrap(body.getBytes(StandardCharsets.UTF_8)))
        );
    }

    private Mono<Void> writeForbidden(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String body = RestBean.failure("JWT 无效或已过期").toString();

        return response.writeWith(
                Mono.just(response.bufferFactory()
                        .wrap(body.getBytes(StandardCharsets.UTF_8)))
        );
    }

    @Override
    public int getOrder() {
        // 优先执行
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
