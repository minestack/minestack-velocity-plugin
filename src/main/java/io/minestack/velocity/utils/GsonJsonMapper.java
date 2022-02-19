package io.minestack.velocity.utils;

import io.javalin.plugin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;

public class GsonJsonMapper implements JsonMapper {

    @NotNull
    @Override
    public <T> T fromJsonStream(@NotNull InputStream json, @NotNull Class<T> targetClass) {
        return GsonSingleton.getInstance().fromJson(new InputStreamReader(json), targetClass);
    }

    @NotNull
    @Override
    public String toJsonString(@NotNull Object obj) {
        return GsonSingleton.getInstance().toJson(obj);
    }
}