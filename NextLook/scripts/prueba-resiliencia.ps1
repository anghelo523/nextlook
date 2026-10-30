# U2 / Criterio 1 - Timeout, reintentos y fallback ante una caida provocada (S06).
# Detiene catalogo-ms, crea una orden (orden-ms reintenta 3 veces y cae al fallback CARRITO),
# muestra el Circuit Breaker abierto y, al levantar catalogo-ms, confirma la orden desde el carrito.
# Uso: pwsh scripts/prueba-resiliencia.ps1
$Gateway  = "http://localhost:18080"
$Keycloak = "http://localhost:18443/realms/nextlook/protocol/openid-connect/token"
function Titulo($t) { Write-Host "`n=== $t ===" -ForegroundColor Cyan }
$token = (Invoke-RestMethod -Method Post -Uri $Keycloak -Body @{
    grant_type = "password"; client_id = "nextlook-api"; username = "cliente@nextlook.pe"; password = "cliente123" }).access_token
$h = @{ Authorization = "Bearer $token" }
$body = '{"metodoPago":"TARJETA","items":[{"varianteId":3,"cantidad":1}]}'
function Estado-CB { (Invoke-RestMethod "http://localhost:8084/actuator/circuitbreakers").circuitBreakers |
    ForEach-Object { $_.PSObject.Properties } | ForEach-Object { "{0,-9} {1}" -f $_.Name, $_.Value.state } }

Titulo "1. Estado inicial de los Circuit Breakers de orden-ms"
Estado-CB

Titulo "2. Caida provocada: docker stop catalogo-ms"
docker stop catalogo-ms | Out-Null
Start-Sleep -Seconds 3
$cronometro = [Diagnostics.Stopwatch]::StartNew()
$r = Invoke-WebRequest -Method Post -Uri "$Gateway/api/v1/ordenes" -Headers $h -ContentType "application/json" -Body $body -SkipHttpErrorCheck
$cronometro.Stop()
$orden = $r.Content | ConvertFrom-Json
"HTTP $($r.StatusCode) en $($cronometro.ElapsedMilliseconds) ms  -> orden $($orden.id) estado=$($orden.estado)"
"observacion: $($orden.observacion)"
Write-Host "(los ~1.2 s extra son los 3 intentos con backoff 400 ms -> 800 ms; ver log component=resiliencia)" -ForegroundColor DarkGray

Titulo "3. Mas pedidos mientras el catalogo sigue caido (el circuito se abre)"
1..2 | ForEach-Object {
    $c = [Diagnostics.Stopwatch]::StartNew()
    $x = Invoke-WebRequest -Method Post -Uri "$Gateway/api/v1/ordenes" -Headers $h -ContentType "application/json" -Body $body -SkipHttpErrorCheck
    $c.Stop(); "HTTP $($x.StatusCode) en $($c.ElapsedMilliseconds) ms -> $((($x.Content | ConvertFrom-Json)).estado)"
}
Estado-CB
"health orden-ms: $((Invoke-RestMethod 'http://localhost:8084/actuator/health').status)   readiness: $((Invoke-RestMethod 'http://localhost:8084/actuator/health/readiness').status)"

Titulo "4. Recuperacion: docker start catalogo-ms y confirmar la orden del carrito"
docker start catalogo-ms | Out-Null
for ($i = 0; $i -lt 40; $i++) {
    Start-Sleep -Seconds 3
    $c = Invoke-WebRequest -Method Post -Uri "$Gateway/api/v1/ordenes/$($orden.id)/confirmar" -Headers $h -SkipHttpErrorCheck
    $estado = ($c.Content | ConvertFrom-Json).estado
    if ($c.StatusCode -eq 201) { break }
}
"HTTP $($c.StatusCode) -> orden $($orden.id) estado=$estado"
Estado-CB
