package enyo.io.tests;

import enyo.io.CSVReader;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.ThrowingRunnable;

import java.io.StringReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public final class TestCSVReader {
    @Test
    public static void emptyNextRow(Testing t) throws IOException {
        CSVReader r = wrap("");
        t.assertFalse(r.nextRow());
        t.assertFalse(r.nextRow());
        checkNoRowState(t, r);
        t.assertFalse(r.nextRow());
        checkNoRowState(t, r);
    }

    @Test
    public static void noNextRow(Testing t) throws IOException {
        CSVReader r = wrap("1\n");
        t.expect(r::lineNumber, IllegalStateException.class);
        t.expect(r::rowNumber, IllegalStateException.class);
        checkNoRowState(t, r);
    }

    @Test
    public static void noNextRowEmpty(Testing t) throws IOException {
        CSVReader r = wrap("");
        t.expect(r::lineNumber, IllegalStateException.class);
        t.expect(r::rowNumber, IllegalStateException.class);
        checkNoRowState(t, r);
    }

    @Test
    public static void emptyFetchRow(Testing t) throws IOException {
        CSVReader r = wrap("");
        t.examine(r.fetchRow()).is(List.of());
        checkNoRowState(t, r);
    }

    @Test
    public static void noColumnNextRow(Testing t) throws IOException {
        CSVReader r = wrap("\n");
        t.assertTrue(r.nextRow());
        t.examine(r.lineNumber()).is(1);
        t.examine(r.rowNumber()).is(0);
        t.assertFalse(r.nextRow());
        t.examine(r.rowNumber()).is(1);
        checkNoRowState(t, r);
    }

    @Test
    public static void noColumnMoreColumns(Testing t) throws IOException {
        CSVReader r = wrap("\n");
        t.assertTrue(r.nextRow());
        t.assertTrue(r.moreColumns());
        t.assertFalse(r.nextRow());
        t.expect(r::moreColumns, IllegalStateException.class);
        t.examine(r.rowNumber()).is(1);
        checkNoRowState(t, r);
    }

    @Test
    public static void noColumnFetchColumn1(Testing t) throws IOException {
        noColumnFetchColumn(t, false);
    }

    @Test
    public static void noColumnFetchColumn2(Testing t) throws IOException {
        noColumnFetchColumn(t, true);
    }

    private static void noColumnFetchColumn(Testing t, boolean extraFetch)
        throws IOException {
        CSVReader r = wrap("\n");
        t.assertTrue(r.nextRow());
        t.examine(r.fetchColumn()).is("");
        t.assertFalse(r.lastColumnWasQuoted());
        t.examine(r.lastColumnIndex()).is(0);
        t.assertFalse(r.moreColumns());
        if (extraFetch)
            expectEOL(t, r::fetchColumn);
        t.assertFalse(r.nextRow());
        t.examine(r.rowNumber()).is(1);
        checkNoRowState(t, r);
    }

    @Test
    public static void noColumnFetchNullableColumn(Testing t)
        throws IOException {
        CSVReader r = wrap("\n");
        t.assertTrue(r.nextRow());
        t.examine(r.lineNumber()).is(1);
        t.examine(r.rowNumber()).is(0);
        t.examine(r.fetchOptionalColumn()).isSame(Optional.empty());
        t.examine(r.lineNumber()).is(1);
        t.examine(r.rowNumber()).is(0);
        t.assertFalse(r.lastColumnWasQuoted());
        t.assertFalse(r.moreColumns());
        t.assertFalse(r.nextRow());
        t.examine(r.rowNumber()).is(1);
        checkNoRowState(t, r);
    }

    @Test
    public static void noColumnFetchRow(Testing t) throws IOException {
        CSVReader r = wrap("\n");
        t.examine(r.fetchRow()).is(List.of(""));
        t.examine(r.lineNumber()).is(1);
        t.examine(r.rowNumber()).is(0);
        t.examine(r.fetchRow()).is(List.of());
        t.examine(r.rowNumber()).is(1);
        checkNoRowState(t, r);
    }

    @Test
    public static void oneColumnFetchColumn(Testing t) throws IOException {
        for (int flags = 0; flags < 4; ++ flags)
            for (int length = 0; length <= 10; ++ length) {
                String contents = "X".repeat(length);
                oneColumnFetchColumn(t, contents,
                    (flags & 1) != 0, (flags & 2) != 0);
            }
    }

    private static void oneColumnFetchColumn(Testing t, String contents,
        boolean quoted, boolean extraFetch) throws IOException {
        CSVReader r;
        if (quoted)
            r = wrap("\"" + contents + "\"\n");
        else
            r = wrap(contents + "\n");
        t.assertTrue(r.nextRow());
        t.examine(r.fetchColumn()).is(contents);
        t.examine(r.lineNumber()).is(1);
        t.examine(r.rowNumber()).is(0);
        t.examine(r.lastColumnWasQuoted()).is(quoted);
        t.assertFalse(r.moreColumns());
        if (extraFetch)
            expectEOL(t, r::fetchColumn);
        t.assertFalse(r.nextRow());
        t.examine(r.rowNumber()).is(1);
        checkNoRowState(t, r);
    }

    @Test
    public static void nullableColumnVsEmpty(Testing t) throws IOException {
        CSVReader r = wrap(",\n\"\",\"\"\n,\"\"\n\"\",\n");
        t.assertTrue(r.nextRow());
        t.examine(r.lineNumber()).is(1);
        t.examine(r.rowNumber()).is(0);
        t.examine(r.lastColumnIndex()).is(-1);
        t.examine(r.fetchOptionalColumn()).isSame(Optional.empty());
        t.examine(r.lastColumnIndex()).is(0);
        t.examine(r.fetchOptionalColumn()).isSame(Optional.empty());
        t.examine(r.lastColumnIndex()).is(1);
        t.assertFalse(r.moreColumns());

        t.assertTrue(r.nextRow());
        t.examine(r.lineNumber()).is(2);
        t.examine(r.rowNumber()).is(1);
        t.examine(r.fetchOptionalColumn()).is(Optional.of(""));
        t.examine(r.fetchOptionalColumn()).is(Optional.of(""));
        t.assertFalse(r.moreColumns());

        t.assertTrue(r.nextRow());
        t.examine(r.lineNumber()).is(3);
        t.examine(r.rowNumber()).is(2);
        t.examine(r.fetchOptionalColumn()).isSame(Optional.empty());
        t.examine(r.fetchOptionalColumn()).is(Optional.of(""));
        t.assertFalse(r.moreColumns());

        t.assertTrue(r.nextRow());
        t.examine(r.lineNumber()).is(4);
        t.examine(r.rowNumber()).is(3);
        t.examine(r.fetchOptionalColumn()).is(Optional.of(""));
        t.examine(r.fetchOptionalColumn()).isSame(Optional.empty());
        t.assertFalse(r.moreColumns());

        t.assertFalse(r.nextRow());
        t.examine(r.rowNumber()).is(4);
        checkNoRowState(t, r);
    }

    @Test
    public static void quotedNewLine1(Testing t) throws IOException {
        CSVReader r = wrap("\"\n\"\n");
        t.examine(r.fetchRow()).is(List.of("\n"));
        t.examine(r.fetchRow()).is(List.of());
    }

    @Test
    public static void quotedNewLine2(Testing t) throws IOException {
        CSVReader r = wrap("\"\nX\",\"Y\n\",\"\nZ\n\"\n");
        t.examine(r.fetchRow()).is(List.of("\nX", "Y\n", "\nZ\n"));
        t.examine(r.fetchRow()).is(List.of());
    }

    @Test
    public static void quotedQuote1(Testing t) throws IOException {
        CSVReader r = wrap("\"\"\"\"\n");
        t.examine(r.fetchRow()).is(List.of("\""));
        t.examine(r.fetchRow()).is(List.of());
    }

    @Test
    public static void quotedQuote2(Testing t) throws IOException {
        CSVReader r = wrap("\"\"\"\",\n");
        t.examine(r.fetchRow()).is(List.of("\"", ""));
        t.examine(r.fetchRow()).is(List.of());
    }

    @Test
    public static void quotedQuote3(Testing t) throws IOException {
        CSVReader r = wrap("\"\"X,\n");
        t.assertTrue(r.nextRow());
        t.expect(r::fetchColumn, IOException.class,
            e -> t.examine(e.getMessage())
                .is("Invalid character after quote"));
    }

    @Test
    public static void testBOM1(Testing t) throws IOException {
        CSVReader r = wrap("\uFEFF,\uFEFF\n");
        t.examine(r.fetchRow()).is(List.of("\uFEFF", "\uFEFF"));
    }

    @Test
    public static void testBOM2(Testing t) throws IOException {
        CSVReader r = wrap("\uFEFF\uFEFF,\uFEFF\n");
        t.examine(r.fetchRow()).is(List.of("\uFEFF\uFEFF", "\uFEFF"));
    }

    @Test
    public static void testBOM3(Testing t) throws IOException {
        CSVReader r = wrap("\uFEFF,\uFEFF\n", stripBOMConfig());
        t.examine(r.fetchRow()).is(List.of("", "\uFEFF"));
    }

    @Test
    public static void testBOM4(Testing t) throws IOException {
        CSVReader r = wrap("\uFEFF\uFEFF,\uFEFF\n", stripBOMConfig());
        t.examine(r.fetchRow()).is(List.of("\uFEFF", "\uFEFF"));
    }

    @Test
    public static void testBOM5(Testing t) throws IOException {
        CSVReader r = wrap("X\uFEFF,\uFEFF\n", stripBOMConfig());
        t.examine(r.fetchRow()).is(List.of("X\uFEFF", "\uFEFF"));
    }

    @Test
    public static void testBOM6(Testing t) throws IOException {
        CSVReader r = wrap("X\uFEFF\uFEFF,\uFEFF\n", stripBOMConfig());
        t.examine(r.fetchRow()).is(List.of("X\uFEFF\uFEFF", "\uFEFF"));
    }

    @Test
    public static void timeSeriesBBEX3(Testing t) throws IOException {
        timeSeriesBBEX3(t, false);
    }

    @Test
    public static void timeSeriesBBEX3BOM(Testing t) throws IOException {
        timeSeriesBBEX3(t, true);
    }

    private static void timeSeriesBBEX3(Testing t, boolean withBOM)
        throws IOException {
        CSVReader r;
        if (withBOM)
            r = new CSVReader(new StringReader(
                    '\uFEFF' + DATA), stripBOMConfig());
        else
            r = wrap(DATA);
        long lineCount = DATA.chars()
            .filter(ch -> ch == '\n')
            .count();

        t.examine(r.fetchRow()).is(List.of(
            "", "BBEX3.D.USD.EUR.BB.AC.000",
            "BBEX3.D.USD.EUR.BB.AC.000_FLAGS"));
        t.examine(r.fetchRow()).is(List.of("",
                "Euro foreign exchange reference rate of the ECB / EUR 1 = USD ... / United States",
                ""));
        t.examine(r.fetchRow()).is(List.of("unit", "USD", ""));
        t.examine(r.fetchRow()).is(List.of("unit multiplier", "one", ""));
        t.examine(r.fetchRow()).is(List.of(
                "last update", "2019-06-14 15:56:50", ""));
        t.examine(r.fetchRow()).is(List.of(
                "1999-01-01", ".", "No value available"));
        t.examine(r.fetchRow()).is(List.of(
                "1999-01-02", ".", "No value available"));
        t.examine(r.fetchRow()).is(List.of(
                "1999-01-03", ".", "No value available"));
        t.examine(r.fetchRow()).is(List.of("1999-01-04", "1.1789", ""));
        t.examine(r.fetchRow()).is(List.of("1999-01-05", "1.1790", ""));
        t.examine(r.fetchRow()).is(List.of("1999-01-06", "1.1743", ""));
        t.examine(r.fetchRow()).is(List.of("1999-01-07", "1.1632", ""));
        t.examine(r.fetchRow()).is(List.of("1999-01-08", "1.1659", ""));
        t.examine(r.fetchRow()).is(List.of(
                "1999-01-09", ".", "No value available"));
        t.examine(r.fetchRow()).is(List.of(
                "1999-01-10", ".", "No value available"));
        t.examine(r.fetchRow()).is(List.of("1999-01-11", "1.1569", ""));
        t.examine(r.fetchRow()).is(List.of("1999-01-12", "1.1520", ""));
        t.examine(r.fetchRow()).is(List.of("2019-06-14", "1.1265", ""));
        t.examine(r.fetchRow()).is(List.of("",
                    "General: The ECB publishes daily euro foreign exchange reference rates, which are calculated on the basis of the concertation between central banks at 14.15.", ""));
        t.examine(r.fetchRow()).is(List.of(
            "", "Source: European Central Bank (ECB).", ""));

        t.examine(r.lineNumber()).is(lineCount);
        t.examine(r.rowNumber()).is(lineCount - 1);
        t.examine(r.fetchRow()).is(List.of());
        t.assertFalse(r.nextRow());
        t.examine(r.rowNumber()).is(lineCount);
    }

    private static CSVReader.Configuration  stripBOMConfig() {
        return new CSVReader.Configuration()
            .byteOrderMark(CSVReader.Configuration.ByteOrderMark
                .CONSUME_ONCE_AT_START);
    }

    private static CSVReader wrap(String input) throws IOException {
        return new CSVReader(new StringReader(input));
    }

    private static CSVReader wrap(String input, CSVReader.Configuration config)
        throws IOException {
        return new CSVReader(new StringReader(input), config);
    }

    private static void expectEOF(Testing t, ThrowingRunnable r) {
        t.expect(r, EOFException.class, e -> t.examine(e.getMessage()).is(
                "EOF when column was expected"));
    }

    private static void expectEOL(Testing t, ThrowingRunnable r) {
        t.expect(r, IOException.class, e -> t.examine(e.getMessage()).is(
                "End of line when column was expected"));
    }

    private static void checkNoRowState(Testing t, CSVReader r) {
        expectIllegalState(t, r::moreColumns);
        expectIllegalState(t, r::lastColumnWasQuoted);
        expectIllegalState(t, r::lastColumnIndex);
        expectIllegalState(t, r::lineNumber);
        expectIllegalState(t, r::fetchColumn);
        expectIllegalState(t, r::fetchOptionalColumn);
    }

    private static void expectIllegalState(Testing t, ThrowingRunnable r) {
        t.expect(r, IllegalStateException.class);
    }

    /**
     * BBEX3.D.USD.EUR.BB.AC.000 time series as published by Deutsche
     * Bundesbank.  English version (with comma as the separator).
     */
    static private final String DATA =
        "\"\",BBEX3.D.USD.EUR.BB.AC.000,BBEX3.D.USD.EUR.BB.AC.000_FLAGS\n"
        + "\"\",Euro foreign exchange reference rate of the ECB / EUR 1 = USD ... / United States,\n"
        + "unit,USD,\n"
        + "unit multiplier,one,\n"
        + "last update,2019-06-14 15:56:50,\n"
        + "1999-01-01,.,No value available\n"
        + "1999-01-02,.,No value available\n"
        + "1999-01-03,.,No value available\n"
        + "1999-01-04,1.1789,\n"
        + "1999-01-05,1.1790,\n"
        + "1999-01-06,1.1743,\n"
        + "1999-01-07,1.1632,\n"
        + "1999-01-08,1.1659,\n"
        + "1999-01-09,.,No value available\n"
        + "1999-01-10,.,No value available\n"
        + "1999-01-11,1.1569,\n"
        + "1999-01-12,1.1520,\n"
        + "2019-06-14,1.1265,\n"
        + "\"\",\"General: The ECB publishes daily euro foreign exchange reference rates, which are calculated on the basis of the concertation between central banks at 14.15.\",\n"
        + "\"\",Source: European Central Bank (ECB).,\n";
}
