package com.peercollab.backend.dto.analytics;

public record MetricPointResponse(
        String label,
        long value
) {
}
