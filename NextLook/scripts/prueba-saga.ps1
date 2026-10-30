# Prueba end-to-end de la Saga de compra de NextLook (S06-S09).
# Requisitos: todo levantado con docker compose (ver docs/avance.md).
# Uso:  pwsh scripts/prueba-saga.ps1      (o powershell -File scripts\prueba-saga.ps1)

$ErrorActionPreference = "Stop"
$Gateway  = "http://localhost:18080"
$Keycloak = "http://localhost:18443"

function Get-Token($usuario, $clave) {
    $body = @{ grant_type = "password"; client_id = "nextlook-api"; username = $usuario; password = $clave }
    (Invoke-RestMethod -Method Post -Uri "$Keycloak/realms/nextlook/protocol/openid-connect/token" -Body $body).access_token
}

function Invoke-Api($metodo, $ruta, $token, $cuerpo = $null) {
    $headers = @{ "X-Trace-ID" = [guid]::NewGuid().ToString().Substring(0, 8) }
    if ($token) { $headers["Authorization"] = "Bearer $token" }
    $params = @{ Method = $metodo; Uri = "$Gateway$ruta"; Headers = $headers; ContentType = "application/json" }
    if ($cuerpo) { $params["Body"] = ($cuerpo | ConvertTo-Json -Depth 5) }
    try {
        return Invoke-RestMethod @params
    } catch {
        $codigo = $_.Exception.Response.StatusCode.value__
        return [pscustomobject]@{ httpStatus = $codigo }
    }
}

function Wait-Estado($ordenId, $esperado, $token, $segundos = 30) {
    for ($i = 0; $i -lt $segundos; $i++) {
        $orden = Invoke-Api GET "/api/v1/ordenes/$ordenId" $token
        if ($orden.estado -eq $esperado) { return $orden }
        Start-Sleep -Seconds 1
    }
    throw "La orden $ordenId no llego a $esperado (ultimo estado: $($orden.estado))"
}

function Wait-ListoParaPagar($ordenId, $token, $segundos = 30) {
    for ($i = 0; $i -lt $segundos; $i++) {
        $pago = Invoke-Api GET "/api/v1/pagos/orden/$ordenId" $token
        if ($pago.estado -eq "LISTO_PARA_PAGAR") { return $pago }
        Start-Sleep -Seconds 1
    }
    throw "El pago de la orden $ordenId no llego a LISTO_PARA_PAGAR"
}

function Titulo($texto) { Write-Host "`n=== $texto ===" -ForegroundColor Cyan }
function Ok($texto) { Write-Host "  OK  $texto" -ForegroundColor Green }

Titulo "Tokens de Keycloak"
$tCliente = Get-Token "cliente@nextlook.pe" "cliente123"
$tAdmin   = Get-Token "admin@nextlook.pe" "admin123"
Ok "token CLIENTE y ADMIN obtenidos"

Titulo "Seguridad (S07)"
$r = Invoke-Api GET "/api/v1/ordenes/mias" $null
Write-Host "  sin token -> HTTP $($r.httpStatus) (esperado 401)"
$r = Invoke-Api GET "/api/v1/inventario" $tCliente
Write-Host "  CLIENTE en /inventario -> HTTP $($r.httpStatus) (esperado 403)"
$r = Invoke-Api GET "/api/v1/productos" $null
Ok "catalogo publico: $($r.Count) prendas"

Titulo "Perfil y direccion del comprador"
$null = Invoke-Api POST "/api/v1/clientes" $tCliente @{ nombres = "Lucia"; apellidos = "Ramos"; dni = "71234567"; telefono = "987654321" }
$direcciones = Invoke-Api GET "/api/v1/clientes/me/direcciones" $tCliente
if (-not $direcciones -or $direcciones.Count -eq 0) {
    $null = Invoke-Api POST "/api/v1/clientes/me/direcciones" $tCliente @{
        alias = "Casa"; direccion = "Av. Arequipa 1234, dpto 502"; distrito = "Lince"; ciudad = "Lima"; referencia = "Frente al parque"; principal = $true }
}
$perfil = Invoke-Api GET "/api/v1/clientes/me" $tCliente
Ok "cliente $($perfil.id): $($perfil.nombres) $($perfil.apellidos) - $($perfil.direccionPrincipal.distrito)"

Titulo "Caso 1: compra pagada (titular APRO)"
$o1 = Invoke-Api POST "/api/v1/ordenes" $tCliente @{ metodoPago = "TARJETA"; items = @(@{ varianteId = 2; cantidad = 2 }, @{ varianteId = 7; cantidad = 1 }) }
Write-Host "  orden $($o1.id) creada en $($o1.estado), total S/ $($o1.total)"
$null = Wait-Estado $o1.id "PENDIENTE_PAGO" $tCliente
$null = Wait-ListoParaPagar $o1.id $tCliente
$p1 = Invoke-Api POST "/api/v1/pagos/procesar" $tCliente @{ ordenId = $o1.id; titular = "APRO" }
Write-Host "  pago $($p1.estado) idTransaccion=$($p1.idTransaccion)"
$null = Wait-Estado $o1.id "PAGADA" $tCliente
Ok "orden $($o1.id) PAGADA"
(Invoke-Api GET "/api/v1/inventario/reservas/$($o1.id)" $tAdmin) | Format-Table varianteId, cantidad, estado

Titulo "Caso 2: pago rechazado (titular FUND) -> compensacion"
$antes = (Invoke-Api GET "/api/v1/inventario/4" $tAdmin).disponible
$o2 = Invoke-Api POST "/api/v1/ordenes" $tCliente @{ metodoPago = "YAPE"; items = @(@{ varianteId = 4; cantidad = 3 }) }
$null = Wait-Estado $o2.id "PENDIENTE_PAGO" $tCliente
$null = Wait-ListoParaPagar $o2.id $tCliente
$p2 = Invoke-Api POST "/api/v1/pagos/procesar" $tCliente @{ ordenId = $o2.id; titular = "FUND" }
Write-Host "  pago $($p2.estado): $($p2.motivo)"
$c2 = Wait-Estado $o2.id "CANCELADA" $tCliente
Start-Sleep -Seconds 2
$despues = (Invoke-Api GET "/api/v1/inventario/4" $tAdmin).disponible
Ok "orden $($o2.id) CANCELADA ($($c2.observacion)); stock polo M negro: $antes -> $despues (debe ser igual)"

Titulo "Caso 3: sin stock (zapatillas 42 negro, stock 0)"
$o3 = Invoke-Api POST "/api/v1/ordenes" $tCliente @{ metodoPago = "PLIN"; items = @(@{ varianteId = 16; cantidad = 1 }) }
$c3 = Wait-Estado $o3.id "CANCELADA" $tCliente
Ok "orden $($o3.id) CANCELADA: $($c3.observacion)"

Titulo "Caso 4: el cliente desiste antes de pagar -> orden.cancelada"
$antes = (Invoke-Api GET "/api/v1/inventario/18" $tAdmin).disponible
$o4 = Invoke-Api POST "/api/v1/ordenes" $tCliente @{ metodoPago = "TARJETA"; items = @(@{ varianteId = 18; cantidad = 2 }) }
$null = Wait-Estado $o4.id "PENDIENTE_PAGO" $tCliente
$null = Wait-ListoParaPagar $o4.id $tCliente
$separado = (Invoke-Api GET "/api/v1/inventario/18" $tAdmin).disponible
$c4 = Invoke-Api POST "/api/v1/ordenes/$($o4.id)/cancelar" $tCliente
Write-Host "  orden $($o4.id) $($c4.estado): $($c4.observacion)"
Start-Sleep -Seconds 3
$despues = (Invoke-Api GET "/api/v1/inventario/18" $tAdmin).disponible
$p4 = Invoke-Api GET "/api/v1/pagos/orden/$($o4.id)" $tCliente
Ok "stock hoodie M: $antes -> $separado (separado) -> $despues (liberado); pago $($p4.estado)"

Titulo "Caso 5: el metodo se elige al pagar (orden creada con TARJETA, pagada con PLIN)"
$o5 = Invoke-Api POST "/api/v1/ordenes" $tCliente @{ metodoPago = "TARJETA"; items = @(@{ varianteId = 1; cantidad = 1 }) }
$null = Wait-Estado $o5.id "PENDIENTE_PAGO" $tCliente
$null = Wait-ListoParaPagar $o5.id $tCliente
$null = Invoke-Api POST "/api/v1/pagos/procesar" $tCliente @{ ordenId = $o5.id; titular = "APRO"; metodoPago = "PLIN" }
$c5 = Wait-Estado $o5.id "PAGADA" $tCliente
Ok "orden $($o5.id) PAGADA con $($c5.metodoPago)"

Titulo "Dueno de la orden (S07)"
$tOtro = Get-Token "cliente2@nextlook.pe" "cliente123"
$r = Invoke-Api GET "/api/v1/ordenes/$($o1.id)" $tOtro
Write-Host "  otro CLIENTE viendo la orden $($o1.id) -> HTTP $($r.httpStatus) (esperado 403)"

Write-Host "`nListo. Revisa los eventos en Kafka UI: http://localhost:18085" -ForegroundColor Yellow
