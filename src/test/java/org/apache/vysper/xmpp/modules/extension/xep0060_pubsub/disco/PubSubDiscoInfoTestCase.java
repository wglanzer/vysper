package org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.disco;

import java.util.Arrays;
import java.util.List;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.modules.core.base.handler.IQHandler;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.AbstractPublishSubscribeTestCase;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.handler.AbstractStanzaGenerator;
import org.apache.vysper.xmpp.modules.servicediscovery.handler.DiscoInfoIQHandler;
import org.apache.vysper.xmpp.protocol.NamespaceURIs;
import org.apache.vysper.xmpp.protocol.ResponseStanzaContainer;
import org.apache.vysper.xmpp.stanza.IQStanza;
import org.apache.vysper.xmpp.stanza.IQStanzaType;
import org.apache.vysper.xmpp.stanza.Stanza;
import org.apache.vysper.xmpp.stanza.StanzaBuilder;
import org.apache.vysper.xmpp.xmlfragment.XMLElement;


public class PubSubDiscoInfoTestCase extends AbstractPublishSubscribeTestCase {
    
    @Override
    protected AbstractStanzaGenerator getDefaultStanzaGenerator() {
        return new DefaultDiscoInfoStanzaGenerator();
    }

    @Override
    protected IQHandler getHandler() {
        return new DiscoInfoIQHandler();
    }

    
    public void testIdentityAndFeature() {
        DefaultDiscoInfoStanzaGenerator sg = (DefaultDiscoInfoStanzaGenerator)getDefaultStanzaGenerator();
        Stanza stanza = sg.getStanza(client, pubsubService.getBareJID(), "id123");

        ResponseStanzaContainer result = sendStanza(stanza, true);
        assertTrue(result.hasResponse());
        IQStanza response = new IQStanza(result.getResponseStanza());

        assertEquals(IQStanzaType.RESULT.value(),response.getType());

        assertEquals("id123", response.getAttributeValue("id")); // IDs must match

        // get the query Element
        XMLElement query = response.getFirstInnerElement();
        List<XMLElement> inner = query.getInnerElements();

        assertEquals("query", query.getName());
        
        // at least we have an identity and a feature element
        assertTrue(inner.size() >= 2);
        
        // ordering etc. is unknown; step through all subelements and pick the ones we need
        XMLElement identity = null;
        XMLElement feature = null;
        for(XMLElement el : inner) {
            if(el.getName().equals("identity")
                    //&& el.getNamespace().equals(NamespaceURIs.XEP0030_SERVICE_DISCOVERY_INFO) // TODO enable when the parser is fixed
                    && el.getAttributeValue("category").equals("pubsub")
                    && el.getAttributeValue("type").equals("service")) {
                identity = el;
            } else if(el.getName().equals("feature")
                    /*&& el.getNamespace().equals(NamespaceURIs.XEP0030_SERVICE_DISCOVERY_INFO)*/  // TODO enable when the parser is fixed
                    && el.getAttributeValue("var").equals(NamespaceURIs.XEP0060_PUBSUB)) {
                feature = el;
            }
        }
        
        // make sure they were there (booleans would have sufficed)
        assertNotNull(identity);
        assertNotNull(feature);
    }
    
    public void testInfoRequestForANode() throws Exception {
        root.createNode(serverEntity, "news", "News");

        DefaultDiscoInfoStanzaGenerator sg = (DefaultDiscoInfoStanzaGenerator)getDefaultStanzaGenerator();
        Stanza stanza = sg.getStanza(client, pubsubService.getBareJID(), "id123","news");

        ResponseStanzaContainer result = sendStanza(stanza, true);
        assertTrue(result.hasResponse());
        IQStanza response = new IQStanza(result.getResponseStanza());

        assertEquals(IQStanzaType.RESULT.value(),response.getType());

        assertEquals("id123", response.getAttributeValue("id")); // IDs must match
        
        // get the query Element
        XMLElement query = response.getFirstInnerElement();
        List<XMLElement> inner = query.getInnerElements();

        assertEquals("query", query.getName());
        
        // at least we have an identity element
        assertTrue(inner.size() >= 1);
        
        // ordering etc. is unknown; step through all subelements and pick the ones we need
        XMLElement identity = null;
        for(XMLElement el : inner) {
            if(el.getName().equals("identity")
                    //&& el.getNamespace().equals(NamespaceURIs.XEP0030_SERVICE_DISCOVERY_INFO) // TODO enable when the parser is fixed
                    && el.getAttributeValue("category").equals("pubsub")
                    && el.getAttributeValue("type").equals("leaf")) {
                identity = el;
            }
        }
        
        // make sure they were there
        assertNotNull(identity);
        
        XMLElement[] elementList = collectFeatures(inner, new String[] {NamespaceURIs.XEP0060_PUBSUB});
        for(XMLElement el : elementList) {
            assertNotNull(el);
        }
    }

    private XMLElement[] collectFeatures(List<XMLElement> inner, String[] features) {
        XMLElement[] elementList = new XMLElement[features.length];
        Arrays.sort(features);
        for(XMLElement el : inner) {
            if(el.getName().equals("feature"))
                    /*&& el.getNamespace().equals(NamespaceURIs.XEP0030_SERVICE_DISCOVERY_INFO)*/ { // TODO enable when the parser is fixed
                int index = Arrays.binarySearch(features, el.getAttributeValue("var"));
                if(index != -1) {
                    elementList[index] = el;
                }
            }
        }
        return elementList;
    }
    
    class DefaultDiscoInfoStanzaGenerator extends AbstractStanzaGenerator {
        @Override
        protected StanzaBuilder buildInnerElement(Entity client, Entity pubsub, StanzaBuilder sb, String node) {
            return sb;
        }

        @Override
        protected String getNamespace() {
            return NamespaceURIs.XEP0030_SERVICE_DISCOVERY_INFO;
        }

        @Override
        protected IQStanzaType getStanzaType() {
            return IQStanzaType.GET;
        }
        
        public Stanza getStanza(Entity client, Entity pubsub, String id) {
            StanzaBuilder stanzaBuilder = StanzaBuilder.createIQStanza(client, pubsub, getStanzaType(), id);
            stanzaBuilder.startInnerElement("query");
            stanzaBuilder.addNamespaceAttribute(getNamespace());

            return stanzaBuilder.getFinalStanza();
        }
        
        @Override
        public Stanza getStanza(Entity client, Entity pubsub, String id, String node) {
            StanzaBuilder stanzaBuilder = StanzaBuilder.createIQStanza(client, pubsub, getStanzaType(), id);
            stanzaBuilder.startInnerElement("query");
            stanzaBuilder.addNamespaceAttribute(getNamespace());
            stanzaBuilder.addAttribute("node", node);

            stanzaBuilder.endInnerElement();

            return stanzaBuilder.getFinalStanza();
        }
    }
}
