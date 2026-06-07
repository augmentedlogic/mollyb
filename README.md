# mollyb

mollyb is a basic gemini protocol server written in java, without dependencies.

## supported features

* serves .gmi files

* automatically looks for index.gmi in a folder

* uses TLS (1.2 or 1.3)

* writes a basic access log (if so configured)

* returns a decent "File not found" page if file or path does not exist

## currently unsupported features

* does currently not serve images or other media files. (likely to be added at some point)

* does not handle interactive stuff (not so likely to be added)

## setup


### basics

Assuming you have some recent JDK and maven installed:

* clone the repository

* inside the folder, run

```
mvn package
```

### TLS

The server does not directly work with pem files. To help you convert your fullchain.pem and privkey.pem into a .jks (java keystore) file,
see the certifcates.sh in the scripts/ folder.


### configuration

* Copy the config/config.ini-dist file to config/config.ini

* Set the required parameters, such as the path to your webroot

### starting the server

```
java -jar target/mollyb-server.jar /path/to/your/config.ini
```

### Does it work?

Sort of, yes.

gemini://molly.augmentedlogic.com

