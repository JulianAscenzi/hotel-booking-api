package com.portfolio.hotelbooking.repository;

import com.portfolio.hotelbooking.model.Booking;
import com.portfolio.hotelbooking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findAllByOrderByCreatedAtDesc();

    @Query("""
            select count(b) > 0
            from Booking b
            where b.room.id = :roomId
              and b.status <> :cancelled
              and b.checkIn < :checkOut
              and b.checkOut > :checkIn
            """)
    boolean existsOverlappingBooking(@Param("roomId") Long roomId,
                                     @Param("checkIn") LocalDate checkIn,
                                     @Param("checkOut") LocalDate checkOut,
                                     @Param("cancelled") BookingStatus cancelled);

    @Query("""
            select count(b) > 0
            from Booking b
            where b.room.id = :roomId
              and b.status <> :cancelled
              and b.checkOut >= :today
            """)
    boolean existsFutureBookingForRoom(@Param("roomId") Long roomId,
                                       @Param("today") LocalDate today,
                                       @Param("cancelled") BookingStatus cancelled);
}
