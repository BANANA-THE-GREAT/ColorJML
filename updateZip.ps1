
$sourceDir = ".\build\distributions"
$targetDir = ".\"

# 首先删除根目录下所有以前版本的 zip
$compareFiles = Get-ChildItem -Path $sourceDir | Select-Object -ExpandProperty Name
Get-ChildItem -Path $targetDir | ForEach-Object {
    $file = $_
    if ($compareFiles -contains $file.Name) {
        Remove-Item -Path $file.FullName
        Write-Host "Deleted: $($file.FullName)"
    }
}

# 重新构建插件的 zip
.\gradlew buildPlugin
# 将新的 zip 复制到根目录
Copy-Item -Path $sourceDir\* -Destination $targetDir
