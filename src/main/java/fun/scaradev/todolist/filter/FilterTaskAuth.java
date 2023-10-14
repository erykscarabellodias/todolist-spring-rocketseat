package fun.scaradev.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import fun.scaradev.todolist.user.IUserRepository;
import fun.scaradev.todolist.user.UserModel;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {
    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String serveltPath = request.getServletPath();

        if(!serveltPath.startsWith("/tasks")) {
            filterChain.doFilter(request, response);

            return;
        }

        String authorization = request.getHeader("Authorization");

        String base64UserPassword = authorization.substring("Basic".length()).trim();

        byte[] decodedUserPassword = Base64.getDecoder().decode(base64UserPassword);

        String userPassword = new String(decodedUserPassword);

        String[] credentials = userPassword.split(":");

        String username = credentials[0];
        String password = credentials[1];

        UserModel user = this.userRepository.findByUsername(username);

        if (user == null) {
            response.sendError(401);

            return;
        }

        BCrypt.Result passwordIsValid = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

        if(!passwordIsValid.verified) {
            response.sendError(401);

            return;
        }

        request.setAttribute("idUser", user.getId());

        filterChain.doFilter(request, response);
    }
}
