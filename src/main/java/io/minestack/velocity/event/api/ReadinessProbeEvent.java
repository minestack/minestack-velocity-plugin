package io.minestack.velocity.event.api;

import io.javalin.http.HttpCode;

public class ReadinessProbeEvent extends HealthCheckEvent {
    public ReadinessProbeEvent(HttpCode responseCode) {
        super(responseCode);
    }
}
