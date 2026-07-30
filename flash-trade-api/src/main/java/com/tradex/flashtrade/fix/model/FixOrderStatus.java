package com.tradex.flashtrade.fix.model;

/**
 * FIX 注文ステータス（OrdStatus Tag 39 に対応）
 */
public enum FixOrderStatus {

    NEW('0'),
    PARTIALLY_FILLED('1'),
    FILLED('2'),
    DONE_FOR_DAY('3'),
    CANCELED('4'),
    REPLACED('5'),
    PENDING_CANCEL('6'),
    STOPPED('7'),
    REJECTED('8'),
    SUSPENDED('9'),
    PENDING_NEW('A'),
    CALCULATED('B'),
    EXPIRED('C'),
    ACCEPTED_FOR_BIDDING('D'),
    PENDING_REPLACE('E'),
    UNKNOWN('?');

    private final char fixValue;

    FixOrderStatus(char fixValue) {
        this.fixValue = fixValue;
    }

    public char getFixValue() {
        return fixValue;
    }

    public static FixOrderStatus fromFixValue(char value) {
        for (FixOrderStatus status : values()) {
            if (status.fixValue == value) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
