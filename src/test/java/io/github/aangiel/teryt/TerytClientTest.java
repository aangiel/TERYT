package io.github.aangiel.teryt;

import com.sun.xml.ws.fault.ServerSOAPFaultException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerytClientTest {


    @Test
    void isLoggedIn() {
        var terytClient = TerytClient.create("TestPubliczny", "1234abcd");
        var result = terytClient.czyZalogowany();
        assertTrue(result);
    }

    @Test
    void throwWhenBadCredentials() {
        var terytClient = TerytClient.create("bad", "creds");
        assertThrows(ServerSOAPFaultException.class, terytClient::czyZalogowany);
    }
}