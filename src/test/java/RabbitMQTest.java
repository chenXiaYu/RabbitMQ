import com.rabbitmq.client.*;
import kotlin.reflect.KVariance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

public class RabbitMQTest {
    public static Connection connection;
    public static  String queue = "SimpleQueue";

    @Before
    public void  init() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.15.141");
        factory.setVirtualHost("/itcast");
        factory.setUsername("itcast");
        factory.setPassword("itcast");
        factory.setPort(5672);
        connection = factory.newConnection();
        if(connection!= null) System.out.println("connect successfully");
    }

    @After
    public  void destory(){
        if(connection != null ) {
            try {
                connection.close();
                System.out.println("close over");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //==========简单模式==========

    /**
     * 简单发送，无交换机模式
     * @throws IOException
     * @throws TimeoutException
     */
    @Test
    public void send() throws IOException, TimeoutException {
        Channel channel = connection.createChannel();
        channel.queueDeclare(queue,true,false,false,null);
        int i = 10;
        while (i>0) {
            channel.basicPublish("", queue, null, "发送一条信息".getBytes("utf-8"));
            i--;
        }
        channel.close();
    }

    /**
     * 简单接收
     */
    @Test
    public void reciver() throws IOException {
        Channel channel = connection.createChannel();
        channel.queueDeclare(queue,true,false,false,null);
        DefaultConsumer consumer = new DefaultConsumer(channel){
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("routekey: "+envelope.getRoutingKey());
                System.out.println("exchange:"+envelope.getExchange());
                System.out.println("deliverTag:"+envelope.getDeliveryTag());
                System.out.println("consumenrTag:"+consumerTag);
                System.out.println("message:"+new String(body));
            }
        };
        //自动确认
        channel.basicConsume(queue,true,consumer);
        //如果不等待马上退出，会出现信息消费了但是控制台没有打印出来
//        while (true){}
    }


    //==========================工作模式=========================
    public  static String workmode = "workModeQueue";
    @Test
    public void workModeSend() throws IOException, TimeoutException {
        Channel channel = connection.createChannel();
        channel.queueDeclare(workmode,true,false,false,null);
        for(int i = 0 ; i < 20 ; i++){
            String msg = "这是消息" + i;
            channel.basicPublish("exchange",workmode,null,msg.getBytes("utf-8"));
            System.out.println("已经发送信息" + i);
        }
        channel.close();
    }

    @Test
    public void workModeRecv(){
        //这里创建两个消费者即可，可以指定每次消费的信息
        //见 work mode 下的 consumer 和 consumer2
    }

    //===========订阅模式===============
    //===订阅之 fanout模式==============
    public static String fanoutExchange = "fanoutExchange";
    public static String fanoutQueue1 = "fanoutQueue1";
    public static String fanoutQueue2 = "fanoutQueue2";

    @Test
    public void fanoutSend() throws IOException, TimeoutException {
        Channel channel = connection.createChannel();
        //声明交换机
        channel.exchangeDeclare(fanoutExchange,BuiltinExchangeType.FANOUT);
        //声明队列
        channel.queueDeclare(fanoutQueue1,true,false,false,null);
        channel.queueDeclare(fanoutQueue2,true,false,false,null);
        //
        channel.queueBind(fanoutQueue1,fanoutExchange,"");
        channel.queueBind(fanoutQueue2,fanoutExchange,"");
        for(int i = 0 ; i < 20 ; i++){
            String msg = "这是消息" + i;
            channel.basicPublish(fanoutExchange,"",null,msg.getBytes("utf-8"));
            System.out.println("已经发送信息" + i);
        }
        channel.close();
    }

    //======rout模式
    public static String routeExchange = "routeExchange";
    public static String routeQueue1 = "routeQueue1";
    public static String routeQueue2 = "routeQueue2";

    @Test
    public void RoutSend() throws IOException, TimeoutException {
        Channel channel = connection.createChannel();
        //声明交换机  route模式为direct
        channel.exchangeDeclare(routeExchange,BuiltinExchangeType.DIRECT);
        //声明队列
        channel.queueDeclare(routeQueue1,true,false,false,null);
        channel.queueDeclare(routeQueue2,true,false,false,null);
        //route模式需要定义routKey
        channel.queueBind(routeQueue1,routeExchange,"insert");
        channel.queueBind(routeQueue2,routeExchange,"delete");

        for(int i = 0 ; i < 20 ; i++){
            String msg = "这是消息" + i;
            String routeKey = i <5 ? "insert" : "delete" ;
            System.out.println(routeKey);
            channel.basicPublish(routeExchange,routeKey,null,msg.getBytes("utf-8"));
            System.out.println("已经发送信息" + i);
        }
        channel.close();
    }

    //======topic模式
    public static String topicExchange = "topicExchange";
    public static String topicQueue1 = "topicQueue1";
    public static String topicQueue2 = "topicQueue2";

    @Test
    public void topicSend() throws IOException, TimeoutException {
        Channel channel = connection.createChannel();
        //声明交换机  route模式为topic
        channel.exchangeDeclare(topicExchange,BuiltinExchangeType.TOPIC);
        //声明队列
        channel.queueDeclare(topicQueue1,true,false,false,null);
        channel.queueDeclare(topicQueue2,true,false,false,null);
        // #代表匹配一个或者多个词，* 代表1个词
        channel.queueBind(topicQueue1,topicExchange,"insert.#");
        channel.queueBind(topicQueue2,topicExchange,"delete.*");
        String msg = "这是一条信息" ;
        channel.basicPublish(topicExchange,"insert.abc",null,msg.getBytes("utf-8"));
        channel.basicPublish(topicExchange,"insert.delete",null,msg.getBytes("utf-8"));
        channel.basicPublish(topicExchange,"delete.insert",null,msg.getBytes("utf-8"));
        channel.close();
    }
}
