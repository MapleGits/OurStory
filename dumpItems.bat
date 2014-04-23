@echo off
@title Dump Items
set CLASSPATH=.;dist\*
java -Dwz=wz\ tools.wztosql.DumpItems
pause