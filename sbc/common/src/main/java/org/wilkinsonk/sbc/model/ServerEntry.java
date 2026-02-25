package org.wilkinsonk.sbc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ServerEntry(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("iconMaterial") String iconMaterial,
    @JsonProperty("isOnline") Boolean isOnline,
    @JsonProperty("isCurrentPlayerServer") Boolean isCurrentPlayerServer
) {}
