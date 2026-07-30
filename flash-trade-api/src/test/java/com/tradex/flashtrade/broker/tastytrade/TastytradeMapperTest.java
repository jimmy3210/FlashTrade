package com.tradex.flashtrade.broker.tastytrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.tradex.flashtrade.broker.dto.PlaceOrderResult;
import com.tradex.flashtrade.broker.tastytrade.dto.TastytradeOrder;

class TastytradeMapperTest {

    private final TastytradeMapper mapper = new TastytradeMapper();

    @Test
    void toPlaceOrderResult_handlesFilledAndPartialAndRejected() {
        TastytradeOrder o1 = new TastytradeOrder();
        o1.setId("o-1"); o1.setStatus("filled");
        PlaceOrderResult r1 = mapper.toPlaceOrderResult(o1);
        assertTrue(r1.isSuccess());
        assertEquals("o-1", r1.getExternalOrderId());

        TastytradeOrder o2 = new TastytradeOrder();
        o2.setId("o-2"); o2.setStatus("partially_filled");
        PlaceOrderResult r2 = mapper.toPlaceOrderResult(o2);
        assertTrue(r2.isSuccess());

        TastytradeOrder o3 = new TastytradeOrder();
        o3.setId("o-3"); o3.setStatus("rejected");
        PlaceOrderResult r3 = mapper.toPlaceOrderResult(o3);
        assertFalse(r3.isSuccess());
        assertEquals("REJECTED", r3.getMessage());
    }
}
