package com.portfolio.hotelbooking.service;

import com.portfolio.hotelbooking.dto.BookingRequest;
import com.portfolio.hotelbooking.dto.BookingResponse;

import java.util.List;

public interface BookingService {

    List<BookingResponse> getBookings();

    BookingResponse getBooking(Long id);

    BookingResponse createBooking(BookingRequest request);

    BookingResponse cancelBooking(Long id);
}
