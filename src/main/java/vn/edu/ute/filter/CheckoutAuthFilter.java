package vn.edu.ute.filter;

import com.google.gson.Gson;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.security.CurrentUser;
import vn.edu.ute.security.JwtUserParser;

import java.io.IOException;
import java.util.Map;

@WebFilter("/api/v1/checkout")
public class CheckoutAuthFilter implements Filter {

    private final JwtUserParser jwtUserParser = new JwtUserParser();
    private final Gson gson = new Gson();

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        CurrentUser currentUser = jwtUserParser.parseFromRequest(req);

        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(gson.toJson(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            )));
            return;
        }

        req.setAttribute("userId", currentUser.getUserId());
        req.setAttribute("email", currentUser.getEmail());
        req.setAttribute("role", currentUser.getRole());

        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
    }
}