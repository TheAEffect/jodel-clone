package de.hse.jodel.utils.exception;

import org.jboss.logging.Logger;

import javax.persistence.NoResultException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Exception Handler
 */
@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = Logger.getLogger(ExceptionHandler.class);

    /**
     * Exception
     * @param exception exception
     * @return Responce
     */
    @Override
    public Response toResponse(Exception exception) {
        LOGGER.error("Errorhandler");
        LOGGER.error(exception.getMessage());
        Response.Status status = exception instanceof HttpExceptions
                ? ((HttpExceptions) exception).getStatus()
                : (exception instanceof NoResultException) ? Response.Status.NOT_FOUND
                : Response.Status.INTERNAL_SERVER_ERROR;

        String message = exception.getMessage();

        return Response.status(status).entity(message).build();
    }
}
