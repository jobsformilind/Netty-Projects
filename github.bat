@echo off 

echo ---Pull-----------------------------
git pull
echo. 

echo ----Status--------------------------
git status
echo.

echo ---Add------------------------------
git add -A -v
echo. 

echo ---Commit/Push----------------------
git commit -m "Commiting changes"
git push
echo. 

echo ----Status--------------------------
git status
echo. 
pause