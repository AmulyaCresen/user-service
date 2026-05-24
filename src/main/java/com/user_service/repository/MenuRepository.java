package com.user_service.repository;
import com.user_service.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface MenuRepository extends JpaRepository<Menu, Long> {
    @Query(value = "SELECT m.* FROM code.menu m " +
           "JOIN code.role r ON m.role_id = r.id " +
           "WHERE UPPER(r.role_name) = UPPER(:roleName) AND m.active = true " +
           "ORDER BY m.menu_order", nativeQuery = true)
    List<Menu> findByRoleName(@Param("roleName") String roleName);
}