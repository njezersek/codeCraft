package org.eu.jezersek.http;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import org.eu.jezersek.App;

public class WebServer{
    public HttpServer httpServer;

    public WebServer(App plugin) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(plugin.getConfig().getInt("port")), 0);
            httpServer.createContext("/", new RequestHandler(plugin));
            httpServer.setExecutor(null);
            httpServer.start();
            System.out.println("Server is starting.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}