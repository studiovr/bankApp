package com.bankapp.repository;

import com.bankapp.utils.TransactionManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public abstract class AbstractRepository<T, ID> implements Repository<T, ID> {

    protected final TransactionManager transactionManager;

    public AbstractRepository(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void save(T entity) throws SQLException {
    }

    @Override
    public void update(T entity) throws SQLException {
    }

    @Override
    public Optional<T> findById(ID id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<T> findAll() throws SQLException {
        return List.of();
    }

    @Override
    public int count() throws SQLException {
        return 0;
    }
}