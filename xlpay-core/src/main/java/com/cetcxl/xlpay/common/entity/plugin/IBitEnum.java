package com.cetcxl.xlpay.common.entity.plugin;

public interface IBitEnum {
    int getBitPos();

    default boolean isOpen(Integer bitInt) {
        return ((1 << (getBitPos() - 1)) & bitInt) > 0;
    }

    default boolean isClose(Integer bitInt) {
        return !isOpen(bitInt);
    }

    default Integer open(Integer bitInt) {
        return (1 << (getBitPos() - 1)) | bitInt;
    }

    default Integer close(Integer bitInt) {
        return ((1 << (getBitPos() - 1)) ^ Integer.MAX_VALUE) & bitInt;
    }
}
