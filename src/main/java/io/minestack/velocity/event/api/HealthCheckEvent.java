package io.minestack.velocity.event.api;

import io.javalin.http.HttpCode;

public class HealthCheckEvent {

    private HttpCode responseCode;

    public HealthCheckEvent(HttpCode responseCode) {
        this.responseCode = responseCode;
    }

    public HttpCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(HttpCode responseCode) {
        this.responseCode = responseCode;
    }
}
