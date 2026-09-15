package com.iseekfree.common.sdk.demo;

import com.iseekfree.common.sdk.common.net.ClientIp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** The context/log IP must read as IPv4 whenever the address is IPv4 in an IPv6 coat. */
class ClientIpTest {

    @Test
    void foldsTheIpv6LoopbackToIpv4() {
        assertEquals("127.0.0.1", ClientIp.normalize("0:0:0:0:0:0:0:1"));
        assertEquals("127.0.0.1", ClientIp.normalize("::1"));
        assertEquals("127.0.0.1", ClientIp.normalize("[::1]:16802"));
    }

    @Test
    void foldsMappedAndCompatibleIpv6ToIpv4() {
        assertEquals("10.0.0.1", ClientIp.normalize("::ffff:10.0.0.1"));
        assertEquals("10.0.0.1", ClientIp.normalize("0:0:0:0:0:ffff:10.0.0.1"));
    }

    @Test
    void keepsIpv4AndRealIpv6Untouched() {
        assertEquals("127.0.0.1", ClientIp.normalize("127.0.0.1"));
        assertEquals("2001:db8::1", ClientIp.normalize("2001:db8::1"));
    }

    @Test
    void toleratesBlankAndUnresolvedValues() {
        assertNull(ClientIp.normalize(null));
        assertNull(ClientIp.normalize(""));
        assertEquals("not-an-ip", ClientIp.normalize("not-an-ip"));
    }
}
