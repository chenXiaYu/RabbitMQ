package utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitUtils {

    public static Connection getConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.15.141");
        factory.setVirtualHost("/itcast");
        factory.setUsername("itcast");
        factory.setPassword("itcast");
        factory.setPort(5672);
        return  factory.newConnection();
    }
}
