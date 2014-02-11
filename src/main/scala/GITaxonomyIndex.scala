package ohnosequences.bio4j.bundles

import shapeless._
import shapeless.ops.hlist._
import ohnosequences.typesets._
import ohnosequences.statika._
import ohnosequences.statika.aws._
import ohnosequences.statika.ami._
import ohnosequences.bio4j.statika._
import ohnosequences.awstools.s3._
import ohnosequences.awstools.regions._
import com.ohnosequences.bio4j.titan.programs._
import java.io._

/* This bundle is important, it doesn't really import anything, but initializes Bio4j */
case object InitialBio4j extends Bundle() with AnyBio4jInstanceBundle {
  val dbLocation: File = new File("/media/ephemeral0/bio4jtitandb")

  override def install[D <: AnyDistribution](d: D): InstallResults = {
    if (!dbLocation.exists) dbLocation.mkdirs
    InitBio4jTitan.main(Array(dbLocation.getAbsolutePath))
    success(s"Initialized Bio4j DB in ${dbLocation}")
  }
}

case object GITaxonomyIndexRawData 
  extends RawDataBundle("ftp://ftp.ncbi.nih.gov/pub/taxonomy/gi_taxid_nucl.dmp.gz")

case object GITaxonomyIndexAPI extends APIBundle(NCBITaxonomyAPI :~: ∅){
}

case class GITaxonomyIndexProgram(
  table : File, // 1. Tax-id <--> Gi-id table file
  db    : File  // 2. Bio4j DB folder
) extends ImporterProgram(new IndexNCBITaxonomyByGiIdTitan(), Seq(
  table.getAbsolutePath, 
  db.getAbsolutePath
))

case object GITaxonomyIndexImportedData extends ImportedDataBundle(
    rawData = GITaxonomyIndexRawData :~: ∅,
    initDB = InitialBio4j,
    importDeps = NCBITaxonomyImportedData :~: ∅
  ) {
    override def install[D <: AnyDistribution](d: D): InstallResults = {
      GITaxonomyIndexProgram(
        table = GITaxonomyIndexRawData.inDataFolder("gi_taxid_nucl.dmp"),
        db    = dbLocation
      ).execute ->-
      success(s"Data ${name} is imported to ${dbLocation}")
    }
  }

case object GITaxonomyIndexModule extends ModuleBundle(GITaxonomyIndexAPI, GITaxonomyIndexImportedData)

case object GITaxonomyIndexMetadata extends generated.metadata.GiTaxonomyIndexModule()

case object GITaxonomyIndexRelease extends ReleaseBundle(
  ObjectAddress("bio4j.releases", 
                "gi_taxonomy_index/v" + GITaxonomyIndexMetadata.version.stripSuffix("-SNAPSHOT")), 
  GITaxonomyIndexModule
)

case object GITaxonomyIndexDistribution extends DistributionBundle(
  GITaxonomyIndexRelease,
  destPrefix = new File("/media/ephemeral0/")
)

