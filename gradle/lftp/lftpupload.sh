#!/bin/bash

SCRIPTDIR="$( cd "$( dirname "$0" )" && pwd )"
REPODIR="$SCRIPTDIR/../.."

TEMPDIR="$SCRIPTDIR/temp"
TEMPDISTDIR="$TEMPDIR/dist"

if [ "$SYNCANY_FTP_HOST" == "" -o "$SYNCANY_FTP_USER" == "" -o "$SYNCANY_FTP_PASS" == "" ]; then
	echo "ERROR: SYNCANY_FTP_* environment variables not set."
	exit 1
fi

# Reset tempdir
rm -rf "$TEMPDIR"
mkdir $TEMPDIR
mkdir $TEMPDISTDIR

# Gather deb/tar-gz/zip
echo ""
echo "Gathering distributables ..."
echo "----------------------------"
cp $REPODIR/syncany-cli/build/distributions/*.{zip,tar.gz} $TEMPDISTDIR
cp $REPODIR/syncany-cli/build/linux-package/*.deb $TEMPDISTDIR
cp $REPODIR/syncany-cli/build/innosetup/*.exe $TEMPDISTDIR

ls $TEMPDISTDIR

if [ $(ls $TEMPDISTDIR | wc -l) != "4" ]; then
	echo "ERROR: Wrong files in $TEMPDISTDIR: "
	ls $TEMPDISTDIR
	
	exit 2
fi

# Gather JavaDoc & Test Reports
echo ""
echo "Gathering JavaDoc & Test Reports ..."
echo "---------------------"

PWD=`pwd`
cd $REPODIR/build
rm javadoc.tar.gz 2> /dev/null
rm reports.tar.gz 2> /dev/null
tar -czf javadoc.tar.gz javadoc/
tar -czf reports.tar.gz reports/
mv javadoc.tar.gz $TEMPDIR/
mv reports.tar.gz $TEMPDIR/
cd "$PWD"

ls $TEMPDIR

# Copy to FTP 
echo "Uploading files to Syncany FTP ..."

lftp -c "open ftp://$SYNCANY_FTP_HOST
user $SYNCANY_FTP_USER $SYNCANY_FTP_PASS
mirror --reverse --exclude javadoc --exclude reports --delete --parallel=3 --verbose $TEMPDIR /
bye
"

# Delete tempdir
rm -rf "$TEMPDIR"
