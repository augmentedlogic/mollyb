# mollyb

mollyb is a basic gemini protocol server written in java, without dependencies.

## supported features

serves .gmi file
automatically looks for index.gmi in a folder

## currently unsupported features

does not handle interactive stuff

does currently not serve images or other media files

## setup


### basics

clone the repository

mvn package


### TLS

The server does not directly work with pem files. To help you convert your fullchain.pem and privkey.pem into a .jks (java keystore) file,
see the certifcaites.sh in the scripts/ folder.


### configuration

Copy the config/config.ini-dist file to config/config.ini

Set the required parameters, such as the path to your webroot

### starting the server

```
java -jar target/mollyb-server /path/to/your/config.ini
```



