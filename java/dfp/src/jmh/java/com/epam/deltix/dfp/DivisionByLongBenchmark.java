package com.epam.deltix.dfp;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(time = 3, iterations = 1)
@Measurement(time = 3, iterations = 3)
@State(Scope.Thread)
public class DivisionByLongBenchmark {
    private static final Random RANDOM = new Random();
    private long value;

    @Setup
    public void setUp() {
        value = RANDOM.nextLong();
        while (value < 0)
            value = RANDOM.nextLong();
    }

    @Benchmark
    public void regularDivision(final Blackhole blackhole) {
        final long divisor = 1000;
        final long quotient = value / divisor;
        blackhole.consume(quotient);
        blackhole.consume(value - quotient * divisor);
    }

    @Benchmark
    public void divisionViaMultiplication(final Blackhole blackhole) {
        final int extra_digits = 3;
        final long QH;
        final long Q_low_0;
        final long Q_low_1;
        final long ALBL_0;
        final long ALBL_1;
        final long ALBH_0;
        final long ALBH_1;
        final long QM2_0;
        final long QM2_1;
        {
            final long CXH;
            final long CXL;
            final long CYH;
            final long CYL;
            final long PL;
            long PH;
            long PM;
            final long PM2;
            CXH = value >>> 32;
            CXL = (int) ((value));
            CYH = bid_reciprocals10_128[extra_digits][1] >>> 32;
            CYL = (int) bid_reciprocals10_128[extra_digits][1];
            PM = CXH * CYL;
            PH = CXH * CYH;
            PL = CXL * CYL;
            PM2 = CXL * CYH;
            PH += (PM >>> 32);
            PM = ((int) PM) + PM2 + (PL >> 32);
            ALBH_1 = PH + (PM >> 32);
            ALBH_0 = (PM << 32) + (int) PL;
        }
        {
            final long CXH;
            final long CXL;
            final long CYH;
            final long CYL;
            final long PL;
            long PH;
            long PM;
            final long PM2;
            CXH = value >>> 32;
            CXL = (int) value;
            CYH = bid_reciprocals10_128[extra_digits][0] >>> 32;
            CYL = (int) bid_reciprocals10_128[extra_digits][0];
            PM = CXH * CYL;
            PH = CXH * CYH;
            PL = CXL * CYL;
            PM2 = CXL * CYH;
            PH += (PM >>> 32);
            PM = ((int) PM) + PM2 + (PL >>> 32);
            ALBL_1 = PH + (PM >>> 32);
            ALBL_0 = (PM << 32) + (int) PL;
        }
        Q_low_0 = ALBL_0;
        {
            long R64H;
            R64H = ALBH_1;
            QM2_0 = ALBL_1 + ALBH_0;
            if (QM2_0 < ALBL_1)
                R64H++;
            QM2_1 = R64H;
        }
        Q_low_1 = QM2_0;
        QH = QM2_1;

        // Now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
        final int amount = bid_recip_scale[extra_digits];

        final long _C64 = QH >>> amount;

        //
        blackhole.consume(_C64);

        final int amount2 = 64 - amount;
        long remainder_h = 0;
        remainder_h--;
        remainder_h >>>= amount2;
        remainder_h = remainder_h & QH;

        blackhole.consume(remainder_h);
        blackhole.consume(Q_low_0);
        blackhole.consume(Q_low_1);
    }

    private static final int[] bid_recip_scale = {
        129 - 128,    // 1
        129 - 128,    // 1/10
        129 - 128,    // 1/10^2
        129 - 128,    // 1/10^3
        3,    // 131 - 128
        6,    // 134 - 128
        9,    // 137 - 128
        13,    // 141 - 128
        16,    // 144 - 128
        19,    // 147 - 128
        23,    // 151 - 128
        26,    // 154 - 128
        29,    // 157 - 128
        33,    // 161 - 128
        36,    // 164 - 128
        39,    // 167 - 128
        43,    // 171 - 128
        46,    // 174 - 128
        49,    // 177 - 128
        53,    // 181 - 128
        56,    // 184 - 128
        59,    // 187 - 128
        63,    // 191 - 128

        66,    // 194 - 128
        69,    // 197 - 128
        73,    // 201 - 128
        76,    // 204 - 128
        79,    // 207 - 128
        83,    // 211 - 128
        86,    // 214 - 128
        89,    // 217 - 128
        92,    // 220 - 128
        96,    // 224 - 128
        99,    // 227 - 128
        102,    // 230 - 128
        109,    // 237 - 128, 1/10^35
    };

    private static final long[][] bid_reciprocals10_128 = {
        {0L, 0L},                                      // 0 extra digits
        {0x3333333333333334L, 0x3333333333333333L},    // 1 extra digit
        {0x51eb851eb851eb86L, 0x051eb851eb851eb8L},    // 2 extra digits
        {0x3b645a1cac083127L, 0x0083126e978d4fdfL},    // 3 extra digits
        {0x4af4f0d844d013aaL, 0x00346dc5d6388659L},    //  10^(-4) * 2^131
        {0x08c3f3e0370cdc88L, 0x0029f16b11c6d1e1L},    //  10^(-5) * 2^134
        {0x6d698fe69270b06dL, 0x00218def416bdb1aL},    //  10^(-6) * 2^137
        {0xaf0f4ca41d811a47L, 0x0035afe535795e90L},    //  10^(-7) * 2^141
        {0xbf3f70834acdaea0L, 0x002af31dc4611873L},    //  10^(-8) * 2^144
        {0x65cc5a02a23e254dL, 0x00225c17d04dad29L},    //  10^(-9) * 2^147
        {0x6fad5cd10396a214L, 0x0036f9bfb3af7b75L},    // 10^(-10) * 2^151
        {0xbfbde3da69454e76L, 0x002bfaffc2f2c92aL},    // 10^(-11) * 2^154
        {0x32fe4fe1edd10b92L, 0x00232f33025bd422L},    // 10^(-12) * 2^157
        {0x84ca19697c81ac1cL, 0x00384b84d092ed03L},    // 10^(-13) * 2^161
        {0x03d4e1213067bce4L, 0x002d09370d425736L},    // 10^(-14) * 2^164
        {0x3643e74dc052fd83L, 0x0024075f3dceac2bL},    // 10^(-15) * 2^167
        {0x56d30baf9a1e626bL, 0x0039a5652fb11378L},    // 10^(-16) * 2^171
        {0x12426fbfae7eb522L, 0x002e1dea8c8da92dL},    // 10^(-17) * 2^174
        {0x41cebfcc8b9890e8L, 0x0024e4bba3a48757L},    // 10^(-18) * 2^177
        {0x694acc7a78f41b0dL, 0x003b07929f6da558L},    // 10^(-19) * 2^181
        {0xbaa23d2ec729af3eL, 0x002f394219248446L},    // 10^(-20) * 2^184
        {0xfbb4fdbf05baf298L, 0x0025c768141d369eL},    // 10^(-21) * 2^187
        {0x2c54c931a2c4b759L, 0x003c7240202ebdcbL},    // 10^(-22) * 2^191
        {0x89dd6dc14f03c5e1L, 0x00305b66802564a2L},    // 10^(-23) * 2^194
        {0xd4b1249aa59c9e4eL, 0x0026af8533511d4eL},    // 10^(-24) * 2^197
        {0x544ea0f76f60fd49L, 0x003de5a1ebb4fbb1L},    // 10^(-25) * 2^201
        {0x76a54d92bf80caa1L, 0x00318481895d9627L},    // 10^(-26) * 2^204
        {0x921dd7a89933d54eL, 0x00279d346de4781fL},    // 10^(-27) * 2^207
        {0x8362f2a75b862215L, 0x003f61ed7ca0c032L},    // 10^(-28) * 2^211
        {0xcf825bb91604e811L, 0x0032b4bdfd4d668eL},    // 10^(-29) * 2^214
        {0x0c684960de6a5341L, 0x00289097fdd7853fL},    // 10^(-30) * 2^217
        {0x3d203ab3e521dc34L, 0x002073accb12d0ffL},    // 10^(-31) * 2^220
        {0x2e99f7863b696053L, 0x0033ec47ab514e65L},    // 10^(-32) * 2^224
        {0x587b2c6b62bab376L, 0x002989d2ef743eb7L},    // 10^(-33) * 2^227
        {0xad2f56bc4efbc2c5L, 0x00213b0f25f69892L},    // 10^(-34) * 2^230
        {0x0f2abc9d8c9689d1L, 0x01a95a5b7f87a0efL},    // 35 extra digits
    };
}

