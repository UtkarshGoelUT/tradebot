import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class test_jackson {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BigDecimal qty = BigDecimal.valueOf(100.0)
                .setScale(8, RoundingMode.HALF_DOWN)
                .stripTrailingZeros();

        Map<String, Object> map = new HashMap<>();
        map.put("qty", qty);

        System.out.println(mapper.writeValueAsString(map));
    }
}
