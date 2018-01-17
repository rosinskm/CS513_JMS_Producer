/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.jms;

import java.util.Properties;
import java.util.logging.Logger;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class HelloWorldJMSClient {
    private static final Logger log = Logger.getLogger(HelloWorldJMSClient.class.getName());

    // Set up all the default values
    private static final String DEFAULT_MESSAGE = "1200 A Ohm 1000 2200 20180113 111730";
    private static final String DEFAULT_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
    private static final String DEFAULT_DESTINATION = "jms/queue/test";
    private static final String DEFAULT_MESSAGE_COUNT = "1";
    private static final String DEFAULT_USERNAME = "quickstartUser";
    private static final String DEFAULT_PASSWORD = "quickstartPwd1!";
    private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    //private static final String PROVIDER_URL = "http-remoting://192.168.1.1:8080";
    private static final String PROVIDER_URL = "http-remoting://127.0.0.1:8080";

    public static void main(String[] args) throws InterruptedException {

        Context namingContext = null;

        try {
            String userName = System.getProperty("username", DEFAULT_USERNAME);
            String password = System.getProperty("password", DEFAULT_PASSWORD);

            // Set up the namingContext for the JNDI lookup
            final Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
            env.put(Context.PROVIDER_URL, System.getProperty(Context.PROVIDER_URL, PROVIDER_URL));
            env.put(Context.SECURITY_PRINCIPAL, userName);
            env.put(Context.SECURITY_CREDENTIALS, password);
            namingContext = new InitialContext(env);

            // Perform the JNDI lookups
            String connectionFactoryString = System.getProperty("connection.factory", DEFAULT_CONNECTION_FACTORY);
            log.info("Attempting to acquire connection factory \"" + connectionFactoryString + "\"");
            ConnectionFactory connectionFactory = (ConnectionFactory) namingContext.lookup(connectionFactoryString);
            log.info("Found connection factory \"" + connectionFactoryString + "\" in JNDI");

            String destinationString = System.getProperty("destination", DEFAULT_DESTINATION);
            log.info("Attempting to acquire destination \"" + destinationString + "\"");
            Destination destination = (Destination) namingContext.lookup(destinationString);
            log.info("Found destination \"" + destinationString + "\" in JNDI");

            int count = Integer.parseInt(System.getProperty("message.count", DEFAULT_MESSAGE_COUNT));
            String content = System.getProperty("message.content", DEFAULT_MESSAGE);
            String msgAddA = "1200 A Ohm 1000 2200 20180113 ";
            //String msgAddL = "1000 L null 1000 1 20180113 ";
            String msgAddL = "1000 L null 1000 ";
            String msgDate =  " 20180113 ";
            String tampon;
            Integer heure = 111730;
            try (JMSContext context = connectionFactory.createContext(userName, password)) {
            	
            	/**
            	 *             	Envoie de 20 messages contenant des mesures logiques  et 20 messages contenant des mesures analogiques
            	 */
            	
            	for (int u = 0; u < 20; u++){
            		
            		tampon = msgAddA + String.valueOf(heure + u);
            		content = System.getProperty("message.content", tampon);
	            	log.info("Sending " + count + " messages with content: " + content);
	            	
	                // Send the specified number of messages
	                
	            	for (int i = 0; i < count; i++) {
							context.createProducer().send(destination, content);
	                }
	            	Thread.sleep(100);
	            	if(u%2 == 0) {
	            		tampon = msgAddL + 1 + msgDate + String.valueOf(heure + u);
	            	}else {
	            		tampon = msgAddL + 0 + msgDate + String.valueOf(heure + u);
	            	}
            		content = System.getProperty("message.content", tampon);
            		
            		log.info("Sending " + count + " messages with content: " + content);
	                // Send the specified number of messages
	                
	            	for (int i = 0; i < count; i++) {
							context.createProducer().send(destination, content);
	                }
	            	Thread.sleep(10);
            	}
            }
        } catch (NamingException e) {
            log.severe(e.getMessage());
        } finally {
            if (namingContext != null) {
                try {
                    namingContext.close();
                } catch (NamingException e) {
                    log.severe(e.getMessage());
                }
            }
        }
    }
}
