
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
    static char[]  passphrase = "123456".toCharArray();
    static String TRUST_STORE = "client_truststore.jks",
            KEY_STORE   = "client.jks";

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

        SSLContext context = SSLContext.getInstance("TLSv1.3");

        context.init(getKeyManagers(), getTrustManagers(), null);

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
// ==========================================================================
