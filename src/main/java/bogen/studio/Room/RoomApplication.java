package bogen.studio.Room;

import bogen.studio.Room.Controller.JobHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.util.TimeZone;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableMongoAuditing
@ComponentScan({"bogen.studio.Room.*"})
@EntityScan("bogen.studio.Room.*")
@Configuration
@EnableDiscoveryClient
public class RoomApplication {

    public static void main(String[] args) {

        TimeZone.setDefault(TimeZone.getTimeZone("Iran"));
        ApplicationContext applicationContext = SpringApplication.run(RoomApplication.class, args);
        JobHandler jobHandler = applicationContext.getBean(JobHandler.class);
        jobHandler.run();
    }

}
