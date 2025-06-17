package com.example.event_backend.repository;

import com.example.event_backend.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUserId(Long userId);
    
    @Query("SELECT DISTINCT e.category FROM Event e")
    List<String> findAllCategories();
    
    @Query("SELECT DISTINCT e.city FROM Event e")
    List<String> findDistinctCities();
    
    Page<Event> findByCategory(String category, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.city) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Event> searchEvents(String search, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.category = :category AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.city) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Event> searchEventsByCategoryAndSearch(String category, String search, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Event e")
    long countAllEvents();

    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.city) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Event> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrCityContainingIgnoreCase(
            String search, String search2, String search3, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.city = :city")
    Page<Event> findByCity(String city, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.city = :city AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Event> findByCityAndSearch(String city, String search, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.category = :category AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.city) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Event> findByCategoryAndSearch(String category, String search, Pageable pageable);
}