# mollyb

mollyb is a standalone gemini protocol server and library written in java, without dependencies.

## supported features

* serves .gmi files

* automatically looks for index.gmi in a directory

* uses TLS (1.2 or 1.3)

* allows to set a default "not found" page

* serves media files of type image/jpeg, image/png and image/gif

* serves feed/xml files

* writes a basic access log (if so configured)

* returns a decent "File not found" page if file or path does not exist

* dynamic pages/input handling: embedded use only (no cgi support)

## currently unsupported features

* client certificates (Identities)

## Setup


### Building

Assuming you have some recent JDK and maven installed:

* clone the repository

* inside the folder, run

```
mvn package
```

### TLS

The server does not directly work with pem files. To help you convert your fullchain.pem and privkey.pem into a .jks (java keystore) file,
see the certifcates.sh in the scripts/ folder.


### Configuration

* Copy the config/config.ini-dist file to config/config.ini

* Set the required parameters, such as the path to your webroot

### Starting the server

```
java -jar target/mollyb-server.jar /path/to/your/config.ini
```

### Does it work?

Sort of, yes.

gemini://molly.augmentedlogic.com


## Using mollyb as a library to create dynamic services 


You can get mollyb from maven central

<dependency>
    <groupId>com.augmentedlogic</groupId>
    <artifactId>mollyb</artifactId>
    <version>0.7.4</version>
</dependency>

### Setting up the embedded service

```
import com.augmentedlogic.mollyb.*;

public class Main {

    public static void main( String[] args ) {

        MollybService ms = new MollybService("0.0.0.0", 1965);
        // if you want debug output
        ms.setDebug(true);

        ms.setWebroot("/path/to/your/webroot");
        ms.setKeystore("/path/to/your/keystore.jks");
        ms.setKeystorePassword("mysecret");
        ms.setKeyPassword("mysecret");
        // set a custom not found page
        ms.setCustomNotFound("path/to/your/not_found.gmi");

        // logging
        ms.setAccessLog("/path/to/your/access.log");

        // add a path that will be handle dynamically
        ms.addHandler("/dynamic", new ExampleHandler());

        try {
            ms.start();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

}
```

Note that addHandler() accepts both exact as well as wildcards, e.g. :

```

ms.addHandler("/dynamic", new ExampleHandler());

or 

ms.addHandler("/projects/*", new ExampleHandler());

```

Now you need a handler, as an example, a handler that will take an input:


```
import com.augmentedlogic.mollyb.*;

    public class ExampleHandler implements GeminiHandler
    {

      public Response handle(Request request, Response response)
      {

            if(request.getQuery() == null) {

                response.log.debug("client is " + request.getRemoteAddress());
                response.setHeader(Response.INPUT_REQUIRED);

            } else {

                response.setHeader(Response.OK);
                response.log.debug("data recieved: " + request.getQuery());

                response.addBody("# Hello World");
                response.addBody("This is a dynamic page");
                response.addBody("QUERY was:" + request.getQuery());
                response.addBody("FROM:" + request.getRemoteAddress());
            }
            return response;
      }

    }
```
