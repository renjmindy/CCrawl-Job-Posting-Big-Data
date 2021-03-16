package com.revature.ccrawlsapp

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.BasicAWSCredentials
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions
import org.apache.spark.sql.SaveMode
import org.apache.spark.SparkContext

object Runner {
  def main(args: Array[String]): Unit = {

    if(args.length != 2) {
      println("please specify: `yyyy-mm` [e.g. 2020-16] `Nrows` [e.g. 2000] ")
      System.exit(1)
    }

    val spark = SparkSession
      .builder()
      .config("spark.debug.maxToStringFields", 100)
      .appName("ccrawlsapp")
      .master("local[4]")
      .getOrCreate()

    // Reference: https://sparkbyexamples.com/spark/spark-read-text-file-from-s3/#s3-dependency
    val key = System.getenv(("AWS_ACCESS_KEY_ID"))
    val secret = System.getenv(("AWS_SECRET_ACCESS_KEY"))

    spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", key)
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", secret)
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "s3.amazonaws.com")
    
    import spark.implicits._
    spark.sparkContext.setLogLevel("WARN")

    /*val dfExpFromFile = spark.read.parquet("s3a://commoncrawl/cc-index/table/cc-main/warc/crawl=CC-MAIN-2021-04/subset=warc/part-00299-364a895c-5e5c-46bb-846e-75ec7de82b3b.c000.gz.parquet")
    dfExpFromFile.printSchema()
    dfExpFromFile.columns.take(2000).foreach(println)*/
/*
root
 |-- url_surtkey: string (nullable = true)
 |-- url: string (nullable = true)
 |-- url_host_name: string (nullable = true)
 |-- url_host_tld: string (nullable = true)
 |-- url_host_2nd_last_part: string (nullable = true)
 |-- url_host_3rd_last_part: string (nullable = true)
 |-- url_host_4th_last_part: string (nullable = true)
 |-- url_host_5th_last_part: string (nullable = true)
 |-- url_host_registry_suffix: string (nullable = true)
 |-- url_host_registered_domain: string (nullable = true)
 |-- url_host_private_suffix: string (nullable = true)
 |-- url_host_private_domain: string (nullable = true)
 |-- url_protocol: string (nullable = true)
 |-- url_port: integer (nullable = true)
 |-- url_path: string (nullable = true)
 |-- url_query: string (nullable = true)
 |-- fetch_time: timestamp (nullable = true)
 |-- fetch_status: short (nullable = true)
 |-- fetch_redirect: string (nullable = true)
 |-- content_digest: string (nullable = true)
 |-- content_mime_type: string (nullable = true)
 |-- content_mime_detected: string (nullable = true)
 |-- content_charset: string (nullable = true)
 |-- content_languages: string (nullable = true)
 |-- content_truncated: string (nullable = true)
 |-- warc_filename: string (nullable = true)
 |-- warc_record_offset: integer (nullable = true)
 |-- warc_record_length: integer (nullable = true)
 |-- warc_segment: string (nullable = true)

url_surtkey
url
url_host_name
url_host_tld
url_host_2nd_last_part
url_host_3rd_last_part
url_host_4th_last_part
url_host_5th_last_part
url_host_registry_suffix
url_host_registered_domain
url_host_private_suffix
url_host_private_domain
url_protocol
url_port
url_path
url_query
fetch_time
fetch_status
fetch_redirect
content_digest
content_mime_type
content_mime_detected
content_charset
content_languages
content_truncated
warc_filename
warc_record_offset
warc_record_length
warc_segment
*/
    val dfFromFile = spark.read.load("s3a://commoncrawl/cc-index/table/cc-main/warc/")

    /*val dfSelectedFromFile = dfFromFile
    .select("url_surtkey", "url", "url_host_name", "url_host_registry_suffix", "url_host_registered_domain", 
    "url_host_private_suffix", "url_host_private_domain", "url_protocol", "url_port", "url_path", "url_query", 
    "fetch_time", "fetch_status", "content_digest", "content_mime_type", "content_mime_detected", "warc_filename", 
    "warc_record_offset", "warc_record_length", "warc_segment")
    .filter($"crawl" === "CC-MAIN-"+args(0))
    .filter($"subset" === "warc")
    .filter($"url_path".rlike("job") || $"url_path".rlike("career"))*/

    val dfSelectedFromFile = dfFromFile
    .select("url", "warc_filename")
    .filter($"crawl" === "CC-MAIN-"+args(0))
    .filter($"subset" === "warc")
    .filter($"url_path".rlike("job") || $"url_path".rlike("career"))

    //dfSelectedFromFile.take(50000).foreach(println)
    
    val n = args(1)

    dfSelectedFromFile.show(n.toInt, false)

    dfSelectedFromFile.explain(true)

    println(dfSelectedFromFile.rdd.toDebugString)

  }
}