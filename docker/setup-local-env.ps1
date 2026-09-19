[CmdletBinding()]
param()

$envPath = Join-Path $PSScriptRoot ".env"

if (Test-Path -LiteralPath $envPath) {
    throw "docker/.env already exists. Rotate the file and PostgreSQL roles together; this setup script will not overwrite it."
}

function New-StrongPassword {
    $bytes = [byte[]]::new(36)
    $generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $generator.GetBytes($bytes)
    }
    finally {
        $generator.Dispose()
    }
    return [Convert]::ToBase64String($bytes).TrimEnd("=").Replace("+", "-").Replace("/", "_")
}

$adminPassword = New-StrongPassword
$applicationPassword = New-StrongPassword
$contents = @"
POSTGRES_DB=ai_interview
POSTGRES_ADMIN_USER=ai_interview_admin
POSTGRES_ADMIN_PASSWORD=$adminPassword
DB_USERNAME=ai_interview_app
DB_PASSWORD=$applicationPassword
DB_URL=jdbc:postgresql://127.0.0.1:5432/ai_interview
"@

[System.IO.File]::WriteAllText(
    $envPath,
    $contents,
    [System.Text.UTF8Encoding]::new($false)
)

Write-Host "Created docker/.env with independent local database credentials."
