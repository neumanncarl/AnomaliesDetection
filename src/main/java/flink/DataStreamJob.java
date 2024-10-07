/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package flink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.core.fs.Path;
import org.apache.flink.formats.csv.CsvReaderFormat;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import flink.operators.*;
import org.example.pojo.Anomaly;
import org.example.pojo.EquipmentEvent;
import org.example.serdes.EquipmentEventDeserializer;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

/**
 * Skeleton for a Flink DataStream Job.
 *
 * <p>For a tutorial how to write a Flink application, check the
 * tutorials and examples on the <a href="https://flink.apache.org">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class DataStreamJob {

//	public static final String KAFKA_BOOTSTRAP = "172.24.33.55:9094";
	public static final String KAFKA_BOOTSTRAP = "localhost:9092";

	public static void main(String[] args) throws Exception {
		Instant start = Instant.now();
		System.out.println(String.format("Program started at %s", start.toString()));
		// Sets up the execution environment, which is the main entry point
		// to building Flink applications.
		KafkaSource<EquipmentEvent> equipmentSrc = KafkaSource.<EquipmentEvent>builder()
				.setTopics("huge")
				.setBootstrapServers(KAFKA_BOOTSTRAP)
				.setGroupId("equipmenteventReader2s")
				.setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
				.setValueOnlyDeserializer(new EquipmentEventDeserializer())
				.build();

		WatermarkStrategy<EquipmentEvent> watermarkStrategy = WatermarkStrategy.
				<EquipmentEvent>forGenerator(context -> new org.example.operators.EquipmentWatermarkGenerator())
				.withTimestampAssigner((element, recordTimestamp) -> element.getTimestamp().getTime());



		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


		DataStreamSource<EquipmentEvent> equipmentEventDataStreamSource =  env.fromSource(equipmentSrc, watermarkStrategy, "equipment_data");

		Properties props = new Properties();
		props.setProperty("bootstrap.servers", "localhost:9092"); // Define Kafka server
		props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		String outputTopic = "huge-flink-out-test2";
		Properties properties = new Properties();
		properties.setProperty("bootstrap.servers", "localhost:9092");

		FlinkKafkaProducer<Anomaly> kafkaSink = new FlinkKafkaProducer<>(
				outputTopic,
				new AnomalyKafkaSerializer(outputTopic),  // Use the custom SerDe here
				properties,
				FlinkKafkaProducer.Semantic.EXACTLY_ONCE);

		equipmentEventDataStreamSource
				.keyBy(EquipmentEvent::getEquipment)
				.window(GlobalWindows.create())
				.trigger(new org.example.operators.EquipmentWindowTrigger())
				.process(new org.example.operators.EquipmentWindowProcess())
				.addSink(kafkaSink);
//				.addSink(new PrintSinkFunction<>());



//		DataStreamSource<EquipmentEvent> equipmentEventDataStreamSource =  env.fromSource(equipmentSrc, watermarkStrategy, "equipment_data").addSink(new PrintSinkFunction<>());

//		equipmentEventDataStreamSource.keyBy(EquipmentEvent::getEquipment).process(new ProcessJob()).addSink(new PrintSinkFunction<>());


		env.execute("Flink Java API Skeleton");
	}
}
