# S11 - Crea la SPA Angular de NextLook y le copia el codigo de nextlook-web-src/.
# Se genera con la CLI (en vez de versionar package.json a mano) para que las versiones
# de Angular, TypeScript y el builder sean exactamente las que trae Angular CLI 22.
# Uso (desde la carpeta frontend/):   pwsh ./crear-frontend.ps1   y luego:  cd nextlook-web; npm start
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
if (-not (Test-Path "nextlook-web")) {
    npx -y @angular/cli@22 new nextlook-web --routing --style=css --ssr=false --skip-git --skip-tests --defaults
    # npx es un ejecutable externo: $ErrorActionPreference no lo detiene si falla (p. ej. Node viejo)
    if ($LASTEXITCODE -ne 0) {
        Remove-Item "nextlook-web" -Recurse -Force -ErrorAction SilentlyContinue
        throw "ng new fallo (Angular CLI 22 pide Node >= 24.15). Corrige el error y vuelve a correr el script."
    }
}
Copy-Item -Path "nextlook-web-src/src/*" -Destination "nextlook-web/src" -Recurse -Force
# imagenes editoriales de la portada (public/ se sirve tal cual en la raiz del sitio)
Copy-Item -Path "nextlook-web-src/public/*" -Destination "nextlook-web/public" -Recurse -Force
Write-Host "Listo. Ahora:  cd nextlook-web ; npm start   ->  http://localhost:4200" -ForegroundColor Green
