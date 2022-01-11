package de.hse.jodel.utils.exception;

import javax.ws.rs.core.Response;

/**
 * HttpExceptions Handler
 */
public class HttpExceptions extends Exception {

    private Response.Status status;

    /**
     * Gets status
     * @return status
     */
    public Response.Status getStatus() {
        return status;
    }

    /**
     * Default constructor
     */
    public HttpExceptions() {
        super();
    }

    /**
     * Constructor
     * @param msg message
     * @param status status
     */
    public HttpExceptions(String msg, Response.Status status) {
        super(msg);

        this.status = status;
    }

    /**
     * Constructor
     * @param msg message
     * @param e exception
     * @param status status
     */
    public HttpExceptions(String msg, Exception e, Response.Status status) {
        super(msg, e);

        this.status = status;
    }
}