package myapps;

import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
 
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
 
public class Test {
 
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "ckyrouac-kstreams-test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "platform-mq-ci-kafka-bootstrap.platform-mq-ci.svc:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
 
        final StreamsBuilder builder = new StreamsBuilder();


        final KStream<Long, String> ckyrouacTestStream = builder.stream("ckyrouac-test-stream", Consumed.with(Serdes.Long(), Serdes.String()));

        final GlobalKTable<Long, String> ckyrouacTestGlobal =
                builder.globalTable("ckyrouac-test-global", Materialized.as("ckyrouac-test-global"));

        final KStream<Long, String> joinedStream = ckyrouacTestStream.leftJoin(ckyrouacTestGlobal,
                                                                        (leftKey, leftValue) -> leftKey + 100,
                                                                        (leftValue, rightValue) -> "left=" + leftValue + ", right=" + rightValue);
        joinedStream.to("ckyrouac-test-enriched", Produced.with(Serdes.Long(), Serdes.String()));
     
        final Topology topology = builder.build();
 
        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);
 
        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });
 
        try {
            System.out.println("Starting app...");
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
