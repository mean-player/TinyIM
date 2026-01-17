package com.example.demo.Component;



public class SnowflakeIdUtils {

    // ============================== Fields ==============================
    // 开始时间戳 (可自定义，比如项目上线日期，减少位数)
    private static final long START_STAMP = 1609459200000L; // 2021-01-01

    private static final long DATA_CENTER_BIT = 5;  // 数据中心位数
    private static final long MACHINE_BIT = 5;      // 机器标识位数
    private static final long SEQUENCE_BIT = 12;    // 序列号位数（每毫秒 4096）

    private static final long MAX_DATA_CENTER_NUM = ~(-1L << DATA_CENTER_BIT);
    private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    private static final long MACHINE_LEFT = SEQUENCE_BIT;
    private static final long DATA_CENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private static final long TIMESTAMP_LEFT = DATA_CENTER_LEFT + DATA_CENTER_BIT;

    private long dataCenterId;
    private long machineId;
    private long sequence = 0L;
    private long lastStamp = -1L;

    // ============================== Constructor ==============================
    public SnowflakeIdUtils(long dataCenterId, long machineId) {
        if (dataCenterId > MAX_DATA_CENTER_NUM || dataCenterId < 0) {
            throw new IllegalArgumentException("dataCenterId 超出范围");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId 超出范围");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    // ============================== Core Method ==============================
    public synchronized long nextId() {
        long currStamp = System.currentTimeMillis();
        if (currStamp < lastStamp) {
            throw new RuntimeException("系统时钟倒退，生成ID失败");
        }

        if (currStamp == lastStamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currStamp = getNextMill();
            }
        } else {
            sequence = 0L;
        }

        lastStamp = currStamp;

        return (currStamp - START_STAMP) << TIMESTAMP_LEFT
                | (dataCenterId << DATA_CENTER_LEFT)
                | (machineId << MACHINE_LEFT)
                | sequence;
    }

    private long getNextMill() {
        long mill = System.currentTimeMillis();
        while (mill <= lastStamp) {
            mill = System.currentTimeMillis();
        }
        return mill;
    }
}