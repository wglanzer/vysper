/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.vysper.xmpp.extension.websockets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.vysper.xmpp.protocol.SessionStateHolder;
import org.apache.vysper.xmpp.server.ServerRuntimeContext;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 */
public class XmppWebSocketServlet extends WebSocketServlet {
    
    public static final String SERVER_RUNTIME_CONTEXT_ATTRIBUTE = "org.apache.vysper.xmpp.server.ServerRuntimeContext";

    private final static Logger LOG = LoggerFactory.getLogger(XmppWebSocketServlet.class);
    
    private static final long serialVersionUID = 197413099255392883L;
    private static final String SUB_PROTOCOL = "xmpp";

    private ServerRuntimeContext serverRuntimeContext;
    
    public XmppWebSocketServlet() {
        // default cstr needed
    }
    
    public XmppWebSocketServlet(ServerRuntimeContext serverRuntimeContext) {
        this.serverRuntimeContext = serverRuntimeContext;
    }
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        if(serverRuntimeContext == null) {
            serverRuntimeContext = (ServerRuntimeContext) getServletContext().getAttribute(SERVER_RUNTIME_CONTEXT_ATTRIBUTE);
            if(serverRuntimeContext == null) {
                throw new RuntimeException("Failed to get Vysper ServerRuntimeContext from servlet context attribute \"" + SERVER_RUNTIME_CONTEXT_ATTRIBUTE + "\"");
            }
        }        
    }

    protected WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        if(SUB_PROTOCOL.equals(protocol)) {
            WebSocketBackedSessionContext sessionContext = new WebSocketBackedSessionContext(serverRuntimeContext);
            return sessionContext;
        } else {
            LOG.warn("Unsupported WebSocket sub protocol, must be \"xmpp\"");
            return null;
        }
    }
}
