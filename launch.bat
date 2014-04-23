@echo off
@title MagmaStory
set CLASSPATH=.;dist\*
java -client -Dnet.sf.odinms.wzpath=wz server.Start
pause