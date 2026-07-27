package top.huanyu666.backend.common.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * 单据编号生成器
 * 格式：前缀 + yyyyMMdd + 4位流水号，如 SO20240315001
 */
public final class CodeGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private CodeGenerator() {}

    /**
     * 生成单据编号
     *
     * @param prefix          前缀，如 SO / PO / MO
     * @param maxCodeSupplier 查询当天最大编号的 Supplier，返回 null 表示当天尚无单据
     * @return 新编号
     */
    public static String generate(String prefix, Supplier<String> maxCodeSupplier) {
        String dateStr = LocalDate.now().format(DATE_FMT);
        String prefixWithDate = prefix + dateStr;

        String maxCode = maxCodeSupplier.get();
        int seq = 1;
        if (maxCode != null && maxCode.startsWith(prefixWithDate) && maxCode.length() == prefixWithDate.length() + 4) {
            try {
                seq = Integer.parseInt(maxCode.substring(maxCode.length() - 4)) + 1;
            } catch (NumberFormatException ignored) {
                // fall through to seq=1
            }
        }
        return prefixWithDate + String.format("%04d", seq);
    }
}
