Get-Process java -ErrorAction SilentlyContinue | ForEach-Object {
  $proc = Get-CimInstance Win32_Process -Filter "ProcessId=$($_.Id)" -ErrorAction SilentlyContinue
  $cmd = if ($proc) { $proc.CommandLine } else { "n/a" }
  Write-Host "PID=$($_.Id) CMD=$cmd"
}