package com.aiinterview.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

public final class NormalizedNameNormalizer {

    private NormalizedNameNormalizer() {
    }

    public static Optional<NormalizedName> normalize(String value) {
        if (value == null) {
            return Optional.empty();
        }

        String displayName = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .trim()
                .replaceAll("\\s+", " ");
        if (displayName.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new NormalizedName(displayName, displayName.toLowerCase(Locale.ROOT)));
    }

    public record NormalizedName(String displayName, String value) {
    }
}
