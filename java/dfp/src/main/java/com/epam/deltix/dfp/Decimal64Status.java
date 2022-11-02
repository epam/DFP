package com.epam.deltix.dfp;

import static com.epam.deltix.dfp.JavaImplParse.*;

public class Decimal64Status extends JavaImplParse.FloatingPointStatusFlag {
    public enum StatusValue {
        EXACT(BID_EXACT_STATUS),

        OVERFLOW(BID_OVERFLOW_EXCEPTION),

        UNDERFLOW(BID_UNDERFLOW_EXCEPTION),

        INEXACT(BID_INEXACT_EXCEPTION),

        INVALID_FORMAT(BID_INVALID_FORMAT);

        private final int value;

        StatusValue(final int value) {
            this.value = value;
        }

        /**
         * @param value The numeric code corresponding to the enumeration value.
         * @return The enumeration object corresponding to the enumeration value.
         */
        public static StatusValue valueOf(final int value) {
            for (final StatusValue e : StatusValue.values()) {
                if (e.getValue() == value) {
                    return e;
                }
            }
            throw new IllegalArgumentException("Can't convert value " + value + " to enum.");
        }

        /**
         * @return The numeric code corresponding to the enumeration value.
         */
        public int getValue() {
            return this.value;
        }
    }

    public boolean isExact() {
        return this.status == StatusValue.EXACT.value;
    }

    protected boolean isStatus(final StatusValue statusValue) {
        return (this.status & statusValue.value) == statusValue.value;
    }

    public boolean isInvalidFormat() {
        return isStatus(StatusValue.INVALID_FORMAT);
    }

    public boolean isInexact() {
        return isStatus(StatusValue.INEXACT);
    }

    public boolean isOverflow() {
        return isStatus(StatusValue.OVERFLOW);
    }

    public boolean isUnderflow() {
        return isStatus(StatusValue.UNDERFLOW);
    }

    protected long underlying = Decimal64Utils.NaN;

    public int getStatusCode() {
        return status;
    }

    public long getUnderlying() {
        return underlying;
    }

    public Decimal64 getValue() {
        return Decimal64.fromUnderlying(underlying);
    }

    @Override
    public String toString() {
        return "Decimal64Status{" +
            "status=" + getStatusCode() +
            ", value=" + getValue() +
            '}';
    }
}
