package de.e2security.netflow_flowaggregation.kafka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;

import com.espertech.esper.client.EPServiceProvider;

/**
 * KafkaConsumerMaster class has to delegate job for multi-threaded KafkaConsumers [class: KafkaConsumerCallable] ; 
 * Each Consumer should be assigned to its own group, which in turn consists of multiple threads with its own instance of KafkaConsumerCallable;
 * Number of threads equals number of partitions for particular topic (by default in kafka ONE for each topic);
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class KafkaConsumerMaster {

	private List<String> topics;
	private EPServiceProvider engine;
	private KafkaConsumer consumerGeneral;
	private Properties configIn;
	private Map<String,ExecutorService> groupId_executor;
	
	/**
	 * @param engine ONE Esper Engine for each groupID 
	 * Reason: multithreading by Engine itself with really good throughput for one instance (up to 500.000 events)
	 * @see http://esper.espertech.com/release-7.0.0/esper-reference/html_single/index.html#api-threading-advanced
	 * @see http://esper.espertech.com/release-7.0.0/esper-reference/html_single/index.html#perf-tips-3-a
	 */
	public KafkaConsumerMaster(EPServiceProvider engine) {
		this.engine = engine;
	}
	
	/**
	 * @param config Configuration used by each KafkaConsumerCallable
	 * @see https://kafka.apache.org/11/documentation.html#newconsumerconfigs
	 *  bootstrap.servers: list of host/port to use for establishing the initial connection. 
	 *  				   The name of docker container used (e.g. kafka). 
	 *  				   For dev you can specify localhost for your local instance;
	 *  enable.auto.commit: if true -> the consumer's offset will be periodically committed in the background.
	 *  					Set to false due to manual commit handling by KafkaConsumerCallable.
	 *  				    It will be committed only if all the records from ConsumerRecords collection were sent to Esper
	 *  					see class KafkaConsumerCallable, call() method;
	 * 	group.id: specifies the name of the consumer group a kafka consumer belongs to
	 *  		  Generated as follows within startWorkers() method:
	 *  		  group.id.prefix from custom configuration (application.properties) concatenated with the name of topic 
	 *  		  as we group consumers within one group regarding specific topic;
	 *  client.id: to be able to track the source of requests beyond just ip/port by allowing a logical application name
	 *  		   to be included in server-side request logging;
	 *  		   Can be used for debugging of Kafka Server;
	 *  		   Generated as follows: concatenation of custom consumer.client.id + # of Thread (per partition). 
	 *  deserializer: can be custom classes;
	 * @return itself in order to be instantiated in methods chain
	 */
	public KafkaConsumerMaster startWorkers(Properties config) {
		this.groupId_executor = new HashMap<>();
		this.configIn = new Properties();
		configIn.put("bootstrap.servers", config.get("bootstrap.servers"));
		configIn.put("enable.auto.commit", config.get("enable.auto.commit"));
		configIn.put("session.timeout.ms", config.get("session.timeout.ms"));
		configIn.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		configIn.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		this.topics = new ArrayList<String>(Arrays.asList(config.getProperty("consumer.topics").split(",")));
		topics.forEach(topic -> {
			int partitionCount = getPartionsNumber(topic);
			ExecutorService exec = Executors.newFixedThreadPool(partitionCount);
			String groupId = config.get("consumer.group.id.prefix") + topic;
			configIn.put("group.id", groupId);
			this.groupId_executor.put(groupId, exec);
			IntStream.rangeClosed(1, partitionCount).forEach(partitionCounter -> {
				configIn.put("client.id", config.get("consumer.client.id") + "_Thread #" + String.valueOf(partitionCounter));
				KafkaConsumerCallable consumer = new KafkaConsumerCallable<>(configIn, topic, engine);
				exec.submit(consumer);
			});
		});
		return this;
	}

	//TODO: test whether the number of partitions can be read correctly from kafka with the following custom method:
	private int getPartionsNumber(String topic) {
		int count;
		consumerGeneral = new KafkaConsumer<>(configIn);
		Map<String, List<PartitionInfo>> listTopics = (Map<String, List<PartitionInfo>>) consumerGeneral.listTopics();
		if (listTopics.isEmpty()) {
			throw new RuntimeException("No Topics are available to read from Kafka -> no any KafkaConsumer can be created");
		} else {
			count = listTopics.get(topic).size();
		}
		consumerGeneral.close();
		return count;
	}
	
	/**
	 * implementing shutdown for each thread;
	 * threads are grouped by Executor with Thread Pool regarding each kafka group in groupId_executor Map
	 */
	
	public List<String> getKafkaGroups() {
		return groupId_executor.keySet().stream().collect(Collectors.toList());
	}
	
	public void closeThreads(String groupId) {
		this.groupId_executor.get(groupId).shutdown();
	}

}
