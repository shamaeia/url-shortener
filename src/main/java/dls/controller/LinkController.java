package dls.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import dls.database.Client;
import dls.util.Coder;

/**
 * The Controller of the program. Checks for the input parameter and based on
 * them call different methods
 */
@SuppressWarnings("deprecation")
@Controller
public class LinkController {

	@Autowired
	private HttpServletRequest request;

	/**
	 * Long URLs are guided through this method
	 * 
	 * @param url
	 *            The given long URL
	 * @param model
	 *            Model for view
	 * @return model
	 */
	@RequestMapping(value = "/lurl", method = RequestMethod.GET)
	public String longURL(
			@RequestParam(value = "url", required = false, defaultValue = "World") String url,
			Model model) {
		String shortUrl = null;
		String message = null;

		Client client = new Client();
		client.connect("127.0.0.1");
		message = "The short URL is: ";
		shortUrl = client.shortenUrl(url, 1);
		client.close();

		model.addAttribute("url", shortUrl);
		model.addAttribute("message", message);

		return "lurl";
	}

	/**
	 * 
	 * Short URLs are guided through this method
	 * 
	 * @param url
	 *            The given short URL
	 * @param model
	 *            Model for view
	 * @return model
	 */
	@RequestMapping(value = "/surl", method = RequestMethod.GET)
	public String shortURL(
			@RequestParam(value = "url", required = true) String url,
			Model model) {

		Client client = new Client();
		client.connect("127.0.0.1");

		String message;
		String longUrl = null;

		if (!Coder.isValidShortUrl(url)) {
			message = "The short URL entered is not valid! Check your input again.";
		} else {
			longUrl = client.getLongUrlFromSchema(url);

			if (longUrl == null) {
				message = "No such short URL exists!";
			} else {

				// get request headers
				Map<String, String> map = new HashMap<String, String>();

				Enumeration<String> headerNames = request.getHeaderNames();
				while (headerNames.hasMoreElements()) {
					String key = (String) headerNames.nextElement();
					String value = request.getHeader(key);
					map.put(key, value);
				}

				// get current time info
				java.util.Date date = new java.util.Date();
				Timestamp currentTimeStamp = new Timestamp(date.getTime());
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				String dateWithoutTime = sdf.format(currentTimeStamp);

				// add current statistics to database
				client.insertStats(url, dateWithoutTime, currentTimeStamp, map);

				message = "The corresponding url is: ";
			}

		}
		client.close();

		model.addAttribute("url", longUrl);
		model.addAttribute("message", message);
		return "surl";
	}

}