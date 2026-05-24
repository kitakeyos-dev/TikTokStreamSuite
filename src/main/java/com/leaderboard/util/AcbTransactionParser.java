package com.leaderboard.service;

import com.leaderboard.model.BankTransaction;
import com.leaderboard.model.BankTransaction.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses raw ACB API response into normalized {@link BankTransaction} objects.
 */
public class AcbTransactionParser {

    @SuppressWarnings("unchecked")
    public static List<BankTransaction> parse(Map<String, Object> raw, String prefix) {
        List<BankTransaction> result = new ArrayList<>();

        Object items = raw.getOrDefault("data", raw.getOrDefault("transactions", raw));
        if (!(items instanceof List<?>)) {
            return result;
        }

        for (Object obj : (List<?>) items) {
            if (!(obj instanceof Map)) continue;
            Map<String, Object> item = (Map<String, Object>) obj;

            double credit = toDouble(item, "creditAmount", "credit");
            double debit = toDouble(item, "debitAmount", "debit");
            double amount = toDouble(item, "amount", "transactionAmount");

            Type type = Type.UNKNOWN;
            double finalAmount = 0;

            if (credit > 0) {
                type = Type.IN;
                finalAmount = credit;
            } else if (debit > 0) {
                type = Type.OUT;
                finalAmount = debit;
            } else {
                String txType = getString(item, "transactionType", "type", "cdIndicator").toUpperCase();
                if (txType.matches("C|CR|CREDIT|IN|\\+")) {
                    type = Type.IN;
                    finalAmount = Math.abs(amount);
                } else if (txType.matches("D|DR|DEBIT|OUT|-")) {
                    type = Type.OUT;
                    finalAmount = Math.abs(amount);
                } else {
                    String desc = getString(item, "description", "memo").toLowerCase();
                    type = (desc.contains("nhan") || desc.contains("chuyen den") || desc.contains("from"))
                            ? Type.IN : Type.OUT;
                    finalAmount = Math.abs(amount);
                }
            }

            String message = getString(item, "description", "memo", "remark", "narrative");
            String time = getString(item, "transactionDate", "postingDate", "time", "valueDate");
            Double balance = getDoubleOrNull(item, "balance", "runningBalance");
            String reference = getString(item, "referenceNumber", "transactionId", "txnId");

            BankTransaction tx = new BankTransaction(type, finalAmount, message, time, balance, reference);

            if (type == Type.IN && prefix != null && !prefix.isEmpty()) {
                parseUserId(tx, prefix);
            }

            result.add(tx);
        }

        return result;
    }

    static void parseUserId(BankTransaction tx, String prefix) {
        String msg = tx.getMessage().toUpperCase().trim();
        String pfx = prefix.toUpperCase().trim();

        Pattern pattern = Pattern.compile(Pattern.quote(pfx) + "\\s*(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(msg);

        if (matcher.find()) {
            tx.setUserId(matcher.group(1));
            tx.setMatchedPrefix(true);
        }
    }

    private static double toDouble(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object v = map.get(key);
            if (v instanceof Number) return ((Number) v).doubleValue();
        }
        return 0;
    }

    private static Double getDoubleOrNull(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object v = map.get(key);
            if (v instanceof Number) return ((Number) v).doubleValue();
        }
        return null;
    }

    private static String getString(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object v = map.get(key);
            if (v instanceof String && !((String) v).isEmpty()) return (String) v;
        }
        return "";
    }
}
