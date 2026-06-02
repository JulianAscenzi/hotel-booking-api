package com.portfolio.hotelbooking.repository;

import com.portfolio.hotelbooking.model.Room;
import com.portfolio.hotelbooking.model.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Page<Room> findByActiveTrue(Pageable pageable);

    Page<Room> findByActiveTrueAndType(RoomType type, Pageable pageable);

    List<Room> findByActiveTrue();

    Optional<Room> findByIdAndActiveTrue(Long id);

    boolean existsByNumber(String number);

    boolean existsByNumberAndIdNot(String number, Long id);
}
