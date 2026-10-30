# S10 - Health groups, correlacion y falla simulada en NextLook.
# Requisitos: infra + microservicios + obs levantados (ver docs/observabilidad.md).
# Uso:  pwsh scripts/simular-falla.ps1
#       pwsh scripts/simular-falla.ps1 -Servicio inventario-ms   (contenedor a detener)

param([string]$Servicio = "inventario-ms")

$ErrorActionPreference = "Stop"
$Gateway  = "http://localhost:18080"
$Keycloak = "http://localhost:18443"
$Puertos  = [ordered]@{ "catalogo-ms" = 8081; "inventario-ms" = 8082; "orden-ms" = 8084; "pago-ms" = 8085; "cliente-ms" = 8086 }

function Titulo($t) { Write-Host "`n=== $t ===" -ForegroundColor Cyan }
function Estado($url) {
    try { (Invoke-RestMethod -Uri $url -TimeoutSec 10).status }
    catch { $c = $_.Exception.Response.StatusCode.value__; if ($c) { "DOWN (HTTP $c)" } else { "SIN RESPUESTA" } }
}
function Get-Token($u, $p) {
    $b = @{ grant_type = "password"; client_id = "nextlook-api"; username = $u; password = $p }
    (Invoke-RestMethod -Method Post -Uri "$Keycloak/realms/nextlook/protocol/openid-connect/token" -Body $b).access_token
}
function Crear-Orden($token, $varianteId) {
    $traceId = "s10-" + [guid]::NewGuid().ToString().Substring(0, 8)
    $body = @{ metodoPago = "TARJETA"; items = @(@{ varianteId = $varianteId; cantidad = 1 }) } | ConvertTo-Json -Depth 4
    $orden = Invoke-RestMethod -Method Post -Uri "$Gateway/api/v1/ordenes" -ContentType "application/json" `
        -Headers @{ Authorization = "Bearer $token"; "X-Trace-ID" = $traceId } -Body $body
    [pscustomobject]@{ id = $orden.id; estado = $orden.estado; traceId = $traceId }
}
function Estado-Orden($token, $id) {
    (Invoke-RestMethod -Uri "$Gateway/api/v1/ordenes/$id" -Headers @{ Authorization = "Bearer $token" }).estado
}

Titulo "1. Health groups (liveness / readiness)"
foreach ($s in $Puertos.Keys) {
    $p = $Puertos[$s]
    "{0,-14} liveness={1,-6} readiness={2}" -f $s, (Estado "http://localhost:$p/actuator/health/liveness"), (Estado "http://localhost:$p/actuator/health/readiness")
}
"{0,-14} live={1,-10} ready={2}" -f "keycloak", (Estado "http://localhost:19000/health/live"), (Estado "http://localhost:19000/health/ready")

Titulo "2. Correlacion: una compra con X-Trace-ID propio"
$token = Get-Token "cliente@nextlook.pe" "cliente123"
$o1 = Crear-Orden $token 1
Write-Host "orden $($o1.id) -> $($o1.estado)   traceId=$($o1.traceId)"
Write-Host "En Grafana (panel 'Seguir una compra por traceId') pega: $($o1.traceId)" -ForegroundColor Yellow
Write-Host "Deben salir lineas de nextlook-gateway, orden-ms, cliente-ms, catalogo-ms, inventario-ms y pago-ms."

Titulo "3. Falla simulada: se detiene $Servicio"
docker stop $Servicio | Out-Null
$inicio = Get-Date
Write-Host "$(Get-Date -Format HH:mm:ss) $Servicio detenido. Espera ~15 s y mira el panel 'Servicios arriba'."
Start-Sleep -Seconds 5
$o2 = Crear-Orden $token 2
Start-Sleep -Seconds 10
Write-Host "orden $($o2.id) creada durante la falla -> estado ahora: $(Estado-Orden $token $o2.id)   traceId=$($o2.traceId)"
Write-Host "(con inventario-ms caido la orden se queda en PENDIENTE_STOCK: el evento espera en Kafka)"

Titulo "4. Recuperacion"
docker start $Servicio | Out-Null
Write-Host "$(Get-Date -Format HH:mm:ss) $Servicio levantado de nuevo; esperando que consuma el evento pendiente..."
for ($i = 0; $i -lt 90; $i++) {
    Start-Sleep -Seconds 2
    $e = Estado-Orden $token $o2.id
    if ($e -ne "PENDIENTE_STOCK") { break }
}
$segundos = [int]((Get-Date) - $inicio).TotalSeconds
Write-Host "orden $($o2.id) -> $e  (falla + recuperacion: $segundos s)" -ForegroundColor Green
Write-Host "En Grafana: {application=""$Servicio""} |= ""ordenId=$($o2.id)""  o el traceId $($o2.traceId)"
