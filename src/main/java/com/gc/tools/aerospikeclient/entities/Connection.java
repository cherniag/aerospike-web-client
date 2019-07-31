package com.gc.tools.aerospikeclient.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "connections")
@SequenceGenerator(name = "connections_seq_id", sequenceName = "connections_seq", allocationSize = 1)
public class Connection {
    @Id
    @GeneratedValue(generator = "connections_seq_id")
    long id;
    @Column
    String host;
    @Column
    int port;
    @Column
    String username;
    @Column
    String password;

    public Connection() {

    }

    public Connection(int port) {
        this.port = port;
    }
}
