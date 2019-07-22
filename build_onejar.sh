HOME="$( cd "$(dirname "$0")" ; pwd -P )"
TARGETDIR=$HOME/target
ORIGJAR=$TARGETDIR/app-twtrick-0.0.1-SNAPSHOT.jar
TARGETJAR=$TARGETDIR/twtrick.jar
 
# build app-candle firstly
#mvn -f $HOME/../app-candle/pom.xml install -DskipTests
 
 
# clean firstly
mvn -f $HOME/pom.xml clean
 
# Build One Jar firstly
mvn -f $HOME/pom.xml package
 
# Rename jar to TARGET
mv $ORIGJAR $TARGETJAR 