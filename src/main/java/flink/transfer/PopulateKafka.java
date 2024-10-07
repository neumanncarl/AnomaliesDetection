package org.example.transfer;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.example.pojo.EquipmentEvent;
import org.example.pojo.EquipmentEventOld;
import org.example.pojo.JobRecord;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

public class PopulateKafka {

    private String sqlConnectionString = "jdbc:postgresql://131.159.52.23:5432/sicm3s_user?user=sicm3s_user&password=sicm3s_2024";
//    private String kafkaServer = "172.24.33.55:9094";
    private String kafkaServer = "localhost:9092";
    private ObjectMapper objectMapper = new ObjectMapper();


    public static void main (String[] args) throws SQLException, JsonProcessingException {

        // TODO: Benchmark


        Instant start = Instant.now();

        PopulateKafka populateKafka = new PopulateKafka();

        try (KafkaProducer<byte[], byte[]> kafkaProducer = new KafkaProducer<byte[], byte[]>(getKafkaProps(populateKafka.kafkaServer));
        Connection connection = DriverManager.getConnection(populateKafka.sqlConnectionString)) {
            System.out.println("Populating JobRecords");
            Statement jrstatement = connection.createStatement();
            populateKafka.startJobRecordTransfer(jrstatement, kafkaProducer);

            System.out.println("Populating EquipmentEvent");
            Statement equipmentEvents = connection.createStatement();
//            populateKafka.startEquipmentEventTransfer(equipmentEvents, kafkaProducer);
        }

        Instant end = Instant.now();

        Duration transferTime =  Duration.between(start, end);
        System.out.println(String.format("Transferred data in %s seconds", transferTime.getSeconds()));

    }
//
//    private void startEquipmentEventTransfer(Statement stmnt, KafkaProducer<byte[], byte[]> producer) throws SQLException, JsonProcessingException {
//        int count = 0;
//        ResultSet rs = stmnt.executeQuery("select * from events.equipmentevent ORDER BY id ASC ");
//        while(rs.next()) {
//            EquipmentEvent equipmentEvent = new EquipmentEventOld(
//                    rs.getInt(1),
//                    rs.getTimestamp(2),
//                    rs.getTimestamp(3),
//                    rs.getString(4),
//                    rs.getString(5),
//                    rs.getString(6),
//                    rs.getString(7),
//                    rs.getString(8),
//                    rs.getString(9),
//                    rs.getString(10),
//                    rs.getString(11),
//                    rs.getInt(12)
//            );
//            count++;
//
//            ProducerRecord<byte[], byte[]> record = new ProducerRecord<>(
//                    "equipmentevent",
//                    objectMapper.writeValueAsBytes(equipmentEvent.getId()),
//                    objectMapper.writeValueAsBytes(equipmentEvent)
//            );
//
//            producer.send(record);
//            if ((count % 100) == 0){
//                System.out.println(count);
//            }
//        }
//    }
    private void startJobRecordTransfer(Statement stmnt, KafkaProducer<byte[], byte[]> producer) throws SQLException, JsonProcessingException {

            int count = 0;
            ResultSet rs = stmnt.executeQuery("select * from events.jobrecord ORDER BY id ASC ");
            while(rs.next()){
                JobRecord jr = new JobRecord(
                        rs.getInt(1),
                        rs.getInt(2),
                        rs.getTimestamp(3),
                        rs.getString(4),
                        rs.getTimestamp(5),
                        rs.getInt(6),
                        rs.getInt(7),
                        rs.getInt(8),
                        rs.getInt(9)
                );
                count++;
                ProducerRecord<byte[], byte[]> kafkaRecord = new ProducerRecord<>("jobrecord",
                        objectMapper.writeValueAsBytes(jr.getId()),
                        objectMapper.writeValueAsBytes(jr));

                producer.send(kafkaRecord);

                if ((count / 100) == 0){
                    System.out.println(count);
                }
            }



    }
    
    
        protected static Properties getKafkaProps(String bootstrap){
        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
//        props.put(ProducerConfig.BATCH_SIZE_CONFIG, (int)100);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 100);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "populater");
//        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "datagen");


        return props;
    }
}
