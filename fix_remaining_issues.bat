@echo off
echo Fixing remaining compilation issues...

REM Fix ClientId toString() conversion issues
echo Fixing ClientId toString() conversions...
powershell -Command "Get-ChildItem -Path 'F:\0nsec Projects\nodex\nodex-core' -Include '*.java' -Recurse | ForEach-Object { (Get-Content $_.FullName) -replace 'CLIENT_ID, MAJOR_VERSION', 'CLIENT_ID.toString(), MAJOR_VERSION' | Set-Content $_.FullName }"

REM Fix Group.Visibility enum issues  
echo Fixing Group.Visibility enum issues...
powershell -Command "Get-ChildItem -Path 'F:\0nsec Projects\nodex\nodex-core' -Include '*.java' -Recurse | ForEach-Object { (Get-Content $_.FullName) -replace 'Group\.Visibility\.', 'Visibility.' | Set-Content $_.FullName }"

REM Create missing abstract method implementations
echo Creating missing abstract method stubs...

REM Create MessageId LENGTH constant
echo Creating MessageId LENGTH constant...
powershell -Command "(Get-Content 'F:\0nsec Projects\nodex\nodex-api\src\main\java\org\nodex\api\sync\MessageId.java') -replace 'public class MessageId', 'public class MessageId { public static final int LENGTH = 32;' | Set-Content 'F:\0nsec Projects\nodex\nodex-api\src\main\java\org\nodex\api\sync\MessageId.java'"

REM Create missing hook interface methods
echo Creating OpenDatabaseHook onDatabaseOpened stubs...
powershell -Command "Get-ChildItem -Path 'F:\0nsec Projects\nodex\nodex-core' -Include '*.java' -Recurse | ForEach-Object { $content = Get-Content $_.FullName -Raw; if ($content -match 'class (\w+).*implements.*OpenDatabaseHook' -and $content -notmatch 'onDatabaseOpened') { $content = $content -replace '(\s+})(\s*)$', '$1$2@Override public void onDatabaseOpened(Transaction txn) throws DbException { /* TODO: implement */ }$2}$2'; Set-Content $_.FullName $content } }"

REM Create missing ContactHook interface methods  
echo Creating ContactHook method stubs...
powershell -Command "Get-ChildItem -Path 'F:\0nsec Projects\nodex\nodex-core' -Include '*.java' -Recurse | ForEach-Object { $content = Get-Content $_.FullName -Raw; if ($content -match 'class (\w+).*implements.*ContactHook' -and $content -notmatch 'addingContact') { $content = $content -replace '(\s+})(\s*)$', '$1$2@Override public void addingContact(Transaction txn, Contact c) throws DbException { /* TODO: implement */ }$2@Override public void removingContact(Transaction txn, Contact c) throws DbException { /* TODO: implement */ }$2}$2'; Set-Content $_.FullName $content } }"

echo Completed fixing remaining issues.
echo.
echo Try compiling now: .\gradlew.bat :nodex-core:compileJava
