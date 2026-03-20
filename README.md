eXist-db GML Spatial Index (V2)
A high-precision spatial index for eXist-db, specifically designed to overcome the limitations of standard indexes when handling complex GML (2.x and 3.x) data, particularly for coastal areas and high-latitude projections (e.g., Brest, Brittany).

🚀 Key Features
Universal GML Parsing: Hybrid support for both coordinates (GML 2.x) and posList (GML 3.x) tags.

Topology Management: Full support for complex polygons with holes (exterior / interior boundaries).

Geodetic Precision: Dynamic Padding calculation via Proj4J to compensate for projection distortions (Lambert-93, etc.), ensuring 100% recall on windowing queries.

Robust SQL Storage: Powered by HSQLDB for high-performance, transactional BBox indexing.

Modular Architecture: Strict separation between Parsing (GML), Projection Logic (Geodesy), and Storage Backends (SQL, JSON, etc.).

🛠️ Installation
1. Compilation
The project uses Maven. To generate the "Fat JAR" (including JTS and Proj4J dependencies):

Bash
mvn clean package
2. Deployment in eXist-db
Copy the generated file target/gml-index-2.0.0-SNAPSHOT.jar into the lib/ (or lib/user/) directory of your eXist-db instance.

Declare the index module in your conf.xml file:

XML
<modules>
    <module class="org.exist.indexing.spatial.SpatialIndex" id="spatial-index" />
</modules>
3. Collection Configuration
In your collection.xconf, enable the index on your geographic elements:

XML
<collection xmlns="http://exist-db.org/collection-config/1.0">
    <index>
        <custom-index class="org.exist.indexing.spatial.SpatialIndex">
            <parameter name="data-dir" value="webapp/WEB-INF/data/spatial-index"/>
        </custom-index>
    </index>
</collection>
📂 Project Structure
SpatialIndex.java: Main entry point (Orchestrator) for eXist-db.

SpatialIndexWorker.java: Handles document updates and deletions.

UniversalGMLHandler.java: High-performance StAX parsing of XML streams.

AbstractSpatialStore.java: Core logic for BBox expansion (Padding).

BBoxOrientedSQLStore.java: SQL implementation of the storage backend.

ProjectionService.java: Geo-differential calculation service.

CoordinateParser.java: Robust coordinate string tokenizer.

⚖️ License
This project is licensed under the GNU Lesser General Public License v3.0 (LGPL-3.0), consistent with the eXist-db core project.
