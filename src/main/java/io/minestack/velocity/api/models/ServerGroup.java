package io.minestack.velocity.api.models;

import com.google.gson.annotations.Expose;

import java.util.Map;

public class ServerGroup {

    @Expose
    private String name = null;

    @Expose
    private Map<String, String> servers = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getServers() {
        return servers;
    }

    public void setServers(Map<String, String> servers) {
        this.servers = servers;
    }
}
