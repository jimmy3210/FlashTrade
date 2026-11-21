package com.tradex.flashtrade.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public final class ClientIpResolver {

    private static final int MAX_FORWARDED_FOR_LENGTH = 2048;
    private static final int MAX_PROXY_HOPS = 32;
    private static final Pattern IPV6_CHARACTERS = Pattern.compile("[0-9a-fA-F:.]+");

    private final List<IpNetwork> trustedProxyNetworks;

    public ClientIpResolver(@Value("${app.security.trusted-proxies:}") String trustedProxies) {
        this.trustedProxyNetworks = parseTrustedProxies(trustedProxies);
    }

    public String resolve(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        Address peer = Address.parse(remoteAddress).orElse(null);
        if (peer == null) {
            return "unknown";
        }
        if (!isTrustedProxy(peer)) {
            return peer.value();
        }

        Enumeration<String> headerValues = request.getHeaders("X-Forwarded-For");
        if (headerValues == null) {
            return peer.value();
        }
        List<String> headers = Collections.list(headerValues);
        if (headers.size() != 1) {
            return peer.value();
        }

        String forwardedFor = headers.get(0);
        if (forwardedFor == null || forwardedFor.isBlank()
                || forwardedFor.length() > MAX_FORWARDED_FOR_LENGTH) {
            return peer.value();
        }

        String[] forwardedAddresses = forwardedFor.split(",", -1);
        if (forwardedAddresses.length > MAX_PROXY_HOPS) {
            return peer.value();
        }

        List<Address> chain = new ArrayList<>(forwardedAddresses.length);
        for (String forwardedAddress : forwardedAddresses) {
            Optional<Address> address = Address.parse(forwardedAddress);
            if (address.isEmpty()) {
                return peer.value();
            }
            chain.add(address.get());
        }

        for (int index = chain.size() - 1; index >= 0; index--) {
            Address address = chain.get(index);
            if (!isTrustedProxy(address)) {
                return address.value();
            }
        }
        return chain.get(0).value();
    }

    private boolean isTrustedProxy(Address address) {
        return trustedProxyNetworks.stream().anyMatch(network -> network.contains(address));
    }

    private static List<IpNetwork> parseTrustedProxies(String configuredProxies) {
        if (configuredProxies == null || configuredProxies.isBlank()) {
            return List.of();
        }

        List<IpNetwork> networks = new ArrayList<>();
        for (String configuredProxy : configuredProxies.split(",", -1)) {
            if (configuredProxy.isBlank()) {
                throw new IllegalArgumentException("Trusted proxy entries must not be empty");
            }
            networks.add(IpNetwork.parse(configuredProxy.trim()));
        }
        return List.copyOf(networks);
    }

    private record Address(byte[] bytes, String value) {

        private Address {
            bytes = bytes.clone();
        }

        private static Optional<Address> parse(String value) {
            if (value == null || value.isBlank()) {
                return Optional.empty();
            }

            String candidate = value.trim();
            if (candidate.indexOf(':') >= 0) {
                if (!IPV6_CHARACTERS.matcher(candidate).matches()) {
                    return Optional.empty();
                }
                try {
                    byte[] bytes = InetAddress.getByName(candidate).getAddress();
                    if (bytes.length != 16) {
                        return Optional.empty();
                    }
                    return Optional.of(new Address(bytes, InetAddress.getByAddress(bytes).getHostAddress()));
                } catch (UnknownHostException e) {
                    return Optional.empty();
                }
            }

            String[] octets = candidate.split("\\.", -1);
            if (octets.length != 4) {
                return Optional.empty();
            }
            byte[] bytes = new byte[4];
            for (int index = 0; index < octets.length; index++) {
                String octet = octets[index];
                if (octet.isEmpty() || octet.length() > 3
                        || (octet.length() > 1 && octet.charAt(0) == '0')
                        || !octet.chars().allMatch(character -> character >= '0' && character <= '9')) {
                    return Optional.empty();
                }
                int numericOctet;
                try {
                    numericOctet = Integer.parseInt(octet);
                } catch (NumberFormatException e) {
                    return Optional.empty();
                }
                if (numericOctet > 255) {
                    return Optional.empty();
                }
                bytes[index] = (byte) numericOctet;
            }
            return Optional.of(new Address(bytes, (bytes[0] & 0xff) + "." + (bytes[1] & 0xff)
                    + "." + (bytes[2] & 0xff) + "." + (bytes[3] & 0xff)));
        }

        @Override
        public byte[] bytes() {
            return bytes.clone();
        }
    }

    private record IpNetwork(byte[] address, int prefixLength) {

        private IpNetwork {
            address = address.clone();
        }

        private static IpNetwork parse(String configuredNetwork) {
            String[] parts = configuredNetwork.split("/", -1);
            if (parts.length > 2) {
                throw new IllegalArgumentException("Invalid trusted proxy CIDR: " + configuredNetwork);
            }

            Address address = Address.parse(parts[0])
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid trusted proxy CIDR: " + configuredNetwork));
            int addressBits = address.bytes().length * Byte.SIZE;
            int prefixLength = addressBits;
            if (parts.length == 2) {
                try {
                    prefixLength = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid trusted proxy CIDR: " + configuredNetwork, e);
                }
            }
            if (prefixLength <= 0 || prefixLength > addressBits) {
                throw new IllegalArgumentException("Invalid trusted proxy CIDR: " + configuredNetwork);
            }
            return new IpNetwork(address.bytes(), prefixLength);
        }

        private boolean contains(Address candidate) {
            byte[] candidateBytes = candidate.bytes();
            if (address.length != candidateBytes.length) {
                return false;
            }

            int fullBytes = prefixLength / Byte.SIZE;
            for (int index = 0; index < fullBytes; index++) {
                if (address[index] != candidateBytes[index]) {
                    return false;
                }
            }

            int remainingBits = prefixLength % Byte.SIZE;
            if (remainingBits == 0) {
                return true;
            }
            int mask = 0xff << (Byte.SIZE - remainingBits);
            return (address[fullBytes] & mask) == (candidateBytes[fullBytes] & mask);
        }

        @Override
        public byte[] address() {
            return address.clone();
        }
    }
}
