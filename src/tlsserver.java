
//==========================================================================
//Sample tlsserver using sslsockets
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

public class tlsserver {
    // likely this port number is ok to use
    private static final int PORT = 8043;
    static char[]  passphrase = "123456".toCharArray();
    static String TRUST_STORE = "wolvesServer_truststore.jks",
                  KEY_STORE   = "wolvesServer.jks";

    private static TrustManager[] getTrustManagers() throws Exception {
        KeyStore ts = KeyStore.getInstance("JKS");
        ts.load(new FileInputStream(TRUST_STORE), passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);
        return tmf.getTrustManagers();
    }

    private static KeyManager[] getKeyManagers() throws Exception {
        KeyStore ks= KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(KEY_STORE), passphrase);
        // Initialize a KeyManagerFactory with the KeyStore
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
        return kmf.getKeyManagers();
    }

    public static void main(String[] args) throws Exception {
        // TrustStore  set necessary truststore properties - using JKS
        //System.setProperty("javax.net.ssl.trustStore","wolvesServer_truststore.jks");
        //System.setProperty("javax.net.ssl.trustStorePassword","123456");

        // Create an SSLContext to run TLS and initialize it with
        SSLContext context = SSLContext.getInstance("TLSv1.3");
        context.init(getKeyManagers(), getTrustManagers(), null);

        SSLServerSocketFactory ssf = context.getServerSocketFactory();
        ServerSocket ss = ssf.createServerSocket(PORT);

        while (true) {
            try {
                System.out.println("Waiting for connection...");
                SSLSocket s = (SSLSocket)ss.accept();
                s.setNeedClientAuth(true);
                System.out.println("New Connection, waiting for input...");
                //s.setNeedClientAuth​(true);

                // below works only when client is authenticated
                //SSLSession session = ((SSLSocket) s).getSession();
                //Certificate[] cchain = session.getPeerCertificates();
                //System.out.println("The Certificates used by peer");
                //for (int i = 0; i < cchain.length; i++) {
                //    System.out.println(((X509Certificate) cchain[i]).getSubjectDN());
                //};
                //Socket s = ss.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

                String line;
                while (((line = in.readLine()) != null)) {
                    System.out.printf("[Server receive] %s\n", line);
                    if (line.equals("stop"))
                        break;
                    out.write(line + "\n");
                    out.flush();
                }

                System.out.println("Connection closed.");
                out.close();
                in.close();
                s.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
// =========================================================================
