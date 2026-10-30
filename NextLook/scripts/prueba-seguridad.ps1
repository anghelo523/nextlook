# U2 / Criterio 2 - Autenticacion y autorizacion en TODOS los microservicios, no solo en el Gateway.
# Llama a cada microservicio DIRECTO por su puerto (saltandose el Gateway):
#   sin token -> 401 · token CLIENTE en ruta de ADMIN -> 403 · token correcto -> 2xx (o 404 en los esqueletos)
# Uso: pwsh scripts/prueba-seguridad.ps1
$Keycloak = "http://localhost:18443/realms/nextlook/protocol/openid-connect/token"
function Token($u, $p) { (Invoke-RestMethod -Method Post -Uri $Keycloak -Body @{
    grant_type = "password"; client_id = "nextlook-api"; username = $u; password = $p }).access_token }
$cliente = Token "cliente@nextlook.pe" "cliente123"
$admin   = Token "admin@nextlook.pe" "admin123"
function Codigo($url, $token, $metodo = "Get") {
    $h = @{}; if ($token) { $h.Authorization = "Bearer $token" }
    (Invoke-WebRequest -Method $metodo -Uri $url -Headers $h -ContentType "application/json" -Body $(if ($metodo -eq "Get") { $null } else { "{}" }) -SkipHttpErrorCheck).StatusCode
}
$casos = @(
    @{ ms = "catalogo-ms";       url = "http://localhost:8081/api/v1/productos";      admin = $false },
    @{ ms = "inventario-ms";     url = "http://localhost:8082/api/v1/inventario";     admin = $true  },
    @{ ms = "resenas-ms (POST)"; url = "http://localhost:8083/api/v1/resenas";        admin = $false; metodo = "Post" },
    @{ ms = "orden-ms";          url = "http://localhost:8084/api/v1/ordenes";        admin = $true  },
    @{ ms = "pago-ms";           url = "http://localhost:8085/api/v1/pagos";          admin = $true  },
    @{ ms = "cliente-ms";        url = "http://localhost:8086/api/v1/clientes";       admin = $true  },
    @{ ms = "envio-ms";          url = "http://localhost:8087/api/v1/envios";         admin = $false },
    @{ ms = "notificaciones-ms"; url = "http://localhost:8088/api/v1/notificaciones"; admin = $true  }
)
"{0,-18} {1,-10} {2,-12} {3}" -f "servicio", "sin token", "CLIENTE", "ADMIN"
foreach ($c in $casos) {
    $m = if ($c.metodo) { $c.metodo } else { "Get" }
    "{0,-18} {1,-10} {2,-12} {3}" -f $c.ms, (Codigo $c.url $null $m), (Codigo $c.url $cliente $m), (Codigo $c.url $admin $m)
}
Write-Host "`ncatalogo GET es publico (200 sin token). Rutas de ADMIN: CLIENTE -> 403. Esqueletos (resenas, envio, notificaciones): 404 con token = paso la seguridad, aun no hay endpoint." -ForegroundColor DarkGray
