Get-Process java -ErrorAction SilentlyContinue | ForEach-Object {
  $proc = Get-CimInstance Win32_Process -Filter "ProcessId=$($_.Id)" -ErrorAction SilentlyContinue
  if ($proc) {
    $cmd = $proc.CommandLine
    # Kill if it's a PCMS service (not VS Code IDE)
    if ($cmd -notmatch "vscode" -and $cmd -notmatch "jdt.ls" -and $cmd -notmatch "language-server") {
      Write-Host "Killing PID=$($_.Id) CMD=$($cmd.Substring(0, [Math]::Min(80, $cmd.Length)))"
      Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
    } else {
      Write-Host "Keeping PID=$($_.Id) (IDE)"
    }
  }
}