package io.minestack.velocity.event.api;

import io.javalin.http.HttpCode;

public class LivenessProbeEvent extends HealthCheckEvent {
    public LivenessProbeEvent(HttpCode responseCode) {
        super(responseCode);
    }
}
