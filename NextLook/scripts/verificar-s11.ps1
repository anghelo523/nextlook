# S11 - Verifica, sin navegador, las decisiones de seguridad del cliente web.
#   1) nextlook-web NO acepta password grant (solo Authorization Code + PKCE)
#   2) nextlook-web exige PKCE: sin code_challenge, Keycloak rechaza la peticion
#   3) el token trae sub, roles y el claim propio talla_preferida (protocol mapper)
#   4) CORS: el Gateway responde el preflight de http://localhost:4200 y no el de otro origen
# Uso: pwsh scripts/verificar-s11.ps1
$Keycloak = "http://localhost:18443/realms/nextlook/protocol/openid-connect"
$Gateway  = "http://localhost:18080"
function Titulo($t) { Write-Host "`n=== $t ===" -ForegroundColor Cyan }

Titulo "1. Password grant contra nextlook-web (debe fallar)"
$r = Invoke-WebRequest -Method Post -Uri "$Keycloak/token" -SkipHttpErrorCheck -Body @{
    grant_type = "password"; client_id = "nextlook-web"; username = "cliente@nextlook.pe"; password = "cliente123" }
"HTTP $($r.StatusCode)  $($r.Content)"

Titulo "2. Pedir un code SIN code_challenge (debe rechazarse por PKCE obligatorio)"
$q = "client_id=nextlook-web&response_type=code&scope=openid&redirect_uri=http%3A%2F%2Flocalhost%3A4200%2Fauth%2Fcallback&state=prueba"
$r = Invoke-WebRequest -Uri "$Keycloak/auth?$q" -MaximumRedirection 0 -SkipHttpErrorCheck -ErrorAction SilentlyContinue
"HTTP $($r.StatusCode)  Location: $($r.Headers.Location)"

Titulo "3. Claims del token (cliente de pruebas nextlook-api, mismo mapper)"
$t = (Invoke-RestMethod -Method Post -Uri "$Keycloak/token" -Body @{
    grant_type = "password"; client_id = "nextlook-api"; username = "cliente@nextlook.pe"; password = "cliente123" }).access_token
$p = $t.Split(".")[1].Replace("-", "+").Replace("_", "/"); $p += "=" * ((4 - $p.Length % 4) % 4)
$claims = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($p)) | ConvertFrom-Json
[pscustomobject]@{ sub = $claims.sub; azp = $claims.azp; roles = ($claims.realm_access.roles -join ","); talla_preferida = $claims.talla_preferida } | Format-List

Titulo "4. Preflight CORS en el Gateway"
foreach ($origen in "http://localhost:4200", "http://sitio-ajeno.example") {
    $r = Invoke-WebRequest -Method Options -Uri "$Gateway/api/v1/ordenes" -SkipHttpErrorCheck -Headers @{
        Origin = $origen; "Access-Control-Request-Method" = "POST"; "Access-Control-Request-Headers" = "authorization,content-type,x-trace-id" }
    "{0,-28} HTTP {1}  Allow-Origin={2}  Expose={3}" -f $origen, $r.StatusCode, $r.Headers["Access-Control-Allow-Origin"], $r.Headers["Access-Control-Expose-Headers"]
}
