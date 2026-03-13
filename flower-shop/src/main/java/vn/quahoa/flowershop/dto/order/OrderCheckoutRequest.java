package vn.quahoa.flowershop.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderCheckoutRequest {

    @NotBlank(message = "shippingName is required")
    private String shippingName;

    private String shippingPhone;
    private String shippingStreet;
    private String shippingCity;
    private String shippingPostalCode;
    private String note;
}
