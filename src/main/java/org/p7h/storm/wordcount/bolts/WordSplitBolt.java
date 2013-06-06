package org.p7h.storm.wordcount.bolts;

import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;

/**
 * Splits the tweets into words using space as the delimiter.
 *
 * @author - Prashanth Babu
 */
public final class WordSplitBolt extends BaseRichBolt {
	private static final Logger LOGGER = LoggerFactory.getLogger(WordSplitBolt.class);
	private static final long serialVersionUID = 4409872016715413315L;
	private final int minWordLength;
	private OutputCollector _collector;

	public WordSplitBolt(final int minWordLength) {
		this.minWordLength = minWordLength;
	}

	@Override
	public final void prepare(final Map map, final TopologyContext topologyContext,
	                          final OutputCollector collector) {
		this._collector = collector;
	}

	@Override
	public final void execute(final Tuple input) {
		final Status status = (Status) input.getValueByField("tweet");
		final String language = status.getUser().getLang();
		//Consider only English Language tweets, so that its easy to understand and also less input.
		if ("en".equalsIgnoreCase(language)) {
			final String tweet = status.getText().replaceAll("\\p{Punct}", " ").toLowerCase();
			final String[] words = tweet.replaceAll("\n", " ").split(" ");
			for (final String word : words) {
				if (minWordLength < word.length()) {
					_collector.emit(new Values(language, word));
				}
			}
		}
	}

	@Override
	public final void declareOutputFields(final OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("lang", "word"));
	}
}
