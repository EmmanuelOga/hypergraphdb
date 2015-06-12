  * Fixed a bug in compiled queries around not-yet-existing types.
  * Fixed a bug when using HyperGraph.define on HGValueLink.
  * Bug fix in PositionedIncidentLink (thanks Yuqing Tang) and added non-Ref constructors to that class.
  * Added ability to analyze a query to detect time consuming scans and joins.
  * Added version information to each database instance, through HGDatabaseVersionFile (see `HGEnvironment.getVersions(location)`).
  * Fixed a bug  when creating a new type for a HGLink that is also a serializable: a link with an empty value is created in that case because the intention of serializable support is for classes not written for HGDB. Classes written for HGDB should expose their state through standard bean properties.