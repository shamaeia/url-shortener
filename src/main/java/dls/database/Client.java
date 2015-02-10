package dls.database;

import java.sql.Timestamp;
import java.util.Map;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import dls.util.Coder;
import dls.util.MD5;

/**
 * The class for querying the database
 */
public class Client {

	private Cluster cluster;
	private Session session;

	public Client() {
	}

	public void connect(String node) {
		cluster = Cluster.builder().addContactPoint(node).build();
		Metadata metadata = cluster.getMetadata();
		System.out.printf("Connected to cluster: %s\n",
				metadata.getClusterName());
		for (Host host : metadata.getAllHosts()) {
			System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
					host.getDatacenter(), host.getAddress(), host.getRack());
		}
		session = cluster.connect();
	}

	/**
	 * Query database to find the corresponding long URL associated with the
	 * given short URL
	 * 
	 * @param shortUrl
	 *            The given short URL
	 * @return The corresponding long URL stored in the database or null
	 */
	public String getLongUrlFromSchema(String shortUrl) {
		PreparedStatement statement = getSession().prepare(
				"SELECT * FROM urldb.urls " + "WHERE short_url = ?;");
		BoundStatement boundStatement = new BoundStatement(statement);
		ResultSet results = session.execute(boundStatement.bind(shortUrl));

		Row row = results.one();
		if (row == null) {
			return null;
		}
		return row.getString("long_url");
	}

	public void close() {
		session.close();
		cluster.close();
	}

	public Session getSession() {
		return this.session;
	}

	/**
	 * Tries to shorten the given long URL. If the corresponding short URL
	 * exists and is associated with different long URL, the process recursively
	 * continues until finding a unique short URL
	 * 
	 * @param longUrl
	 *            The given long URL to be shorten
	 * @param repeat
	 *            The repeat factor which is 1 by defaults
	 * @return The shortened URL
	 */
	public String shortenUrl(String longUrl, int repeat) {
		// shorten the long URL by calling the corresponding hash function
		String shortUrl = getUrlHashedValue(longUrl, repeat);

		String longUrlFromSchema = getLongUrlFromSchema(shortUrl);
		if (longUrlFromSchema == null) {
			// if no such URL exists, insert it in the database
			insertShortUrl(shortUrl, longUrl);
		} else {
			if (longUrlFromSchema.equals(longUrl)) {
				// The same long_url is requested, just return the shortened
				// value
				return shortUrl;
			} else {
				// repeat the process by rehashing
				shortUrl = shortenUrl(longUrl, ++repeat);
			}
		}

		return shortUrl;
	}

	/**
	 * Insert into the urls table the pair (short URL, long URL)
	 * 
	 * @param shortUrl
	 *            The shortened URL
	 * @param longUrl
	 *            The original long URL
	 */
	private void insertShortUrl(String shortUrl, String longUrl) {
		PreparedStatement statement = getSession().prepare(
				"INSERT INTO urldb.urls " + "(short_url, long_url) "
						+ "VALUES (?, ?);");
		BoundStatement boundStatement = new BoundStatement(statement);
		getSession().execute(boundStatement.bind(shortUrl, longUrl));
	}

	/**
	 * Calculate the hash value for a given long URL by calling MD5 hash method
	 * and masking the lower bits
	 * 
	 * @param url
	 *            The given long URL
	 * @param repeat
	 *            Repeat factor (1 is default)
	 * @return A 7 digit radix 62 string
	 */
	private String getUrlHashedValue(String url, int repeat) {
		String hashedUrl = MD5.getMD5(url, repeat);

		// Simply mask the 40 least significant bits
		// because 2^40 <= 62^7
		// an MD5 hash code is 128 bits
		String maskedHashedUrl = hashedUrl.substring(22);

		// convert the hashed value to long integer
		long maskedHashedUrlNumber = Long.parseLong(maskedHashedUrl, 16);

		// encode the long integer to base 62 string
		String shortUrl = Coder.encode(maskedHashedUrlNumber);

		return shortUrl;
	}

	/**
	 * Insert into schema the related statistics
	 * 
	 * @param shortUrl
	 *            The short URL
	 * @param date
	 *            The date in the format dd-mm-yyyy
	 * @param event_time
	 *            The time of click
	 * @param stats
	 *            HTTP header information
	 */
	public void insertStats(String shortUrl, String date, Timestamp event_time,
			Map<String, String> stats) {

		PreparedStatement statement = getSession().prepare(
				"INSERT INTO urldb.urls_by_date "
						+ "(short_url, date, event_time, stats) "
						+ "VALUES (?, ?, ?, ?);");
		BoundStatement boundStatement = new BoundStatement(statement);
		getSession().execute(
				boundStatement.bind(shortUrl, date, event_time, stats));

	}

}
