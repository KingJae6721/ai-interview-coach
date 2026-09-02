package com.aiinterview.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NormalizedNameNormalizerTest {

    @Test
    void normalize_appliesNfkcWhitespaceCollapseAndRootLowercase() {
        NormalizedNameNormalizer.NormalizedName normalized = NormalizedNameNormalizer
                .normalize("  Ｏｐｅｎ   ＡＩ  ")
                .orElseThrow();

        assertThat(normalized.displayName()).isEqualTo("Open AI");
        assertThat(normalized.value()).isEqualTo("open ai");
    }

    @Test
    void normalize_returnsEmptyForNullOrBlank() {
        assertThat(NormalizedNameNormalizer.normalize(null)).isEmpty();
        assertThat(NormalizedNameNormalizer.normalize(" \t \n ")).isEmpty();
    }
}
