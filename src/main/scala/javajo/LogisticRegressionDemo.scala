package javajo

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Spam detection by Logistic Regression demo
 */
object LogisticRegressionDemo {
  def main(args: Array[String]) {
    val sparkConf = new SparkConf()
      .setMaster("local")
      .setAppName("javajo")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    // Load data set with schema

    val schema = new StructType(Array(
      StructField("label_", StringType, nullable = false),
      StructField("text", StringType, nullable = false)))

    val encodeLabel = udf[Double, String](s => if("ham".equals(s)) 1.0 else 0.0)

    val df = sqlContext.read.format("com.databricks.spark.csv")  // Use spark-csv
      .option("delimiter", "\t")
      .option("quote", "\u0000")  // To avoid error: java.io.IOException: (line 1) invalid char between encapsulated token and delimiter
      .schema(schema)
      .load("SMSSpamCollection")

    val dataSet = df.withColumn("label", encodeLabel(df("label_")))

    // Build ML pipeline

    val tokenizer = new Tokenizer()
      .setInputCol("text")
      .setOutputCol("words")

    val hashingTF = new HashingTF()
      .setInputCol(tokenizer.getOutputCol)
      .setOutputCol("features")

    val lr = new LogisticRegression()
      .setMaxIter(10)

    val pipeline = new Pipeline()
      .setStages(Array(tokenizer, hashingTF, lr))

    // Cross validation & parameter tuning

    val paramGrid = new ParamGridBuilder()
      .addGrid(hashingTF.numFeatures, Array(100, 1000, 10000))
      .addGrid(lr.regParam, Array(10.0, 1.0, 0.1))
      .addGrid(lr.maxIter, Array(10, 50, 100))
      .build()

    val cv = new CrossValidator()
      .setNumFolds(10)
      .setEstimator(pipeline)
      .setEstimatorParamMaps(paramGrid)
      .setEvaluator(new BinaryClassificationEvaluator)

    val model = cv.fit(dataSet)

    // (use model for classification)
  }
}
