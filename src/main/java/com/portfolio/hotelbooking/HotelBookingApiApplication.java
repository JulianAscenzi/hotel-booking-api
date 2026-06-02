package com.portfolio.hotelbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class HotelBookingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelBookingApiApplication.class, args);
    }
}
