# FreeMind Fork for Training

Large Java Code Base to explore (and try to fix) for Training Purposes.

## FreeMind

> [FreeMind](http://freemind.sourceforge.net) is a premier, free mind-mapping program written in Java.
> It lets you organize your ideas as a "mindmap," a graphical structure of nodes and branches
> (or edges) attached to a central "root" node.

### Fork

FreeMind is hosted on [SourceForge](https://sourceforge.net/projects/freemind/).
This is a copy of FreeMind 1.1.0 Beta 2 (`freemind-code`) at commit
[0dc638d2f2d67339b4fb507af975a5818dc88843](TODO link commit)
at TODO date of commit.

This fork is not for further development, but for experiments and exercises.
I removed all languages except English, plugins excluded from built as well as
binary launchers.

### Why FreeMind?

I am searching for large open source projects. There are several available. For example

* JDK libraries
* Tomcat
* JBoss
* Hibernate
* Derby
* Eclipse and NetBeans

All of them are great software, mostly frameworks or infrastructure code.
I would like something visual, an application supporting a domain which is easy to understand.
Tools like [RssOWL](http://www.rssowl.org/) or [ArgoUML](http://argouml.tigris.org/) fall
into this category. Both are Eclipse RCP applications, which makes working with them difficult
if you are not used to Eclipse RCP.

[FreeMind](http://freemind.sourceforge.net) is a visual application with only a few dependencies.
I use it regularly and it is great.

## Code Base

This version of FreeMind has based on Java 1.7. 
The user interface is using Swing and I18n is in place.
FreeMind depends only on a few libraries, half of them are for XML processing.

### Metrics

How large is large enough?

* 504 Java files
* 106.418 LoC

So it is not *that* large but not small either.

### Tests

* 25 Unit tests using JUnit 3
* 5.937 LoC
* 15 tests are active, listed in `Alltests`.

### Build

Eclipse configutation files are checked in. It is possible to start FreeMind directly from
Eclipse with class `freemind.main.FreeMindStarter`.

To build from sources we need [Apache Ant](http://ant.apache.org/). Use

* `ant dist` - to compile the sources,
* `ant post` - to create a new delivery version,
* `ant run` - to start FreeMind from the sources.

### License
GNU GENERAL PUBLIC LICENSE, see `license.txt` in repository.
