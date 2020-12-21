package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class authFilter implements GlobalFilter {

    @Autowired
    private UserFeignClient userFeignClient;

    private AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Value("${authUrls.url}")
    private String authUrls;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String uri = request.getURI().toString();
        System.out.println(uri);
        if (uri.contains("passport") || uri.contains("login") || uri.contains(".css") || uri.contains(".js") || uri.contains(".jpg") || uri.contains(".png") || uri.contains(".ico")) {
            return chain.filter(exchange);
        }

        // 黑名单   只有内部才能访问的接口
        boolean match = antPathMatcher.match("**/admin/**", uri);
        if (match) {
            return out(response, ResultCodeEnum.PERMISSION);
        }
        // 白名单   必须登录才能访问的接口
        String[] split = authUrls.split(",");
        boolean ifWhite = false;
        for (String url : split) {
            if (uri.contains(url)) {
                ifWhite = true;
            }
        }

        // 远程调用sso进行身份认证
        String token = getToken(request);

        Map<String, Object> userMap = null;
        if (!StringUtils.isEmpty(token)) {
            userMap = userFeignClient.verify(token);
        }
        if (null != userMap) {
            // 用户认证成功，传递用户信息
            Object user = userMap.get("user");
            UserInfo userInfo = JSON.parseObject(JSON.toJSONString(user), UserInfo.class);
            Long userId = userInfo.getId();
            // 将userId放入server服务中的request
            request.mutate().header("userId", userId + "").build();
            exchange.mutate().request(request);
            return chain.filter(exchange);
        } else {
            if (ifWhite) {
                response.setStatusCode(HttpStatus.SEE_OTHER);
                response.getHeaders().set(HttpHeaders.LOCATION, "http://passport.gmall.com/login.html?originUrl=" + uri);
                Mono<Void> voidMono = response.setComplete();
                return voidMono;
            }
        }
        return chain.filter(exchange);
    }

    private String getToken(ServerHttpRequest request) {
        String token = "";
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (null != cookies) {
            List<HttpCookie> tokenCookie = cookies.get("token");
            if (null != tokenCookie) {
                for (HttpCookie httpCookie : tokenCookie) {
                    if (httpCookie.getName().equals("token")) {
                        token = httpCookie.getValue();
                    }
                }
            }
        }

        // 异步请求从headers中获取token信息
        if (StringUtils.isEmpty(token)) {
            HttpHeaders headers = request.getHeaders();
            if (null != headers) {
                List<String> list = headers.get("token");
                if (null != list) {
                    token = list.get(0);
                }
            }
        }
        return token;
    }

    // 接口鉴权失败返回数据
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        // 返回用户没有权限登录
        Result<Object> result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer wrap = response.bufferFactory().wrap(bits);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        // 输入到页面
        return response.writeWith(Mono.just(wrap));
    }

}
