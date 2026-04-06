package vn.edu.ute.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import vn.edu.ute.util.FlashMessage;

import java.io.IOException;

@WebFilter("/*")
public class FlashMessageFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest httpRequest) {
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                FlashMessage flashMessage = (FlashMessage) session.getAttribute("flashMessage");
                if (flashMessage != null) {
                    httpRequest.setAttribute("flashMessage", flashMessage);
                    session.removeAttribute("flashMessage");
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
