
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

public class tlsclient {
    private static final String HOST = "localhost";
    private static final int PORT = 8043;

    public static void main(String[] args) throws Exception {
        // TrustStore
        char[] passphrase_ts = "123456".toCharArray();
        KeyStore ts = KeyStore.getInstance("JKS");
        //ts.load(new FileInputStream("truststore.jks"), passphrase_ts);
        ts.load(new FileInputStream("truststoreWolf.jks"), passphrase_ts);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);
        TrustManager[] trustManagers = tmf.getTrustManagers();
 
        SSLContext context = SSLContext.getInstance("TLSv1.3");

        KeyManager[] keyManagers = null;
        context.init(keyManagers, trustManagers, new SecureRandom());

        SSLSocketFactory ssf = context.getSocketFactory();
        System.out.println("TLS client running");
        SSLSocket s = (SSLSocket) ssf.createSocket(HOST, PORT);
        //s.socket.setEnabledCipherSuites(cipher_suites);
        SSLSession session = s.getSession();
        Certificate[] cchain = session.getPeerCertificates();
        System.out.println("The Certificates used by peer");
        for (int i = 0; i < cchain.length; i++) {
            System.out.println(((X509Certificate) cchain[i]).getSubjectDN());
        };

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

        System.out.println("\n-- Connection established. Write \"stop\" to exit --.\n");

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
// ==========================================================================
