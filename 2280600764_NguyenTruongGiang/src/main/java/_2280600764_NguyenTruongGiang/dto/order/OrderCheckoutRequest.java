package _2280600764_NguyenTruongGiang.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderCheckoutRequest {

    @NotBlank(message = "shippingName is required")
    private String shippingName;

    private String shippingPhone;

    @NotBlank(message = "shippingStreet is required")
    private String shippingStreet;

    @NotBlank(message = "shippingCity is required")
    private String shippingCity;

    private String shippingPostalCode;
    private String note;
}
