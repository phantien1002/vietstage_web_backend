$body = @{
    email = "test@example.com"
    password = "password"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:9191/api/auth/login" -Method Post -ContentType "application/json" -Body $body
    Write-Host "Success:"
    Write-Host ($response | ConvertTo-Json -Depth 5)
} catch {
    Write-Host "Error:"
    $result = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($result)
    $responseBody = $reader.ReadToEnd()
    Write-Host $responseBody
}
