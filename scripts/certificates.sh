#
#
#
#
#
#
#


FULLCHAIN=/path/to/fullchain.pem
PRIVKEY=/path/to/privkey.pem
KEYFILE=keystore.jks
PASSWORD=mysecret

openssl pkcs12 -export -out /tmp/keystore.pkcs12 -in $FULLCHAIN -inkey $PRIVKEY -passout pass:$PASSWORD
keytool -importkeystore -srckeystore /tmp/keystore.pkcs12 -srcstoretype PKCS12 -destkeystore $KEYFILE -srcstorepass $PASSWORD -deststorepass $PASSWORD
rm /tmp/keystore.pkcs12
