import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public abstract class TLSConnection {

    protected char[] passphrase;
    protected boolean useClientAuth;
    protected String trustStore, keyStore;

    protected TLSConnection(String passphrase, String trustStore, String keyStore) {
        this(passphrase, trustStore, keyStore, false);
    }

    protected TLSConnection(String passphrase, String trustStore, String keyStore, boolean useClientAuth) {
        this.passphrase = passphrase.toCharArray();
        this.trustStore = trustStore;
        this.keyStore = keyStore;
        this.useClientAuth = useClientAuth;
    }

    // TrustStore  set necessary truststore properties - using JKS
    //System.setProperty("javax.net.ssl.trustStore","wolvesServer_truststore.jks");
    //System.setProperty("javax.net.ssl.trustStorePassword","123456");

    public abstract void run() throws Exception;

    protected TrustManager[] getTrustManagers() throws Exception {
        KeyStore ts = KeyStore.getInstance("JKS");
        ts.load(new FileInputStream(trustStore), passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);
        return tmf.getTrustManagers();
    }

    protected KeyManager[] getKeyManagers() throws Exception {
        KeyStore ks= KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyStore), passphrase);
        // Initialize a KeyManagerFactory with the KeyStore
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
        return kmf.getKeyManagers();
    }

    protected void printCert(SSLSocket sock) throws Exception {
        SSLSession session = sock.getSession();
        var localCerts = session.getLocalCertificates();
        var otherCerts = session.getPeerCertificates();
        int i = 0;
        if (localCerts != null) {
            System.out.println("-- Local Certificates --");
            for (var cert: localCerts) {
                System.out.printf("> %d:  %s\n", ++i, ((X509Certificate) cert).getSubjectDN());
            }
            i = 0;
        } else {
            System.out.println("-- No Local certificates! --");
        }

        if (localCerts != null) {
            System.out.println("-- Remote Certificates --");
            for (var cert : otherCerts) {
                System.out.printf("> %d:  %s\n", ++i, ((X509Certificate) cert).getSubjectDN());
            }
        } else {
            System.out.println("-- No Remote certificates! --");
        }
    }
}
