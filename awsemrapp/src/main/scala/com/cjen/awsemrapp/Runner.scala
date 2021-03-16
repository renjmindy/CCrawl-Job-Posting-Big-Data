package com.cjen.awsemrapp

import org.apache.spark.sql.SparkSession
import org.apache.spark._

object Runner {
    
    def main(args: Array[String]): Unit = {
        
        val spark = SparkSession
        .builder()
        .appName("commoncrawl emr demo")
        .getOrCreate()
    
        import spark.implicits._
        spark.sparkContext.setLogLevel("WARN")

        helloSparkEMR(spark)

    }

    def helloSparkEMR(spark: SparkSession): Unit = {

        import spark.implicits._

        val df = spark.read.load("s3a://commoncrawl/cc-index/table/cc-main/warc/")

        val crawl = "CC-MAIN-2021-04"
        val jobUrls = df
        .select("url_host_name", "url_path")
        .filter($"crawl" === crawl)
        .filter($"subset" === "warc")
        .filter($"url_path".contains("job"))
        .limit(500000)

        val s3OutputBucket = "s3a://output-bucket/commoncrawl-demo-data"

        jobUrls.write.format("csv").mode("overwrite").save(s3OutputBucket)

        spark.close
    }

}