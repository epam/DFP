package com.epam.deltix.dfp;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class FormattingAndParsingTest {
    private void checkToString(final String message, final String hex, final String expectedPlain, final String expectedScientific) {
        @Decimal final long dfp64 = UnsignedLong.parse(hex, 16);
        final String actual = Decimal64Utils.toString(dfp64);

        final String info = (message != null ? message : "") + " Hex = " + hex + ", Value = " + expectedScientific;
        assertEquals(info, expectedPlain, actual);

        @Decimal final long result1 = Decimal64Utils.parse(expectedPlain);
        assertEquals(info, expectedPlain, Decimal64Utils.toString(result1));

        @Decimal final long result2 = Decimal64Utils.parse(expectedScientific);
        assertEquals(info, expectedPlain, Decimal64Utils.toString(result2));

        @Decimal final long result3 = Decimal64Utils.tryParse(expectedPlain, Decimal64Utils.NaN);
        assertEquals(info, expectedPlain, Decimal64Utils.toString(result3));

        @Decimal final long result4 = Decimal64Utils.tryParse(expectedScientific, Decimal64Utils.NaN);
        assertEquals(info, expectedPlain, Decimal64Utils.toString(result4));
    }

    @Test
    public void parse() throws IOException {
        final File file = new File(System.getProperty("com.epam.deltix.dfp.dataFiles.parsingAndFormatting"));
        final FileReader fileReader = new FileReader(file);
        final BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line;
        long number = 0;
        while ((line = bufferedReader.readLine()) != null) {
            number += 1;

            final String[] parts = line.split(",");
            switch (parts[1]) {
                case "+Inf":
                    checkToString("Line #" + number, parts[0], "Infinity", parts[1]);
                    break;

                case "-Inf":
                    checkToString("Line #" + number, parts[0], "-Infinity", parts[1]);
                    break;

                case "+NaN":
                case "-NaN":
                case "+SNaN":
                case "-SNaN":
                    checkToString("Line #" + number, parts[0], "NaN", parts[1]);
                    break;

                default:
                    checkToString("Line #" + number, parts[0], new BigDecimal(parts[1]).stripTrailingZeros().toPlainString(), parts[1]);
                    break;
            }
        }
    }

    @Test
    public void tryParseInvalidString() {
        final @Decimal long value = Decimal64Utils.tryParse("INVALID", Decimal64Utils.NaN);
        Assert.assertTrue(Decimal64Utils.isNaN(value));
    }

}
