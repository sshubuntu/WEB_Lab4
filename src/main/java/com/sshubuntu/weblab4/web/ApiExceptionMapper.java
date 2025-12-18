package com.sshubuntu.weblab4.web;

import com.sshubuntu.weblab4.dto.ErrorResponse;
import com.sshubuntu.weblab4.exception.InvalidPointException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException webEx) {
            Object entity = webEx.getResponse().getEntity();
            if (entity instanceof ErrorResponse) {
                return webEx.getResponse();
            }
            return Response.status(webEx.getResponse().getStatus())
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorResponse(webEx.getMessage()))
                    .build();
        }

        if (exception instanceof InvalidPointException invalidPointException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorResponse(invalidPointException.getMessage()))
                    .build();
        }

        return Response.serverError()
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse("Internal error: " + exception.getMessage()))
                .build();
    }
}




