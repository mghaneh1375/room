package bogen.studio.Room;

import bogen.studio.Room.Controller.JobHandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import java.util.TimeZone;

@SpringBootApplication
//@OpenAPIDefinition(info = @Info(title = "Koochita Boom API", version = "2.0", description = "Koochita Information"))
@EnableMongoAuditing
public class RoomApplication {

    public static void main(String[] args) {

        TimeZone.setDefault(TimeZone.getTimeZone("Iran"));
        ApplicationContext applicationContext = SpringApplication.run(RoomApplication.class, args);
        JobHandler jobHandler = applicationContext.getBean(JobHandler.class);
        jobHandler.run();
    }

}
