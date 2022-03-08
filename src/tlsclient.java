
//==========================================================================
//Sample tlsclient using sslsockets
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Scanner;
import javax.net.ssl.*;

public class tlsclient extends TLSConnection {
    private final String HOST = "localhost";
    private final int PORT = 8043;

    public tlsclient() {
        super(
                "123456",
                //"dolly_truststore.jks",
                "client_truststoreWolf.jks",
                //"client_truststoreSheep.jks",
                //"cheat.jks",
                "dolly.jks"
        );
    }

    public static void main(String[] args) throws Exception {
        new tlsclient().run();
    }

    public void run() throws Exception {
        SSLContext context = SSLContext.getInstance("TLSv1.3");
        context.init(getKeyManagers(), getTrustManagers(), null);

        SSLSocketFactory ssf = context.getSocketFactory();
        System.out.println("TLS client running");
        SSLSocket s = (SSLSocket) ssf.createSocket(HOST, PORT);

        printCert(s);

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

        System.out.println("\n-- Connection established. Write \"stop\" to exit --.");

        String msg = "";
        var buf = new ArrayList<String>();
        while (!msg.equals("stop")) {
            msg = stdin.readLine();
            out.write(msg + "\n");
            out.flush();
            // Append server echo to buffer
            buf.add(in.readLine());
        }

        System.out.println("Closing connection.");
        System.out.println("Server echo:");
        buf.forEach(m -> System.out.printf("> %s\n", m));

        stdin.close();
        in.close();
        out.close();
        s.close();
    }

}