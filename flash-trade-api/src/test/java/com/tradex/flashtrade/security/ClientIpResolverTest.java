package com.tradex.flashtrade.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ClientIpResolverTest {

    @Test
    void ignoresForwardingHeadersFromUntrustedPeer() {
        ClientIpResolver resolver = new ClientIpResolver("");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.8");
        request.addHeader("X-Forwarded-For", "198.51.100.20");
        request.addHeader("X-Real-IP", "192.0.2.10");

        assertThat(resolver.resolve(request)).isEqualTo("203.0.113.8");
    }

    @Test
    void resolvesFirstUntrustedAddressFromRightToLeftProxyChain() {
        ClientIpResolver resolver = new ClientIpResolver("10.0.0.0/8");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.2");
        request.addHeader("X-Forwarded-For", "198.51.100.20, 10.1.0.3");

        assertThat(resolver.resolve(request)).isEqualTo("198.51.100.20");
    }

    @Test
    void fallsBackToPeerWhenForwardedChainIsMalformedOrAmbiguous() {
        ClientIpResolver resolver = new ClientIpResolver("10.0.0.0/8");
        MockHttpServletRequest malformed = new MockHttpServletRequest();
        malformed.setRemoteAddr("10.0.0.2");
        malformed.addHeader("X-Forwarded-For", "spoofed");

        MockHttpServletRequest ambiguous = new MockHttpServletRequest();
        ambiguous.setRemoteAddr("10.0.0.2");
        ambiguous.addHeader("X-Forwarded-For", "198.51.100.20");
        ambiguous.addHeader("X-Forwarded-For", "198.51.100.21");

        assertThat(resolver.resolve(malformed)).isEqualTo("10.0.0.2");
        assertThat(resolver.resolve(ambiguous)).isEqualTo("10.0.0.2");
    }

    @Test
    void matchesIpv6TrustedProxyNetworks() {
        ClientIpResolver resolver = new ClientIpResolver("2001:db8:1::/48");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("2001:db8:1::2");
        request.addHeader("X-Forwarded-For", "198.51.100.20");

        assertThat(resolver.resolve(request)).isEqualTo("198.51.100.20");
    }

    @Test
    void rejectsInvalidTrustedProxyConfiguration() {
        assertThatThrownBy(() -> new ClientIpResolver("proxy.example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid trusted proxy CIDR");
        assertThatThrownBy(() -> new ClientIpResolver("0.0.0.0/0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid trusted proxy CIDR");
    }
}
