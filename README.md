HTTPResolver
============

HTTPResolver is a web service which does DNS lookups for you.


Deployment
----------

```bash
docker run -d --name httpresolver -p 8080:8080 mkroli/httpresolver
```


Usage
-----

Go to http://127.0.0.1:8080/google.de
