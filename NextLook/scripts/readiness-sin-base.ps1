# S10 - Demuestra la diferencia liveness / readiness: se detiene SOLO la base de pago-ms.
# El proceso sigue vivo (liveness UP) pero ya no puede atender (readiness DOWN, HTTP 503).
function Estado($url) {
    try { (Invoke-RestMethod -Uri $url -TimeoutSec 10).status }
    catch { $c = $_.Exception.Response.StatusCode.value__; if ($c) { "DOWN (HTTP $c)" } else { "SIN RESPUESTA" } }
}
function Mostrar($t) {
    "{0}  {1,-22} liveness={2,-6} readiness={3}" -f (Get-Date -Format HH:mm:ss), $t,
        (Estado "http://localhost:8085/actuator/health/liveness"), (Estado "http://localhost:8085/actuator/health/readiness")
}
Mostrar "antes"
docker stop nextlook-postgres-pago | Out-Null
Start-Sleep -Seconds 8
Mostrar "postgres-pago detenido"
docker start nextlook-postgres-pago | Out-Null
Start-Sleep -Seconds 12
Mostrar "postgres-pago de vuelta"
