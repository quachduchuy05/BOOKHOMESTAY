package BookingHomeStay.BookingHomeStay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookingHomeStayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingHomeStayApplication.class, args);
    }

}