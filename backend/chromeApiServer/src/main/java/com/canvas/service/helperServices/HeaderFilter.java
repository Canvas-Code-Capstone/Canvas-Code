package com.canvas.service.helperServices;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Filter class for header processing.
 */
@Component
public class HeaderFilter implements Filter {

    /**
     * Constructor for dependency injection of AESCryptoService.
     * @param aesCryptoService instance of AESCryptoService
     */
    @Autowired
    private final AESCryptoService aesCryptoService;

    @Autowired
    public HeaderFilter(AESCryptoService aesCryptoService) {
        this.aesCryptoService = aesCryptoService;
    }

    /**
     * Implements the doFilter method to process the header by implementing the filter class,
     * all requests that come in go through this method.
     *
     * @param request servlet request
     * @param response servlet response
     * @param chain filter chain
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // logic to filter URLs to implement Authz
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
