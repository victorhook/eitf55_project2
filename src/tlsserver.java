
//==========================================================================
//Sample tlsserver using sslsockets
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

public class tlsserver extends TLSConnection {
    // likely this port number is ok to use
    private static final int PORT = 8043;

    public tlsserver() {
        super(
                "123456",
                "wolvesServer_truststore.jks",
                //"client_truststoreWolf.jks",
                //"client_truststoreSheep.jks",
                "wolvesServer.jks",
                true
        );
    }

    public static void main(String[] args) throws Exception {
        new tlsserver().run();
    }

    public void run() throws Exception {
        // Create an SSLContext to run TLS and initialize it with
        SSLContext context = SSLContext.getInstance("TLSv1.3");
        context.init(getKeyManagers(), getTrustManagers(), null);

        SSLServerSocketFactory ssf = context.getServerSocketFactory();
        ServerSocket ss = ssf.createServerSocket(PORT);

        while (true) {
            try {
                System.out.println("Waiting for connection...");
                SSLSocket s = (SSLSocket)ss.accept();

                if (useClientAuth)
                    s.setNeedClientAuth(true);

                System.out.println("New Connection, waiting for input...");
                //s.setNeedClientAuth​(true);

                printCert(s);

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
