package BookingHomeStay.BookingHomeStay.dto.sepay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SepayWebhookPayload {
    private Long id;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String code;
    private String content;
    private String transferType; // "in" hoặc "out"
    private BigDecimal transferAmount;
    private BigDecimal accumulated;
    private String subAccount;
    private String referenceCode;
    private String description;
}
