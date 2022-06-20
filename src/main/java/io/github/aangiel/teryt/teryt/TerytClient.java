package io.github.aangiel.teryt.teryt;

import io.github.aangiel.teryt.ws.ITerytWs1;
import io.github.aangiel.teryt.ws.TerytWs1;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.AddressingFeature;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TerytClient {


    protected TerytClient() {
    }

    public static ITerytWs1 create(String user, String password) {

        ITerytWs1 instance = new TerytWs1().getCustom(new AddressingFeature(true));
        Binding binding = ((BindingProvider) instance).getBinding();
        var handlerList = binding.getHandlerChain();
        if (handlerList == null)
            handlerList = new ArrayList<>();
        handlerList.add(new TerytHeaderHandler(user, password));
        binding.setHandlerChain(handlerList);
        return instance;
    }

    private record TerytHeaderHandler(String wsUser, String wsPassword) implements SOAPHandler<SOAPMessageContext> {

        @Override
        public boolean handleMessage(SOAPMessageContext smc) {
            Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            if (outboundProperty) {
                try {
                    SOAPEnvelope envelope = smc.getMessage().getSOAPPart().getEnvelope();
                    SOAPHeader header = envelope.getHeader();
                    SOAPElement security = header.addChildElement("Security", "wsse",
                            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
                    SOAPElement usernameToken = security.addChildElement("UsernameToken", "wsse");
                    SOAPElement username = usernameToken.addChildElement("Username", "wsse");
                    username.addTextNode(wsUser);
                    SOAPElement password = usernameToken.addChildElement("Password", "wsse");
                    password.setAttribute("Type",
                            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
                    password.addTextNode(wsPassword);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //This handler does nothing with the response from the Web Service
                //even though it should probably check its mustUnderstand headers
                @SuppressWarnings("unused")
                SOAPMessage message = smc.getMessage();
            }
            return outboundProperty;
        }

        @Override
        public boolean handleFault(SOAPMessageContext context) {
            return false;
        }

        @Override
        public void close(MessageContext context) {

        }

        // Gets the header blocks that can be processed by this Handler instance.
        @Override
        public Set<QName> getHeaders() {
            QName securityHeader = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                    "Security");
            HashSet<QName> headers = new HashSet<>();
            headers.add(securityHeader);
            return headers;
        }
    }
}