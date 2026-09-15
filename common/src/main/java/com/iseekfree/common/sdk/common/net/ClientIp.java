package com.iseekfree.common.sdk.common.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Normalizes a client address for the context and the request logs.
 *
 * <p>The JDK renders the IPv6 loopback as {@code 0:0:0:0:0:0:0:1} and an
 * IPv4-mapped peer as {@code ::ffff:10.0.0.1}; operators expect the familiar
 * IPv4 form. {@link #normalize(String)} therefore folds the IPv6 loopback and
 * any IPv4-mapped/compatible IPv6 address back to IPv4, and leaves a real IPv6
 * address untouched.</p>
 */
public final class ClientIp {

    private static final String IPV4_LOOPBACK = "127.0.0.1";

    private ClientIp() {
    }

    public static String normalize(String ip) {
        String value = unwrap(ip);
        if (value == null || value.indexOf(':') < 0) {
            return value;
        }
        try {
            InetAddress address = InetAddress.getByName(value);
            if (address.isLoopbackAddress()) {
                return IPV4_LOOPBACK;
            }
            // The JDK already collapses an IPv4-mapped address (::ffff:a.b.c.d)
            // to Inet4Address; return the folded form instead of the input text.
            if (address instanceof Inet4Address) {
                return address.getHostAddress();
            }
            if (address instanceof Inet6Address) {
                byte[] bytes = address.getAddress();
                if (isMapped(bytes) || isCompatible(bytes)) {
                    byte[] ipv4 = { bytes[12], bytes[13], bytes[14], bytes[15] };
                    return InetAddress.getByAddress(ipv4).getHostAddress();
                }
            }
        } catch (UnknownHostException notAnAddress) {
            return value;
        }
        return value;
    }

    /** IPv4-mapped IPv6: 10 zero bytes followed by {@code ff ff} and the IPv4 address. */
    private static boolean isMapped(byte[] bytes) {
        if (bytes.length != 16 || bytes[10] != (byte) 0xff || bytes[11] != (byte) 0xff) {
            return false;
        }
        for (int i = 0; i < 10; i++) {
            if (bytes[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /** IPv4-compatible IPv6: the first 12 bytes are zero. */
    private static boolean isCompatible(byte[] bytes) {
        if (bytes.length != 16) {
            return false;
        }
        for (int i = 0; i < 12; i++) {
            if (bytes[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /** Trims, strips a leading {@code /} (InetSocketAddress), brackets and a {@code %zone} suffix. */
    private static String unwrap(String ip) {
        if (ip == null) {
            return null;
        }
        String value = ip.trim();
        if (value.startsWith("/")) {
            value = value.substring(1);
        }
        if (value.startsWith("[") && value.indexOf(']') > 0) {
            value = value.substring(1, value.indexOf(']'));
        }
        int zone = value.indexOf('%');
        if (zone > 0) {
            value = value.substring(0, zone);
        }
        return value.isEmpty() ? null : value;
    }
}
