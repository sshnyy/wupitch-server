package com.server.wupitch.area;

import com.server.wupitch.configure.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long> {
    Optional<Area> findByAreaIdAndStatus(Long areaId, Status status);
}
