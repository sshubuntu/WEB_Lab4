package com.sshubuntu.weblab4.web;

import com.sshubuntu.weblab4.dto.AuthResponse;
import com.sshubuntu.weblab4.dto.ErrorResponse;
import com.sshubuntu.weblab4.dto.LoginRequest;
import com.sshubuntu.weblab4.dto.RegisterRequest;
import com.sshubuntu.weblab4.entity.UserAccount;
import com.sshubuntu.weblab4.service.AuthService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class AuthResource {

    private static final String SESSION_USER_KEY = "userId";

    @Inject
    private AuthService authService;

    @Context
    private HttpServletRequest request;

    @POST
    @Path("/login")
    public Response login(LoginRequest payload) {
        if (payload == null || payload.getUsername() == null || payload.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Введите логин и пароль"))
                    .build();
        }

        String username = payload.getUsername().trim().toLowerCase();
        String password = payload.getPassword();
        if (username.isBlank() || password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Логин и пароль не могут быть пустыми"))
                    .build();
        }

        Optional<UserAccount> userOpt = authService.findByUsername(username);
        if (userOpt.isEmpty() || !authService.passwordMatches(userOpt.get(), password)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Неверные учетные данные"))
                    .build();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER_KEY, userOpt.get().getId());

        return Response.ok(AuthResponse.from(userOpt.get())).build();
    }

    @GET
    @Path("/me")
    public Response me() {
        Optional<UserAccount> userOpt = currentUser();
        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Необходимо войти в систему"))
                    .build();
        }
        return Response.ok(AuthResponse.from(userOpt.get())).build();
    }

    @POST
    @Path("/register")
    public Response register(RegisterRequest payload) {
        if (payload == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Пустое тело запроса"))
                    .build();
        }

        String username = payload.getUsername();
        String password = payload.getPassword();

        if (username == null || username.trim().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Введите имя пользователя"))
                    .build();
        }
        if (password == null || password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Введите пароль"))
                    .build();
        }
        if (password.length() < 4) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Пароль должен содержать не менее 4 символов"))
                    .build();
        }

        username = username.trim().toLowerCase();
        if (authService.findByUsername(username).isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("Пользователь с таким именем уже существует"))
                    .build();
        }

        UserAccount newUser = authService.createUser(username, password);

        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER_KEY, newUser.getId());

        return Response.status(Response.Status.CREATED)
                .entity(AuthResponse.from(newUser))
                .build();
    }

    @POST
    @Path("/logout")
    public Response logout() {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Response.noContent().build();
    }

    Optional<UserAccount> currentUser() {
        HttpSession session = request.getSession(false);
        if (session == null) return Optional.empty();
        Object userId = session.getAttribute(SESSION_USER_KEY);
        if (userId instanceof Integer) {
            userId = ((Integer) userId).longValue();
        }
        if (userId instanceof Long) {
            return authService.findById((Long) userId);
        }
        return Optional.empty();
    }
}


