package com.nextlook.pago.gateway;

import java.math.BigDecimal;

/** Puerto hacia la pasarela. Hoy hay una implementacion simulada; la real (Mercado Pago) se conecta aqui. */
public interface PasarelaPago {
    ResultadoPasarela cobrar(Long ordenId, BigDecimal monto, String metodoPago, String titular);
}
