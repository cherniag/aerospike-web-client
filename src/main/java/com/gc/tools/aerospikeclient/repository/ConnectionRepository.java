package com.gc.tools.aerospikeclient.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.gc.tools.aerospikeclient.entities.Connection;

@Repository
public interface ConnectionRepository extends CrudRepository<Connection, Long> {

}
