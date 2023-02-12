package com.canvas.service.helperServices;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;
@Component
public class HeaderFilter implements Filter {

    @Autowired
    private final AESCryptoService aesCryptoService;

    @Autowired
    public HeaderFilter(AESCryptoService aesCryptoService) {
        this.aesCryptoService = aesCryptoService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // logic to not do AuthZ for below by urls
        if (httpRequest.getRequestURI().toLowerCase().startsWith("/oauth2response") ||
            httpRequest.getRequestURI().startsWith("/login")) {
            chain.doFilter(request, response);
            return;
        }

        String headerValue = httpRequest.getHeader("Authorization");

        // process request based on header value
        if (StringUtils.isBlank(headerValue)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } else {
            try {
                String decryptedToken = "Bearer " + this.aesCryptoService.decrypt(headerValue,"This is a secret key");

                // Wrap the request with a custom implementation that returns the modified header value
                HttpServletRequest modifiedRequest = new HttpServletRequestWrapper(httpRequest) {
                    @Override
                    public String getHeader(String name) {
                        if ("Authorization".equals(name)) {
                            return decryptedToken;
                        }
                        return super.getHeader(name);
                    }
                };
                chain.doFilter(modifiedRequest, httpResponse);
            } catch (Exception e) {
                e.printStackTrace();
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
    }
}
