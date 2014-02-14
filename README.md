## GI Taxonomy Index Bio4j module

This is a Bio4j module representing GI Taxonomy Index. Find more information about [Bio4j modules](https://github.com/bio4j/modules).

The point of this module is to index [NCBI Taxonomy](https://github.com/bio4j/ncbi-taxonomy-module) with GI IDs.

## Usage

To use it in you sbt-project, add this to you `build.sbt`:

```scala
resolvers += "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"

libraryDependencies += "bio4j" %% "gi-taxonomy-index-module" % "0.1.0"
```
