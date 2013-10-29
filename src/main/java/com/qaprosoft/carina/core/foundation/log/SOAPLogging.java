/*
 * Copyright 2013 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation.log;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.XmlFormatter;

/**
 * Enables logging of send and received SOAP messages on the specified {@link BindingProvider}s
 * 
 * @author Alex Khursevich
 */
public class SOAPLogging
{
	@SuppressWarnings("rawtypes")
	public static void enableSoapMessageLogging(final Logger logger, final BindingProvider... bindingProviders) {
	    for(final BindingProvider bindingProvider : bindingProviders) {
	        final List<Handler> handlerChain = bindingProvider.getBinding().getHandlerChain();
	        handlerChain.add(new SOAPHandler<SOAPMessageContext>() {
	            @Override
	            public boolean handleMessage(final SOAPMessageContext context) {
	                try {
	                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                    context.getMessage().writeTo(baos);
	                    System.out.println("\n" + XmlFormatter.prettyPrint(new String(baos.toByteArray())));
	                } catch(final Exception e) {
	                    logger.error("SOAP error: ", e);
	                }
	                
	                return true;
	            }

	            @Override
	            public boolean handleFault(final SOAPMessageContext context) {
	                return true;
	            }

	            @Override
	            public void close(final MessageContext context) {
	            }

	            @Override
	            public Set<QName> getHeaders() {
	                return null;
	            }
	        });
	        bindingProvider.getBinding().setHandlerChain(handlerChain);
	    }
	}
}
