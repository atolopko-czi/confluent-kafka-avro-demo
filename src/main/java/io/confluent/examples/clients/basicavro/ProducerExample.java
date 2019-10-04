package io.confluent.examples.clients.basicavro;

import io.confluent.kafka.schemaregistry.avro.AvroCompatibilityLevel;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.errors.SerializationException;

import java.util.Collections;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class ProducerExample {

    private static final String TOPIC = "transactions";
    public static final int PRODUCE_MESSAGE_COUNT = 3;

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(final String[] args) {

        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        // See https://docs.confluent.io/current/schema-registry/avro.html#using-compatibility-types
        props.put("avro.compatibility.level", AvroCompatibilityLevel.FULL_TRANSITIVE);

        try (KafkaProducer<String, Payment> producer = new KafkaProducer<String, Payment>(props)) {

            for (long i = 0; i < PRODUCE_MESSAGE_COUNT; i++) {
                final String orderId = "id" + UUID.randomUUID();
                final Payment payment = new Payment(orderId, new Random().nextInt(10000) / 100.00, "region" + new Random().nextInt(4));
                final ProducerRecord<String, Payment> record = new ProducerRecord<String, Payment>(TOPIC, payment.getId().toString(), payment);
                producer.send(record);
                Thread.sleep(100L);
            }

            producer.flush();
            System.out.printf("Successfully produced %d messages to a topic called %s%n", PRODUCE_MESSAGE_COUNT, TOPIC);

        } catch (final SerializationException e) {
            e.printStackTrace();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

    }

}

