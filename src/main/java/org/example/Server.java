package org.example;

import java.io.*;
import java.net.URI;
import java.security.KeyStore;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.concurrent.*;
import javax.net.ssl.KeyManagerFactory;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsConfigurator;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * This code creates simple HTTPS server on localhost at port number 8443.
 * Based on article:
 * @see <a href="https://syncagio.medium.com/how-to-setup-java-httpsserver-and-keystore-eb74a8bd89d">How to setup a Java-based HTTPS server</a></a>
 */
public class Server {
    static final String workingDir = System.getProperty("user.dir");
    static final String certificate = "./_ssh/server.pfx"; // certificate
    static final String filesPath = "./src/main/resources/"; // server resources path
    static final char[] password = "11111111".toCharArray(); // password for certificate
    static final int port = 8443; // listen port
    static long time;
    static long time0 = 0;
    static final long delay = 1000; // 1 sec
    static  final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss:S");
    KeyStore keyStore;

    public static void main(String[] args) {
        new Server();
    }

    private byte[] getFile(String fileName) {
        String path = new File(filesPath, fileName).getAbsoluteFile().toPath().normalize().toString();
        try {
            File file = new File(path);
            FileInputStream fin = new FileInputStream(file);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] readData = new byte[1024];
            while (fin.read(readData) != -1) {
                buf.write(readData);
            }
            fin.close();
            buf.close();
            return buf.toByteArray();
        } catch (Exception ex){
            return "error, page not found".getBytes();
        }
    }

    public class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            URI requestURI = ex.getRequestURI();
//            System.out.println("Request: "+requestURI);
            String trimmedURI = requestURI.getPath().substring(1);
            if (trimmedURI.isBlank()) {
                trimmedURI = "index.html";
            }
//            System.out.println("Trimmer URI: "+trimmedURI);
            byte[] response = getFile(trimmedURI);
            ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            ex.sendResponseHeaders(200, response.length);
            OutputStream os = ex.getResponseBody();
            os.write(response);
            os.close();

            time = System.currentTimeMillis();
            if (time - time0 >= delay) {
                time0 = time;
                System.out.println(simpleDateFormat.format(time)+": Total threads count: "+ Thread.activeCount());
            }
//            System.out.println("Response to https://"+ex.getRemoteAddress().getHostName()+":"+ex.getRemoteAddress().getPort());
         }
    }

    Server() {
        try {
            InetSocketAddress address = new InetSocketAddress(port);
            HttpsServer httpsServer = HttpsServer.create(address, 0);
            SSLContext sslContext = getSslContext();
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        SSLContext context = getSSLContext();
                        SSLEngine engine = context.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());
                        params.setSSLParameters(context.getSupportedSSLParameters());
                    } catch (Exception ex) {
                        System.out.println("Failed to create HTTPS port");
                    }
                }
            });
            httpsServer.createContext("/", new MyHandler());
            ExecutorService executor = new ThreadPoolExecutor(100, 10000, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(2));
            httpsServer.setExecutor(executor);
            httpsServer.start();
            System.out.println("Started HTTPS server on localhost port " + port);
        } catch (Exception exception) {
            System.out.println("Failed to create HTTPS server on localhost port " + port);
            exception.printStackTrace();
        }
    }
    private SSLContext getSslContext() throws Exception {
        final KeyStore keyStore = createKeyStore();
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
        return sslContext;
    }
    private KeyStore createKeyStore() throws Exception {
        keyStore = KeyStore.getInstance("PKCS12");
        File cert = new File(workingDir, certificate).getAbsoluteFile().toPath().normalize().toFile();
        FileInputStream fis = new FileInputStream(cert);
        keyStore.load(fis, password);
        return keyStore;
    }
}

