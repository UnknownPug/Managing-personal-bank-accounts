package api.mpba.rastvdmy.dto.response;

import lombok.Builder;

@Builder
public record JwtAuthResponse(
        long expiresIn,

        String token
) {}