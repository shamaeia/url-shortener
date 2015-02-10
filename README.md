# url-shortener
Distributed URL shortener and link tracker.

The program uses Cassandra as the backend for storing long urls and their corresponding short version. A long URL is first hashed with MD5 algorithm. Then the 40 least significant bits of the hashed value is extracted and encoded to the base 62 system (26 lower case letters + 26 upper case letters + 10 digits). The encoded base 62 string which is at most 7 characters constitute the short URL.

When a short URL is requested, data about user (user agent, ip address, referer, etc) will be recorded in a time series data model in Cassandra. The database schema uses the row partitioning technique to limit the number of growing columns. In addition, it provides an efficient way to query database over the different range of times. 

The whole program is implemented as a Spring MVC web service. 

# Usage
The easiest way to run this project is to run the following command from the root directory:

java -jar target/DistributedLinkShortner-0.0.1-SNAPSHOT.jar

Then type the following in your local browser:

For converting a long url to a short one: 

localhost:8080/lurl?url=LONG_URL

For recording info about short url :

localhost:8080/surl?url=SHORT_URL

Note that no error checking has been done so far. Please exactly follow the above guidlines.

