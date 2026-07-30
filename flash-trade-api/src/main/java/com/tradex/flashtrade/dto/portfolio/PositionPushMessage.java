package com.tradex.flashtrade.dto.portfolio;

import java.time.Instant;
import java.util.List;

public class PositionPushMessage {

    private final String type = "POSITION_UPDATE";
    private final List<PositionResponse> data;
    private final Instant timestamp;

    public PositionPushMessage(List<PositionResponse> data) {
        this.data      = data;
        this.timestamp = Instant.now();
    }

    public String getType()                   { return type; }
    public List<PositionResponse> getData()   { return data; }
    public Instant getTimestamp()             { return timestamp; }
}
