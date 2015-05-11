package wikipedia.eventquery;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.jsoup.select.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for querying events that occurred on a specific day of month. Events are extracted from the Wikipedia
 * page about a specific day of month.
 */
public class EventQuery {

	private static Logger	logger = LoggerFactory.getLogger(EventQuery.class);

	private static DateTimeFormatter	formatter = DateTimeFormat.forPattern("YYYY GG").withLocale(Locale.ENGLISH);

	/**
	 * Default value of the timeout for accessing Wikipedia pages.
	 */
	private static final int	TIMEOUT = 5000;

	/**
	 * The timeout for accessing Wikipedia pages in milliseconds.
	 */
	private int	timeout = TIMEOUT;

	/**
	 * Constructs a {@code EventQuery} object with default timeout.
	 *
	 * @see #getTimeout()
	 * @see #setTimeout(int)
	 */
	public EventQuery() {
	}

	/**
	 * Constructs a {@code EventQuery} object with the timeout specified.
	 *
	 * @param timeout the timeout for accessing Wikipedia pages in milliseconds
	 *
	 * @see #getTimeout()
	 * @see #setTimeout(int)
	 */
	public EventQuery(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Returns the timeout for accessing Wikipedia pages.
	 *
	 * @return the timeout for accessing Wikipedia pages in milliseconds
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Sets the timeout for accessing Wikipedia pages.
	 *
	 * @param timeout the timeout for accessing Wikipedia pages in milliseconds
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Returns the web page address of the Wikipedia page about the day of month specified.
	 *
	 * @param monthDay an object that wraps the month of year and the day of month
	 * @return the web page address of the Wikipedia page about the day of month specified, as a string
	 */
	private String buildWikipediaURL(MonthDay monthDay) {
		final String	month = monthDay.monthOfYear().getAsText(Locale.ENGLISH);
		return String.format("http://en.wikipedia.org/wiki/%s_%d", month, monthDay.getDayOfMonth());
	}

	/**
	 * Performs a query for events that occurred on the day of month specified.
	 *
	 * @param monthDay an object that wraps the month of year and the day of month
	 * @return the list of objects that represent events that occurred on the date specified
	 * @throws IOException if any I/O error occurs during the execution of the query
	 */
	public List<Event> query(MonthDay monthDay) throws IOException {
		String	url = buildWikipediaURL(monthDay);
		logger.info("Retrieving web page from {}", url);
		Document	doc = Jsoup.connect(url).timeout(timeout).get();
		List<Event>	events = new ArrayList<Event>();
		Elements	elements = doc.select("h2:has(#Events) + ul > li");
		logger.info("Found {} event(s)", elements.size());
		for (Element element: elements) {
			String[]	parts = element.text().split(" \u2013 ", 2);
			if (parts.length != 2) {
				logger.warn("Skipping a malformed event");
				continue;
			}
			parts[0] = parts[0].trim();
			parts[1] = parts[1].trim();
			int	year = 0;
			try {
				year = Integer.parseInt(parts[0].trim());
			} catch(NumberFormatException e) {
				year = formatter.parseLocalDate(parts[0]).getYear();
			}
			Event	event = new Event(new LocalDate(year, monthDay.getMonthOfYear() , monthDay.getDayOfMonth()), parts[1]);
			events.add(event);
		}
		return events;
	}

	/**
	 * Performs a query for events that occurred on the day of month specified.
	 *
	 * @param monthOfYear the month of the year (1&ndash;12)
	 * @param dayOfMonth the day of the month (1&ndash;31)
	 * @return the list of objects that represent events that occurred on the date specified
	 * @throws IOException if any I/O error occurs during the execution of the query
	 */
	public List<Event> query(int monthOfYear, int dayOfMonth) throws IOException {
		return query(new MonthDay(monthOfYear, dayOfMonth));
	}

}
