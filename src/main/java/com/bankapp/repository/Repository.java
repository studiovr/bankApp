package com.bankapp.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    void save(T entity) throws SQLException;
    void update(T entity) throws SQLException;
    Optional<T> findById(ID id) throws SQLException;
    List<T> findAll() throws SQLException;
    int count() throws SQLException;
}