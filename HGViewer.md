# Introduction #

The HGViewer is a library and an application that helps to visually represent parts of a given HyperGraph instance. It can be used as a standalone application or as a embeddable component. It is mostly based on [Piccolo](http://www.cs.umd.edu/hcil/jazz/index.shtml) project, which provides very useful visual features such as: zooming interface, infinite canvas, scene/camera support, etc. HGViewer lets you examine portions of a given HyperGraph by drawing it as a regular directed graph around a currently focused node. You can also completely examine the Java object of a given node or edge, as in a standard debugger.

## Feature Summary ##

  * Few different algorithms for proper layout of nodes
  * Fully-customizable styling/painting framework to control node/edge appearance
  * Debugger-like object inspector that lets you examine the underlying value of each node
  * Zooming support
  * Ability to export viewed graph in a number of formats, including: GIF, JPEG, PS, PDF and many more.
  * Additional drawing mode that lets you add hand-written comments over the viewed graph


## Screenshots ##

|Standalone |Embedded in Seco|
|:----------|:---------------|
|![![](http://www.freeimagehosting.net/uploads/th.f290f0e2be.jpg)](http://www.freeimagehosting.net/image.php?f290f0e2be.jpg)|![![](http://www.freeimagehosting.net/uploads/th.5cc059ad1e.jpg)](http://www.freeimagehosting.net/image.php?5cc059ad1e.jpg)|

## Standalone vs. Embedded ##
As you can see from the above screenshots, the two versions of the HGViewer have few differences. The standalone viewer provides lazier GUI interface: object inspector is always visible, portions of the viewed graph could be exported in another viewer in the built-in tabbed pane and so on. But you should explicitly place additional jars used by the viewed graphs in the /lib folder of the viewer installation and you don't have the scripting capabilities provided by Seco. Moreover the standalone version doesn't provide any means to switch focus to a completely different place in the graph, or do a search in some other way, as one could do by scripting within Seco. So using the viewer within Seco is the recommended way.

## Downloading and Installing HGViewer ##

All official release packages are located at the [Google Code downloads area](http://code.google.com/p/hypergraphdb/downloads/list).

HGViewer releases are distributed in compressed archive files  (`.tar.gz` for Unix platforms and `.zip` for Windows). To install a distribution, just unzip the archive into a directory of your own choosing. Then read the instructions below on how to use it.

## Distribution Archive Content ##

The unzipped archive contains the following:

| `Readme.txt`          | General information about the HGViewer disribution. |
|:----------------------|:----------------------------------------------------|
| `LicensingInformation` | The HGViewer license conditions.                    |
| `hgviewer.jar`         | The HGViewer jar.                                   |
| `run.cmd`             | Start script for Windows distribution only.         |
| `run.sh`              | Start script for Linux distribution only.           |
| `ViewerInSeco.nb`     | A short tutorial to be used by Seco.                |
| `jars/`                | Third-party Java library dependencies. Note that not all libraries in this directory are needed in every situation. Jar dependencies are described below . |
| `javadoc/`            | API documentation in HTML format.                   |

## Third-party Java library dependencies ##
The following table describes the HGViewer jar dependencies. It's up to you to decide which of them to include according the way you plan to use HGViewer.
| `freehep-base.jar`         | Common functionality to all freehepXXX jars. |
|:---------------------------|:---------------------------------------------|
| `freehep-graphics2d.jar`   | Common functionality to all freehepXXX jars. |
| `freehep-graphicsio.jar`   | Common functionality to all freehepXXX jars. |
| `freehep-graphicsio-gif.jar`         |Export the HGViewer canvas in GIF format.     |
| `freehep-graphicsio-pdf.jar`         | Export the HGViewer canvas in PDF format.    |
| `freehep-graphicsio-ps.jar`         | Export the HGViewer canvas in PS format.     |
| `freehep-graphicsio-svg.jar`         | Export the HGViewer canvas in SVG format.    |
| `freehep-graphicsio-swf.jar`         | Export the HGViewer canvas in SWF format.    |
| `l2fprod-common-sheet.jar`         | Needed for GUI sheets used to define/modify node/edge painters. |
| `org-netbeans-swing-outline.jar`         | Used for the Object Inspector used to examine the value of currently selected node in the viewer. |
| `piccolo.jar`              | Piccolo project jar.                         |
| `piccolox.jar`             | Piccolo project jar.                         |
All freehepXXX jars are used in a single place in the viewer - Export command. If you didn't plan to used it, you could remove these jars. If you are going to use HGViewer inside Seco, you'll not need piccolo, piccolox and l2fprod-common-sheet jars, because they are already included in Seco.

## Running HGViewer ##

HGViewer could be used as a standalone application, as part of Seco or embedded in a different application.
### Standalone ###
Edit the JAVA\_HOME and HGDB\_HOME variables in run.cmd/run.sh file corresponding to your OS  to point to the relevant directories, the run the batch script.
### Seco, Recommended ###

For more information and to download Seco, goto http://www.kobrix.com/seco.jsp.

Add hgviewer.jar and its dependency jars (except piccolo, piccolox and l2fprod-common-sheet) to Seco. That is, either copy them to the Seco lib directory or add in the scripting runtime context. In Seco, import the ViewerInSeco.nb file, then simply follow the short tutorial presented inside.
### Another application ###
You can embed HGViewer component in your own application. Here's a sample code that places a viewer in a JFrame:
```
JFrame f = new JFrame();
HyperGraph graph = HGEnvironment.get("c:/MyHyperGraph");
HGHandle h = graph.getTypeSystem().getTypeHandle(HGStats.class);
HGViewer viewer = new HGViewer(graph, h, 2, null);
f.getContentPane().add(viewer);
f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
f.addWindowListener(new WindowAdapter()
{
   public void windowClosing(WindowEvent e)
   {
      f.setVisible(false);
      //uncomment if you'd use only one instance  
      //graph.close();
   }
});
f.setMinimumSize(new Dimension(600, 400));
f.setVisible(true);
```

## Installing from Source Code ##

The HGViewer source code resides in the [Subversion repository](http://code.google.com/p/hypergraphdb/source/checkout) and it's part of the HyperGraphDB source tree, under the directory `viewer`.

## Source Code Directory Structure ##

Source code is organized in the following top-level directories:

| `src/` | The source code. Sub-divided under `java` |
|:-------|:------------------------------------------|
| `jars/` | Third-party Java library dependencies for the HGViewer. |

## Compiling the Source ##

HGViewer uses the [ANT build system](http://ant.apache.org). You can compile, build and zip the distribution with the top-level ant script. The only prerequisite is the HyperGraphDB jar which you can take from the HyperGraphDB distribution or build from source. For more information see http://code.google.com/p/hypergraphdb/wiki/IntroInstall. Place the hgdbfull.jar in the project's top directory or change the value of hgdb\_jar property to point to HyperGraphDB jar.

The following table describes the three targets used to build HyperGraphDB for deployment:

| **Target** | **Result** | **Contents** |
|:-----------|:-----------|:-------------|
| `jar`      | `hgviewer.jar` | Creates the HGViewer jar. |
| `dist-zip` | `hgviewer-dist-win.zip` | Creates zipped distribution. |
| `dist-tar.gz` | `hgviewer-dist-lin.zip` | Creates tar-gzipped distribution. |

**Tip**: Use the command `ant -p` to list all available targets for a project script.