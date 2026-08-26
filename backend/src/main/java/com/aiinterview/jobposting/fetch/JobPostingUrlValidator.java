package com.aiinterview.jobposting.fetch;

import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;

@Component
public class JobPostingUrlValidator {

    public URI validate(String rawUrl) {
        final URI uri;
        try {
            uri = URI.create(rawUrl);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.JOB_POSTING_URL_NOT_ALLOWED);
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (!StringUtils.hasText(scheme) || !StringUtils.hasText(host)
                || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                || uri.getUserInfo() != null || isLocalHostname(host)) {
            throw new BusinessException(ErrorCode.JOB_POSTING_URL_NOT_ALLOWED);
        }

        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (isBlockedAddress(address)) {
                    throw new BusinessException(ErrorCode.JOB_POSTING_URL_NOT_ALLOWED);
                }
            }
        } catch (UnknownHostException e) {
            throw new BusinessException(ErrorCode.JOB_POSTING_FETCH_FAILED);
        }

        return uri;
    }

    private boolean isLocalHostname(String host) {
        String normalizedHost = host.toLowerCase(Locale.ROOT);
        return "localhost".equals(normalizedHost) || normalizedHost.endsWith(".localhost");
    }

    private boolean isBlockedAddress(InetAddress address) {
        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()
                || isCarrierGradeNat(address)
                || isUniqueLocalIpv6(address);
    }

    private boolean isCarrierGradeNat(InetAddress address) {
        if (!(address instanceof Inet4Address)) {
            return false;
        }
        byte[] bytes = address.getAddress();
        int first = Byte.toUnsignedInt(bytes[0]);
        int second = Byte.toUnsignedInt(bytes[1]);
        return first == 100 && second >= 64 && second <= 127;
    }

    private boolean isUniqueLocalIpv6(InetAddress address) {
        if (!(address instanceof Inet6Address)) {
            return false;
        }
        return (Byte.toUnsignedInt(address.getAddress()[0]) & 0xFE) == 0xFC;
    }
}
