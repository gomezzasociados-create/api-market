package com.gomezsystems.minierp.repository;

import com.gomezsystems.minierp.model.Ajuste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AjusteRepository extends JpaRepository<Ajuste, String> {
}
