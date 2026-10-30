package com.nextlook.pago.gateway;

public record ResultadoPasarela(boolean aprobado, String idTransaccion, String motivo) { }
