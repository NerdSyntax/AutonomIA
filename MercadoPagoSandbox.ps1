[CmdletBinding()]
param(
    [ValidateSet('Configurar', 'Verificar')]
    [string]$Accion = 'Configurar'
)

$ErrorActionPreference = 'Stop'
$api = 'https://api.mercadopago.com'
$sellerTestId = '3830737395'
$plusPlanId = '3be5f667a82943afb2413cffb9195bba'
$proReason = 'AutonomIA Pro - Prueba'
$resourceName = 'autonomia_mercado_pago_pro_test_plan'
$resourcePath = Join-Path (Get-Location).Path 'app/src/debug/res/values/autonomia_mercado_pago_sandbox.xml'
$paywallPaths = @(
    'app/src/main/java/com/nerdsyntax/juntalucas/feature/paywall/Paywallscreen.kt',
    'app/src/main/java/com/nerdsyntax/juntalucas/feature/paywall/ui/PaywallScreen.kt'
)

if (-not (Test-Path 'app/src/main/AndroidManifest.xml' -PathType Leaf)) {
    throw 'Ejecuta el script desde la carpeta raiz de AutonomIA.'
}
if ($Accion -eq 'Configurar') {
    $paywall = @($paywallPaths | Where-Object {
        (Test-Path -LiteralPath $_ -PathType Leaf) -and
        (Select-String -LiteralPath $_ -SimpleMatch $resourceName -Quiet)
    })
    if ($paywall.Count -eq 0) {
        throw 'Primero reemplaza PaywallScreen.kt por la version que conecta Plus y Pro.'
    }
}

$proPlanId = $null
if (Test-Path $resourcePath -PathType Leaf) {
    [xml]$config = Get-Content -LiteralPath $resourcePath -Raw
    $node = $config.SelectSingleNode("/resources/string[@name='$resourceName']")
    if (-not $node -or $node.InnerText -cnotmatch '^[a-f0-9]{32}$' -or
        $node.InnerText -eq $plusPlanId) {
        throw 'El identificador local de Pro no es valido.'
    }
    $proPlanId = $node.InnerText
}

if ($Accion -eq 'Verificar' -and -not $proPlanId) {
    throw 'Ejecuta primero la accion Configurar para conectar Pro.'
}

function Assert-TestPlan {
    param($Plan, [string]$Id, [int]$Price, [string]$ApplicationId)
    $expectedLink = "https://www.mercadopago.cl/subscriptions/checkout?preapproval_plan_id=$Id"
    if ($Id -cnotmatch '^[a-f0-9]{32}$' -or $Plan.id -cne $Id -or
        [string]$Plan.collector_id -ne $sellerTestId -or
        [string]$Plan.application_id -ne $ApplicationId -or
        $Plan.status -ne 'active' -or $Plan.init_point -ne $expectedLink -or
        $Plan.auto_recurring.currency_id -ne 'CLP' -or
        [decimal]$Plan.auto_recurring.transaction_amount -ne $Price -or
        $Plan.auto_recurring.frequency -ne 1 -or $Plan.auto_recurring.frequency_type -ne 'months') {
        throw 'Mercado Pago no devolvio el plan de prueba esperado. No se cambio la configuracion.'
    }
}

function Get-MpResults {
    param([string]$Url)
    $offset = 0
    do {
        $page = Invoke-RestMethod -Method Get -Uri "$Url&offset=$offset&limit=20" -Headers $headers -TimeoutSec 20
        if (-not $page.PSObject.Properties['results']) {
            throw 'Mercado Pago no devolvio resultados en el formato esperado.'
        }
        $rows = @($page.results | Where-Object { $null -ne $_ })
        $rows
        $offset += $rows.Count
    } while ($rows.Count -gt 0 -and $offset -lt $page.paging.total)
}

function Save-SandboxReceipt {
    param($Plan, $Subscription, $Invoice, [string]$PlanCode)

    $paymentId = [string]$Invoice.payment.id
    if ($Invoice.payment.status -ne 'approved' -or $paymentId -notmatch '^[0-9]{1,25}$' -or
        $Invoice.currency_id -ne 'CLP' -or
        [decimal]$Invoice.transaction_amount -ne [decimal]$Plan.auto_recurring.transaction_amount) {
        return
    }

    $dir = Join-Path $env:LOCALAPPDATA 'AutonomIA/MercadoPagoSandbox/Comprobantes'
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
    $target = Join-Path $dir "pago-$paymentId.json"
    if (Test-Path -LiteralPath $target -PathType Leaf) { return $target }

    $receipt = [ordered]@{
        entorno = 'prueba'
        proveedor = 'Mercado Pago'
        plan = $PlanCode
        planId = [string]$Plan.id
        suscripcionId = [string]$Subscription.id
        estadoSuscripcionAlVerificar = [string]$Subscription.status
        pagoId = $paymentId
        estadoPago = 'approved'
        monto = [decimal]$Invoice.transaction_amount
        moneda = 'CLP'
        verificadoUtc = [DateTimeOffset]::UtcNow.ToString('o')
    }
    $temp = Join-Path $dir ('.' + [guid]::NewGuid().ToString('N') + '.tmp')
    try {
        $receipt | ConvertTo-Json | Set-Content -LiteralPath $temp -Encoding UTF8
        try { [System.IO.File]::Move($temp, $target) }
        catch [System.IO.IOException] {
            if (-not (Test-Path -LiteralPath $target -PathType Leaf)) { throw }
        }
    } finally {
        Remove-Item -LiteralPath $temp -ErrorAction SilentlyContinue
    }
    return $target
}

$secret = Read-Host 'Access Token de Credenciales de prueba' -AsSecureString
try {
    $token = [System.Net.NetworkCredential]::new('', $secret).Password.Trim()
    $headers = @{ Authorization = "Bearer $token" }

    $seller = Invoke-RestMethod -Method Get -Uri 'https://api.mercadolibre.com/users/me' -Headers $headers -TimeoutSec 20
    if ([string]$seller.id -ne $sellerTestId) {
        throw 'El token no corresponde al vendedor de prueba de AutonomIA.'
    }

    $plus = Invoke-RestMethod -Method Get -Uri "$api/preapproval_plan/$plusPlanId" -Headers $headers -TimeoutSec 20
    $testApplicationId = [string]$plus.application_id
    if (-not $testApplicationId) { throw 'No se pudo identificar la aplicacion de prueba de Plus.' }
    Assert-TestPlan $plus $plusPlanId 4990 $testApplicationId

    if (-not $proPlanId) {
        $query = [uri]::EscapeDataString($proReason)
        $existing = @(Get-MpResults "$api/preapproval_plan/search?q=$query&status=active" |
            Where-Object { $_.reason -ceq $proReason })
        if ($existing.Count -gt 1) {
            throw 'Hay varios planes Pro de prueba con el mismo nombre. Revisa el panel antes de repetir.'
        }
        if ($existing.Count -eq 1) {
            $proPlanId = [string]$existing[0].id
        } else {
            $body = @{
                reason = $proReason
                auto_recurring = @{
                    frequency = 1
                    frequency_type = 'months'
                    transaction_amount = 12990
                    currency_id = 'CLP'
                }
                back_url = 'https://www.mercadopago.cl/'
            } | ConvertTo-Json -Depth 4

            $created = Invoke-RestMethod -Method Post -Uri "$api/preapproval_plan" -Headers $headers -ContentType 'application/json' -Body $body -TimeoutSec 20
            $proPlanId = [string]$created.id
        }
    }

    if ($proPlanId -cnotmatch '^[a-f0-9]{32}$' -or $proPlanId -eq $plusPlanId) {
        throw 'Mercado Pago no devolvio un identificador valido para Pro.'
    }
    $pro = Invoke-RestMethod -Method Get -Uri "$api/preapproval_plan/$proPlanId" -Headers $headers -TimeoutSec 20
    Assert-TestPlan $pro $proPlanId 12990 $testApplicationId

    if ($Accion -eq 'Configurar') {
        New-Item -ItemType Directory -Path (Split-Path -Parent $resourcePath) -Force | Out-Null
        @"
<resources>
    <string name="$resourceName" translatable="false">$proPlanId</string>
</resources>
"@ | Set-Content -LiteralPath $resourcePath -Encoding UTF8
        Write-Host 'Plus y Pro listos. Vuelve a ejecutar la app en modo debug.'
        return
    }

    $buyerId = (Read-Host 'User ID de Buyer Test User (Comprador de prueba)').Trim()
    if ($buyerId -notmatch '^[0-9]+$' -or $buyerId -eq $sellerTestId) {
        throw 'Ingresa el User ID numerico de la cuenta Comprador de prueba.'
    }

    $approvedCount = 0
    foreach ($plan in @($plus, $pro)) {
        $planCode = if ($plan.id -eq $plusPlanId) { 'PLUS' } else { 'PRO' }
        $subscriptions = @(Get-MpResults "$api/preapproval/search?preapproval_plan_id=$($plan.id)&payer_id=$buyerId" |
            Where-Object { $_.preapproval_plan_id -eq $plan.id -and [string]$_.payer_id -eq $buyerId -and
                [string]$_.collector_id -eq $sellerTestId })
        if ($subscriptions.Count -eq 0) {
            Write-Host "$($plan.reason): no se encontro una suscripcion de ese comprador."
            continue
        }
        foreach ($sub in $subscriptions) {
            if ($sub.id -notmatch '^[a-zA-Z0-9_-]+$') { throw 'Identificador de suscripcion invalido.' }
            Write-Host "$($plan.reason): suscripcion $($sub.id), estado $($sub.status)."
            $invoices = @(Get-MpResults "$api/authorized_payments/search?preapproval_id=$($sub.id)" |
                Where-Object { $_.preapproval_id -eq $sub.id })
            if ($invoices.Count -eq 0) {
                Write-Host '  Aun no hay un cobro registrado.'
            }
            foreach ($invoice in $invoices) {
                $state = if ($invoice.payment.status) { $invoice.payment.status } else { 'sin confirmar' }
                Write-Host "  Cobro: $state ($($invoice.currency_id) $($invoice.transaction_amount))."
                $path = Save-SandboxReceipt $plan $sub $invoice $planCode
                if ($path) {
                    $approvedCount++
                    Write-Host "  Comprobante de prueba: $path"
                } elseif ($state -eq 'approved') {
                    Write-Warning 'El cobro no coincide con el monto, la moneda o el identificador esperado. No se guardo.'
                }
            }
        }
    }
    if ($approvedCount -eq 0) {
        Write-Host 'No se confirmo ningun cobro de prueba. No se guardaron comprobantes.'
    }
} finally {
    $secret.Dispose()
    Remove-Variable -Name token, headers, secret -ErrorAction SilentlyContinue
}
