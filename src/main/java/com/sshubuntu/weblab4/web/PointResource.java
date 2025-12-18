package com.sshubuntu.weblab4.web;

import com.sshubuntu.weblab4.dto.ErrorResponse;
import com.sshubuntu.weblab4.dto.PointRequest;
import com.sshubuntu.weblab4.dto.PointResponse;
import com.sshubuntu.weblab4.entity.PointResult;
import com.sshubuntu.weblab4.entity.UserAccount;
import com.sshubuntu.weblab4.exception.InvalidPointException;
import com.sshubuntu.weblab4.service.AuthService;
import com.sshubuntu.weblab4.service.ResultService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

@Path("/points")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class PointResource {

    private static final String SESSION_USER_KEY = "userId";

    @Inject
    private ResultService resultService;

    @Inject
    private AuthService authService;

    @Context
    private HttpServletRequest request;

    @POST
    public Response check(PointRequest payload) {
        Optional<UserAccount> userOpt = currentUser();
        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorResponse("Необходимо войти в систему")).build();
        }

        if (payload == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse("Пустое тело запроса")).build();
        }

        double x = valueOrThrow(payload.getX(), "X");
        double y = valueOrThrow(payload.getY(), "Y");
        double r = valueOrThrow(payload.getR(), "R");

        PointResult saved = resultService.registerPoint(userOpt.get(), x, y, r);
        return Response.ok(PointResponse.from(saved)).build();
    }

    @GET
    @Path("/results")
    public Response results() {
        Optional<UserAccount> userOpt = currentUser();
        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Необходимо войти в систему"))
                    .build();
        }
        List<PointResponse> payload = resultService.toDto(resultService.fetchAllOrdered(userOpt.get()));
        return Response.ok(payload).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePoint(@PathParam("id") Long id) {
        Optional<UserAccount> userOpt = currentUser();
        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Необходимо войти в систему"))
                    .build();
        }
        resultService.deletePoint(userOpt.get(), id);
        return Response.ok().build();
    }

    @DELETE
    @Path("/all")
    public Response deleteAllPoints() {
        Optional<UserAccount> userOpt = currentUser();
        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Необходимо войти в систему"))
                    .build();
        }
        resultService.deleteAllPoints(userOpt.get());
        return Response.ok().build();
    }

    private double valueOrThrow(Double value, String name) {
        if (value == null) {
            throw new InvalidPointException("Введите " + name);
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new InvalidPointException(name + " должен быть числом");
        }
        return value;
    }

    private Optional<UserAccount> currentUser() {
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


