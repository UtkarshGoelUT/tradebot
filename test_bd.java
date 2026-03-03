import java.math.BigDecimal;
import java.math.RoundingMode;

public class test_bd {
    public static void main(String[] args) {
        BigDecimal qty = BigDecimal.valueOf(100.0)
                .setScale(8, RoundingMode.HALF_DOWN)
                .stripTrailingZeros();
        System.out.println("qty: " + qty);
        System.out.println("qty.toPlainString(): " + qty.toPlainString());
    }
}
