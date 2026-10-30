# U2 / Criterio 4 - Reprocesar el MISMO evento no duplica el efecto (S09).
# Crea una orden, espera a que la Saga reserve stock, copia su orden.creada desde Kafka y lo
# reenvia 3 veces. Ni el stock, ni las reservas, ni los pagos deben cambiar.
# Uso: pwsh scripts/prueba-idempotencia.ps1
$Gateway  = "http://localhost:18080"
$Keycloak = "http://localhost:18443/realms/nextlook/protocol/openid-connect/token"
function Token($u, $p) { (Invoke-RestMethod -Method Post -Uri $Keycloak -Body @{
    grant_type = "password"; client_id = "nextlook-api"; username = $u; password = $p }).access_token }
$hc = @{ Authorization = "Bearer $(Token 'cliente@nextlook.pe' 'cliente123')" }
$ha = @{ Authorization = "Bearer $(Token 'admin@nextlook.pe' 'admin123')" }
$variante = 2
function Foto($ordenId) {
    $s = Invoke-RestMethod "$Gateway/api/v1/inventario/$variante" -Headers $ha
    $r = @(Invoke-RestMethod "$Gateway/api/v1/inventario/reservas/$ordenId" -Headers $ha)
    $p = @(Invoke-RestMethod "$Gateway/api/v1/pagos" -Headers $ha | Where-Object { $_.ordenId -eq $ordenId })
    "disponible={0} reservado={1} | reservas de la orden={2} | pagos de la orden={3}" -f $s.disponible, $s.reservado, $r.Count, $p.Count
}

$orden = Invoke-RestMethod -Method Post -Uri "$Gateway/api/v1/ordenes" -Headers $hc -ContentType "application/json" `
    -Body "{`"metodoPago`":`"YAPE`",`"items`":[{`"varianteId`":$variante,`"cantidad`":2}]}"
Write-Host "orden $($orden.id) creada ($($orden.estado)); esperando la Saga..."
for ($i = 0; $i -lt 20 -and (Invoke-RestMethod "$Gateway/api/v1/ordenes/$($orden.id)" -Headers $hc).estado -ne "PENDIENTE_PAGO"; $i++) { Start-Sleep 1 }
Start-Sleep 2
Write-Host "ANTES : $(Foto $orden.id)" -ForegroundColor Yellow

# El evento original, tal cual lo publico orden-ms (key|valor)
$linea = docker exec nextlook-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server nextlook-kafka:9092 `
    --topic orden-eventos --from-beginning --timeout-ms 8000 --property print.key=true --property key.separator="|" 2>$null |
    Where-Object { $_ -like "$($orden.id)|*" } | Select-Object -Last 1
Write-Host "evento: $linea"
1..3 | ForEach-Object { $linea } | docker exec -i nextlook-kafka /opt/kafka/bin/kafka-console-producer.sh `
    --bootstrap-server nextlook-kafka:9092 --topic orden-eventos --property parse.key=true --property key.separator="|"
Start-Sleep 5
Write-Host "DESPUES (3 reenvios): $(Foto $orden.id)" -ForegroundColor Green
Write-Host "En los logs de inventario-ms y pago-ms: ordenId=$($orden.id) status=ignored motivo=ya_procesada / ya_registrado"
