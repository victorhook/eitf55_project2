#!/usr/bin/bash

CONF=openssl.cnf
CA_ROOT_DIR=demoCA
CA_ROOT="/C=SE/ST=SCANIA/L=LUND/O=LTH/CN=ANIMALS-CA/emailAddress=ca@animals.com"
CA_wolves="/C=SE/ST=SCANIA/L=LUND/O=LTH/CN=wolvesCA/emailAddress=]wolvesca@animals.com"
CA_sheep="/C=SE/ST=SCANIA/L=LUND/O=LTH/CN=sheepCA/emailAddress=sheep@animals.com"
SERVER_wolves="CN=wolvesServer,O=LTH,L=LUND,ST=SCANIA,C=SE"
SERVER_sheep="CN=sheepServer,O=LTH,L=LUND,ST=SCANIA,C=SE"
SERVER_test="CN=testServer,O=LTH,L=LUND,ST=SCANIA,C=SE"
CLIENT="CN=client,O=LTH,L=LUND,ST=SCANIA,C=SE"
DOLLY="CN=dolly,O=LTH,L=LUND,ST=SCANIA,C=SE"
PASSWORD=123456
SERVER_EXT_V3=server_v3.txt
USE_EXTENSIONS=1

echo \
"[ v3_req ]
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = keyAgreement, keyEncipherment, digitalSignature
" > ${SERVER_EXT_V3}

# Create directory structure
create_dir_structure() {
    cp ../${CONF} .
    mkdir demoCA
    cd demoCA
    echo "01" > serial
    touch index.txt
    mkdir newcerts
    cd ..
}


clean() {
    rm -rf demoCA *.crt *.csr *.jks *.key *.srl
}

clean

echo "> Generating directory structure"
create_dir_structure

# Generate keys
echo "> Generating RSA keys"
openssl genrsa -out demoCA/CA.key 4096
openssl genrsa -out wolvesCA.key 4096
openssl genrsa -out sheepCA.key 4096

# Self-sign certificate for root CA
echo "> Self-sign root CA certificate"
openssl req -x509 -new -key demoCA/CA.key -days 3560 -config ${CONF} -out demoCA/CA.crt -subj ${CA_ROOT}

# Generate certificate requests for subCA's
echo "> Create CSR for subCA's"
openssl req -new -key wolvesCA.key -config openssl.cnf -out wolvesCA.csr -subj ${CA_wolves}
openssl req -new -key sheepCA.key -config openssl.cnf -out sheepCA.csr -subj ${CA_wolves}

# Sign certificate requests from subCA's with roots key.
echo "> Sign subCA's CSR with root CA"
openssl x509 -req -days 365 -in wolvesCA.csr -CA demoCA/CA.crt -CAkey demoCA/CA.key -CAcreateserial -next_serial -out wolvesCA.crt -extfile ${CONF} -extensions v3_ca
openssl x509 -req -days 365 -in sheepCA.csr -CA demoCA/CA.crt -CAkey demoCA/CA.key -CAcreateserial -next_serial -out sheepCA.crt -extfile ${CONF} -extensions v3_ca

# Create keys for servers, with java keytool.
echo "> Generating RSA keys for servers, version 3"
keytool -genkey -dname ${SERVER_wolves} -alias wolvesServer -keystore wolvesServer.jks -keyalg RSA -sigalg Sha256withRSA -storepass ${PASSWORD}
keytool -genkey -dname ${SERVER_sheep} -alias sheepServer -keystore sheepServer.jks -keyalg RSA -sigalg Sha256withRSA -storepass ${PASSWORD}
echo "> Generating RSA key for client"
keytool -genkey -dname ${CLIENT} -alias client -keystore client.jks -keyalg RSA -sigalg Sha256withRSA -storepass ${PASSWORD}
echo "> Generating RSA keys for servers, version 1"
keytool -genkey -dname ${SERVER_wolves} -alias wolvesServer_v1 -keystore wolvesServer_v1.jks -keyalg RSA -sigalg Sha256withRSA -storepass ${PASSWORD}
keytool -genkey -dname ${SERVER_sheep} -alias sheepServer_v1 -keystore sheepServer_v1.jks -keyalg RSA -sigalg Sha256withRSA -storepass ${PASSWORD}
keytool -genkey -dname ${SERVER_test} -alias testServer -keystore testServer.jks -keyalg RSA -sigalg Sha256withRSA -storepass ${PASSWORD}

# Generate certificate requests for servers
echo "> Generating csr for servers..."
keytool -certreq -alias wolvesServer -ext san=DNS:localhost -keystore wolvesServer.jks -file wolvesServer.csr -storepass ${PASSWORD}
keytool -certreq -alias sheepServer -ext san=DNS:localhost -keystore sheepServer.jks -file sheepServer.csr -storepass ${PASSWORD}
keytool -certreq -alias wolvesServer_v1 -ext san=DNS:localhost -keystore wolvesServer_v1.jks -file wolvesServer_v1.csr -storepass ${PASSWORD}
keytool -certreq -alias sheepServer_v1 -ext san=DNS:localhost -keystore sheepServer_v1.jks -file sheepServer_v1.csr -storepass ${PASSWORD}
keytool -certreq -alias testServer -ext san=DNS:localhost -keystore testServer.jks -file testServer.csr -storepass ${PASSWORD}
echo "> Generating csr for client"
keytool -certreq -alias client -ext san=DNS:localhost -keystore client.jks -file client.csr -storepass ${PASSWORD}

# Sign certificate requests
echo "> Signing csr for servers"
openssl x509 -req -days 365 -in wolvesServer.csr -CA wolvesCA.crt -CAkey wolvesCA.key -CAcreateserial -next_serial -out wolvesServer.crt -extfile ${CONF} -extensions v3_ca
openssl x509 -req -days 365 -in sheepServer.csr -CA sheepCA.crt -CAkey sheepCA.key -CAcreateserial -next_serial -out sheepServer.crt -extfile ${CONF} -extensions v3_ca
openssl x509 -req -days 365 -in wolvesServer_v1.csr -CA wolvesCA.crt -CAkey wolvesCA.key -CAcreateserial -next_serial -out wolvesServer_v1.crt
openssl x509 -req -days 365 -in sheepServer_v1.csr -CA sheepCA.crt -CAkey sheepCA.key -CAcreateserial -next_serial -out sheepServer_v1.crt
openssl x509 -req -days 365 -in testServer.csr -CA wolvesCA.crt -CAkey wolvesCA.key -CAcreateserial -next_serial -out testServer.crt -extfile ${CONF} -extensions v3_ca
echo "> Signing csr for client"
openssl x509 -req -days 365 -in client.csr -CA wolvesCA.crt -CAkey wolvesCA.key -CAcreateserial -next_serial -out client.crt -extfile ${CONF} -extensions v3_ca
# Create expired certificate
openssl x509 -req -days 0 -in wolvesServer.csr -CA wolvesCA.crt -CAkey wolvesCA.key -CAcreateserial -next_serial -out wolvesServer_expired.crt -extfile ${CONF} -extensions v3_ca

# Create chains
echo "> Creating certificate chains..."
cp wolvesCA.crt chain.crt && cat wolvesServer.crt >> chain.crt
cp wolvesCA.crt chain_client.crt && cat client.crt >> chain_client.crt
cp wolvesCA.crt chain_test.crt && cat testServer.crt >> chain_test.crt
cp wolvesCA.crt chain_expired.crt && cat wolvesServer_expired.crt >> chain_expired.crt

echo "> Import certificates with into keystore (server)..."
keytool -noprompt -importcert -file chain.crt -keystore wolvesServer.jks -storepass ${PASSWORD} -alias wolvesServer
keytool -noprompt -importcert -file chain_test.crt -keystore testServer.jks -storepass ${PASSWORD} -alias other

echo "> Import certificates with into keystore (client)..."
keytool -noprompt -importcert -file chain_client.crt -keystore client.jks -storepass ${PASSWORD} -alias client

# Uncomment this to add expired certificate to keystore.
#keytool -noprompt -importcert -file wolvesServer_expired.crt -keystore wolvesServer.jks -storepass ${PASSWORD} -alias wolvesServer_expired

echo "> Import certificates into truststore (server)..."
keytool -noprompt -import -file demoCA/CA.crt -alias rootCA -trustcacerts -keystore wolvesServer_truststore.jks -storepass ${PASSWORD}


echo "> Import certificates into truststore (client)..."
keytool -noprompt -import -file demoCA/CA.crt -alias rootCA -trustcacerts -keystore client_truststore.jks -storepass ${PASSWORD}
keytool -noprompt -import -file wolvesCA.crt -alias wolfCA -trustcacerts -keystore client_truststoreWolf.jks -storepass ${PASSWORD}

echo "> Creating client dolly..."
keytool -genkey -dname ${DOLLY} -alias dolly -keystore dolly.jks -keyalg RSA -sigalg Sha256withRSA -storepass ${PASSWORD}
keytool -certreq -alias dolly -ext san=DNS:localhost -keystore dolly.jks -file dolly.csr -storepass ${PASSWORD}
openssl x509 -req -days 365 -in dolly.csr -CA sheepCA.crt -CAkey sheepCA.key -CAcreateserial -next_serial -out dolly.crt -extfile ${CONF} -extensions v3_ca
cp sheepCA.crt chain_dolly.crt && cat dolly.crt >> chain_dolly.crt
keytool -noprompt -importcert -file chain_dolly.crt -keystore dolly.jks -storepass ${PASSWORD} -alias dolly
keytool -noprompt -import -file demoCA/CA.crt -alias rootCA -trustcacerts -keystore dolly_truststore.jks -storepass ${PASSWORD}

echo "> Copying keystores and truststores into source dir"
cp wolvesServer.jks ../src/
cp wolvesServer_truststore.jks ../src/
cp client.jks ../src/
cp client_truststore.jks ../src/
cp dolly.jks ../src/
cp dolly_truststore.jks ../src/
