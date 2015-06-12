# Introduction #

While background compatibility is preserved whenever possible in each new version of HyperGraphDB, frequently the data storage layer will be updated in order to take advantage of newer and better implementations. The storage layer is usually bundled with the HyperGraphDB distribution, but you may need to run some tools to update the disk format of your existing data. Such tools are normally not part of the distribution in order to reduce file size. Below you will find more information on how to use those tools.

In addition, sometimes you need to be aware of minor API changes which are generally documented in the release notes of each version.


# Upgrading Storage Data #

If the new release of HyperGraphDB requires a more recent version of BerkeleyDB, you will need to upgrade your existing data. The following official BerkeleyDB documentation explains how to do that:

http://www.oracle.com/technology/documentation/berkeley-db/db/installation/upgrade.html

To get the command line tools necessary for the upgrade, you may need to download and install BerkeleyDB (either prebuild binaries for Windows or compile under Linux/Unix) from:

http://www.oracle.com/technetwork/database/berkeleydb/downloads/index.html

Usually, an upgrade consists of shutting down the application, running the db\_recover utility, updating with the latest version and restarting the application.

**In any event, always backup your database files before performing any upgrade!**