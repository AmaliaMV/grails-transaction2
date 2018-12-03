package com.example;

import java.io.Serializable;

import org.grails.datastore.gorm.neo4j.IdGenerator;

public class SnowflakeIdGenerator implements IdGenerator {

    private final long datacenterIdBits = 10L;
    private final long sequenceBits = 12L;

    private final long timestampLeftShift = sequenceBits + datacenterIdBits;
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private final long twepoch = 1288834974657L;

    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    @Override
    public Serializable nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            try {
                Thread.sleep((lastTimestamp - timestamp));
            }
            catch (InterruptedException e) {
            }
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - twepoch) << timestampLeftShift) | sequence;

        System.out.println("---> Id generado: " + id + "timestap " );
        return id;
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}