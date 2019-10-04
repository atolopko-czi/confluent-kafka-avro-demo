# Java Producer and Consumer with Avro

This directory includes projects demonstrating how to use the Java producer and consumer with Avro and Confluent Schema Registry

How to use these examples:

* [ProducerExample.java](src/main/java/io/confluent/examples/clients/basicavro/ProducerExample.java): see [Confluent Schema Registry tutorial](https://docs.confluent.io/current/schema-registry/docs/schema_registry_tutorial.html)
* [ConsumerExample.java](src/main/java/io/confluent/examples/clients/basicavro/ConsumerExample.java): see [Confluent Schema Registry tutorial](https://docs.confluent.io/current/schema-registry/docs/schema_registry_tutorial.html)

## Demo

0. Install Confluent platform locally using Docker:
    https://docs.confluent.io/current/quickstart/ce-docker-quickstart.html
1. Produce messages for schema v1 (missing future "region" field):
    ```
    git co schema-v1 && mvn clean package exec:java -Dexec.mainClass=io.confluent.examples.clients.basicavro.ProducerExample
    ```
    Note the automatically registered schema: 
    ```
    curl -X GET http://localhost:8081/subjects/
    curl -X GET http://localhost:8081/subjects/transactions-value/versions
    ```
2. Consume messages for schema v1:
    ```
    git co schema-v1 && mvn clean package exec:java -Dexec.mainClass=io.confluent.examples.clients.basicavro.ConsumerExample
    ```
3. Attempt to change Producer's schema to use a new, but _incompatible_ version (no default value for "region"):
    ```
    git co schema-v2-invalid && mvn clean package exec:java -Dexec.mainClass=io.confluent.examples.clients.basicavro.ProducerExample
    ```
    Note the runtime error:
    ```
    org.apache.kafka.common.errors.SerializationException: Error registering Avro schema: {"type":"record","name":"Payment","namespace":"io.confluent.examples.clients.basicavro","fields":[{"name":"id","type":"string","logicalType":"UUID"},{"name":"amount","type":"double"},{"name":"region","type":"string"}]}
    Caused by: io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException: Schema being registered is incompatible with an earlier schema; error code: 409
    ```
4. Consume messages for schema v2:
    ```
    git co schema-v1 && mvn clean package exec:java -Dexec.mainClass=io.confluent.examples.clients.basicavro.ConsumerExample
    ```
    Note that consumer accepts schema v1 messages, using default value.

5. Produce messages for schema 2 (contains "region" field):
    ```
    git co schema-v2 && mvn clean package exec:java -Dexec.mainClass=io.confluent.examples.clients.basicavro.ProducerExample
    ```
    Note that v1 consumer accepts schema v2 messages, ignoring new region field value.
    Note that v2 consumer accepts schema v2 messages, showing new region field value.


* To replay consumers with existing messages (of any schema version), first terminate consumer processes, wait for consume group to become "stable", and then run:
    ```
    /kafka-consumer-groups.sh --bootstrap-server localhost:9092 --reset-offsets --to-earliest --all-topics --execute --group transactions-v1
    /kafka-consumer-groups.sh --bootstrap-server localhost:9092 --reset-offsets --to-earliest --all-topics --execute --group transactions-v2
    ```
* To run through demo again, first reset Kafka state (topic and schemas):
    * `kafka-topics.sh --zookeeper localhost:2181/ --delete --topic transaction`
    * `curl -X DELETE http://localhost:8081/subjects/transactions-value`
* To modify and test other schema compatibility level checking by Confluent Schema Registry:
    ```
    $ curl -X GET http://localhost:8081/config
    {"compatibilityLevel":"BACKWARD"}
    $ $ curl -X PUT -H "Content-Type: application/json" -d '{"compatibility":"FULL_TRANSITIVE"}' http://localhost:8081/config
    ```
    See:
    * https://docs.confluent.io/current/schema-registry/develop/api.html#compatibility
    * https://docs.confluent.io/current/schema-registry/avro.html#compatibility-types
    * https://docs.confluent.io/current/schema-registry/avro.html#order-of-upgrading-clients
